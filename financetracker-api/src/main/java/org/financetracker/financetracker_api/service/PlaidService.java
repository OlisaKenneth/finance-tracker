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
 */
@Service
public class PlaidService {

    // the finished PlaidApi connector built in PlaidConfig.java
    private final PlaidApi plaidClient;

    // lets us find and save PlaidItems (access tokens) from the DB
    private final PlaidItemRepository plaidItemRepository;

    // lets us save converted transactions into OUR transactions table
    private final TransactionRepository transactionRepository;

    // Spring automatically passes all three dependencies in here
    public PlaidService(PlaidApi plaidClient,
                        PlaidItemRepository plaidItemRepository,
                        TransactionRepository transactionRepository) {
        this.plaidClient = plaidClient;
        this.plaidItemRepository = plaidItemRepository;
        this.transactionRepository = transactionRepository;
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

        // if something went wrong, fail loudly with a clear message
        if (!response.isSuccessful()) {
            throw new IOException("Plaid link token creation failed: " + response.errorBody());
        }

        // pull just the link_token string out and hand it back
        return response.body().getLinkToken();
    }

    /*
     * Takes the TEMPORARY public_token and trades it with Plaid
     * for a PERMANENT access_token, then saves it to our DB
     * linked to the user who just connected their bank
     *
     * UPDATED: checks if user already has a synced bank connection
     * before saving a new PlaidItem — prevents duplicate connections
     */
    public void exchangePublicToken(String publicToken, User currentUser) throws IOException {

        /*
         * NEW DUPLICATE CONNECTION CHECK
         *
         * Before doing anything, check if this user already has
         * a bank connection that has been fully synced.
         *
         * If yes — stop here, do nothing.
         * We don't want to save a second PlaidItem because that
         * would let the user sync the same transactions again.
         *
         * In production with a real bank, users connect once and
         * never need to connect again — this guard enforces that.
         *
         * In sandbox, every "Connect my bank" click creates a brand
         * new Plaid item with new transaction IDs — this check
         * prevents that from creating duplicate transactions.
         */
        Optional<PlaidItem> existingItem = plaidItemRepository
                .findFirstByUserIdOrderByIdDesc(currentUser.getId());

        if (existingItem.isPresent() && existingItem.get().isSynced()) {
            // user already has a synced bank connection — skip entirely
            // do not save a new PlaidItem, do not call Plaid
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
     * Returns Plaid's raw list — syncTransactions() converts them
     */
    public List<Transaction> pullTransactions(String accessToken) throws IOException {

        // this list will collect ALL transactions across all pages
        List<Transaction> allTransactions = new ArrayList<>();

        // empty cursor means "I've never asked before, give me everything"
        String cursor = null;

        // keeps looping until Plaid says there are no more pages
        boolean hasMore = true;

        while (hasMore) {

            // build the request with our access token and current bookmark
            TransactionsSyncRequest request = new TransactionsSyncRequest()
                    .accessToken(accessToken)
                    .cursor(cursor);

            // send the request to Plaid and wait for the response
            Response<TransactionsSyncResponse> response =
                    plaidClient.transactionsSync(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Plaid transactions sync failed: " + response.errorBody());
            }

            // unwrap the response body so we can read from it
            TransactionsSyncResponse body = response.body();

            // "added" = brand new transactions we haven't seen yet
            allTransactions.addAll(body.getAdded());

            // update our bookmark to the end of this page
            cursor = body.getNextCursor();

            // check if there is another page waiting
            hasMore = body.getHasMore();
        }

        return allTransactions;
    }

    /*
     * THE CONVERTER + SAVER
     *
     * 1. Finds this user's most recent PlaidItem (access token)
     * 2. Checks if we already synced this bank connection
     *    — if yes, returns empty list immediately (no duplicates)
     *    — if no, pulls transactions from Plaid
     * 3. Converts each Plaid transaction into OUR Transaction model
     * 4. Saves each one using Plaid's transaction ID as duplicate check
     * 5. Marks the PlaidItem as synced so future calls are skipped
     */
    public List<org.financetracker.financetracker_api.model.Transaction> syncTransactions(User currentUser) throws IOException {

        // Step 1: find this user's most recent PlaidItem
        // if no bank is connected yet, throw a clear error
        PlaidItem plaidItem = plaidItemRepository
                .findFirstByUserIdOrderByIdDesc(currentUser.getId())
                .orElseThrow(() -> new IOException("No bank connected for this user"));

        // Step 2: if we already synced this connection, return empty list
        // this prevents duplicate transactions when the user connects again
        if (plaidItem.isSynced()) {
            return new ArrayList<>();
        }

        // Step 3: get the raw Plaid transactions using the saved access token
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
            String description = plaidTx.getMerchantName() != null
                    ? plaidTx.getMerchantName()
                    : "No description";

            // take the first category from Plaid's list, or "Uncategorized"
            String category;
            if (plaidTx.getCategory() != null && !plaidTx.getCategory().isEmpty()) {
                category = plaidTx.getCategory().get(0);
            } else {
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
        // future calls to syncTransactions() will return empty list immediately
        plaidItem.setSynced(true);
        plaidItemRepository.save(plaidItem);

        return saved;
    }
}