package org.financetracker.financetracker_api.service;

import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.Products;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.ItemPublicTokenExchangeRequest;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.TransactionsSyncRequest;
import com.plaid.client.model.TransactionsSyncResponse;
import com.plaid.client.model.Transaction;
import org.financetracker.financetracker_api.model.PlaidItem;
import org.financetracker.financetracker_api.model.User;
import org.financetracker.financetracker_api.repository.PlaidItemRepository;
import org.financetracker.financetracker_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/*
 * PlaidService — THE WORKER
 *
 * Handles all communication with Plaid:
 * 1. createLinkToken()      — prepares the popup ticket
 * 2. exchangePublicToken()  — trades the ticket for a permanent key
 * 3. pullTransactions()     — fetches raw Plaid transactions
 * 4. syncTransactions()     — converts + saves them to OUR DB
 *                             now with AI categorization via Claude
 */
@Service
public class PlaidService {

    // the finished PlaidApi connector built in PlaidConfig.java
    private final PlaidApi plaidClient;

    // lets us find and save PlaidItems (access tokens) from the DB
    private final PlaidItemRepository plaidItemRepository;

    // lets us save converted transactions into OUR transactions table
    private final TransactionRepository transactionRepository;

    // NEW — the AI service that categorizes transactions via Claude
    private final AIService aiService;

    // Spring automatically passes all four dependencies in here
    public PlaidService(PlaidApi plaidClient,
                        PlaidItemRepository plaidItemRepository,
                        TransactionRepository transactionRepository,
                        AIService aiService) {
        this.plaidClient = plaidClient;
        this.plaidItemRepository = plaidItemRepository;
        this.transactionRepository = transactionRepository;
        this.aiService = aiService;
    }

    /*
     * Asks Plaid to prepare a link_token for this user
     * Returns the link_token as a plain String
     */
    public String createLinkToken(String userId) throws IOException {

        // tell Plaid which of OUR users this is for
        LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser()
                .clientUserId(userId);

        // describe what kind of data we want — transactions
        List<Products> products = Arrays.asList(Products.TRANSACTIONS);

        // build the full request object Plaid expects
        LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                .user(user)
                .clientName("Finance Tracker")
                .products(products)
                .countryCodes(Arrays.asList(CountryCode.US))
                .language("en");

        // send the request to Plaid and wait for the response
        Response<LinkTokenCreateResponse> response =
                plaidClient.linkTokenCreate(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Plaid link token creation failed: " + response.errorBody());
        }

        return response.body().getLinkToken();
    }

    /*
     * Takes the TEMPORARY public_token and trades it with Plaid
     * for a PERMANENT access_token, then saves it to our DB
     *
     * GUARDS against duplicate connections:
     * if user already has a synced PlaidItem, skip entirely
     */
    public void exchangePublicToken(String publicToken, User currentUser) throws IOException {

        // check if this user already has a synced bank connection
        // if yes, don't save a new PlaidItem — prevents duplicates
        Optional<PlaidItem> existingItem = plaidItemRepository
                .findFirstByUserIdOrderByIdDesc(currentUser.getId());

        if (existingItem.isPresent() && existingItem.get().isSynced()) {
            // already have a synced connection — skip entirely
            return;
        }

        // build the request — just the claim ticket we want to trade in
        ItemPublicTokenExchangeRequest request =
                new ItemPublicTokenExchangeRequest().publicToken(publicToken);

        // send it to Plaid and wait for the permanent key back
        Response<ItemPublicTokenExchangeResponse> response =
                plaidClient.itemPublicTokenExchange(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Plaid token exchange failed: " + response.errorBody());
        }

        // pull the permanent access_token and item_id out of the response
        String accessToken = response.body().getAccessToken();
        String itemId = response.body().getItemId();

        // save that permanent key safely in our DB, linked to the logged-in user
        // synced = false by default — transactions not pulled yet
        PlaidItem plaidItem = new PlaidItem();
        plaidItem.setAccessToken(accessToken);
        plaidItem.setItemId(itemId);
        plaidItem.setUser(currentUser);
        plaidItemRepository.save(plaidItem);
    }

    /*
     * Asks Plaid for raw transactions using the permanent access token
     * Plaid sends results in pages — we loop until hasMore = false
     */
    public List<Transaction> pullTransactions(String accessToken) throws IOException {

        List<Transaction> allTransactions = new ArrayList<>();
        String cursor = null;
        boolean hasMore = true;

        while (hasMore) {

            TransactionsSyncRequest request = new TransactionsSyncRequest()
                    .accessToken(accessToken)
                    .cursor(cursor);

            Response<TransactionsSyncResponse> response =
                    plaidClient.transactionsSync(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Plaid transactions sync failed: " + response.errorBody());
            }

            TransactionsSyncResponse body = response.body();
            allTransactions.addAll(body.getAdded());
            cursor = body.getNextCursor();
            hasMore = body.getHasMore();
        }

        return allTransactions;
    }

    /*
     * THE CONVERTER + SAVER + AI CATEGORIZER
     *
     * 1. Finds this user's most recent PlaidItem (access token)
     * 2. Checks if already synced — if yes, returns empty list
     * 3. Pulls raw transactions from Plaid
     * 4. For each transaction:
     *    a. Checks for duplicates using Plaid's transaction ID
     *    b. Gets description from merchant name
     *    c. NEW: asks Claude to categorize the description
     *    d. Saves the transaction with the AI category
     * 5. Marks the PlaidItem as synced
     */
    public List<org.financetracker.financetracker_api.model.Transaction> syncTransactions(User currentUser) throws IOException {

        // Step 1: find this user's most recent PlaidItem
        PlaidItem plaidItem = plaidItemRepository
                .findFirstByUserIdOrderByIdDesc(currentUser.getId())
                .orElseThrow(() -> new IOException("No bank connected for this user"));

        // Step 2: if already synced, return empty list immediately
        if (plaidItem.isSynced()) {
            return new ArrayList<>();
        }

        // Step 3: get the raw Plaid transactions
        List<Transaction> plaidTransactions = pullTransactions(plaidItem.getAccessToken());

        // Step 4: convert each Plaid transaction into OUR Transaction model
        List<org.financetracker.financetracker_api.model.Transaction> saved = new ArrayList<>();

        for (Transaction plaidTx : plaidTransactions) {

            // get Plaid's own unique ID for this transaction
            String plaidTransactionId = plaidTx.getTransactionId();

            // skip if we already saved this exact transaction before
            if (plaidTransactionId != null &&
                    transactionRepository.existsByPlaidTransactionId(plaidTransactionId)) {
                continue;
            }

            // Math.abs() converts Plaid's negative credits to positive
            double amount = Math.abs(plaidTx.getAmount());

            // get the date as a String — format "2026-06-26"
            String date = plaidTx.getDate() != null
                    ? plaidTx.getDate().toString()
                    : "Unknown date";

            // get the merchant name as description
            // fall back to "No description" if Plaid gives nothing
            String description = plaidTx.getMerchantName() != null
                    ? plaidTx.getMerchantName()
                    : "No description";

            /*
             * NEW — AI CATEGORIZATION
             *
             * Instead of using Plaid's category list directly,
             * we ask Claude to categorize based on the description.
             *
             * Why? Because Plaid's categories don't always match
             * your budget categories. Claude maps them intelligently.
             *
             * Example:
             * description = "TIM HORTONS #4521"
             * Claude returns = "Food and Drink"
             *
             * If AI fails for any reason, falls back to Plaid's
             * first category, or "Uncategorized" if none exists
             */
            String category;
            if (!description.equals("No description")) {
                // ask Claude to categorize this transaction
                category = aiService.categorize(description);
            } else if (plaidTx.getCategory() != null && !plaidTx.getCategory().isEmpty()) {
                // no merchant name — fall back to Plaid's own category
                category = plaidTx.getCategory().get(0);
            } else {
                // no category from either source
                category = "Uncategorized";
            }

            // build our Transaction object with the converted values
            org.financetracker.financetracker_api.model.Transaction tx =
                    new org.financetracker.financetracker_api.model.Transaction();

            tx.setAmount(amount);
            tx.setCategory(category);
            tx.setDescription(description);
            tx.setDate(date);
            tx.setUser(currentUser);
            tx.setPlaidTransactionId(plaidTransactionId);

            // save to DB and add to results list
            org.financetracker.financetracker_api.model.Transaction savedTx =
                    transactionRepository.save(tx);

            saved.add(savedTx);
        }

        // Step 5: mark this PlaidItem as synced
        plaidItem.setSynced(true);
        plaidItemRepository.save(plaidItem);

        return saved;
    }
}