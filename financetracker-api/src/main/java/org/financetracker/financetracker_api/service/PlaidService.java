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
import org.financetracker.financetracker_api.repository.TransactionRepository; // lets us save transactions to our own DB
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * PlaidService — THE WORKER
 *
 * Handles all communication with Plaid:
 * 1. createLinkToken()      — prepares the popup ticket
 * 2. exchangePublicToken()  — trades the ticket for a permanent key
 * 3. pullTransactions()     — fetches raw Plaid transactions
 * 4. syncTransactions()     — NEW: converts + saves them to OUR DB
 */
@Service
public class PlaidService {

    // the finished PlaidApi connector built in PlaidConfig.java
    // Spring hands it to us automatically
    private final PlaidApi plaidClient;

    // lets us find PlaidItems (saved access tokens) from the DB
    private final PlaidItemRepository plaidItemRepository;

    // NEW — lets us save converted transactions into OUR transactions table
    private final TransactionRepository transactionRepository;

    // Spring automatically passes all three dependencies in here
    public PlaidService(PlaidApi plaidClient,
                        PlaidItemRepository plaidItemRepository,
                        TransactionRepository transactionRepository) {
        this.plaidClient = plaidClient;
        this.plaidItemRepository = plaidItemRepository;
        this.transactionRepository = transactionRepository; // store it so our methods can use it
    }

    /*
     * Asks Plaid: "please prepare a link_token for this user."
     *
     * userId — identifies WHICH of our own users this ticket
     *          is being prepared for
     *
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
     */
    public void exchangePublicToken(String publicToken, User currentUser) throws IOException {

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
        PlaidItem plaidItem = new PlaidItem();
        plaidItem.setAccessToken(accessToken);
        plaidItem.setItemId(itemId);
        plaidItem.setUser(currentUser);
        plaidItemRepository.save(plaidItem);
    }

    /*
     * THE MAIL CARRIER — asks Plaid for raw transactions
     *
     * Uses the PERMANENT access_token to ask Plaid:
     * "give me this account's transactions."
     *
     * Plaid sends results in PAGES — so we keep asking
     * "is there more?" until it says no.
     *
     * Returns Plaid's raw list of transactions — these are
     * Plaid's own Transaction objects, not ours yet.
     * syncTransactions() below converts them into our format.
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
            // add this page's transactions to our growing list
            allTransactions.addAll(body.getAdded());

            // update our bookmark to the end of this page
            cursor = body.getNextCursor();

            // check if there is another page waiting
            hasMore = body.getHasMore();
        }

        // return the full list of ALL raw Plaid transactions
        return allTransactions;
    }

    /*
     * NEW — THE CONVERTER + SAVER
     *
     * This is the method that was missing.
     *
     * It does three things in order:
     * 1. Finds this user's saved access token from the DB
     * 2. Calls pullTransactions() to get the raw Plaid data
     * 3. Converts each Plaid transaction into OUR Transaction
     *    model and saves it to OUR transactions table
     *
     * Think of it like this:
     * Plaid speaks "Plaid language" — their Transaction object
     * has different field names and structure than ours.
     * This method is the TRANSLATOR that converts their format
     * into our format, then saves it.
     *
     * currentUser — the logged-in user, so we can:
     *   a) find THEIR access token
     *   b) stamp each saved transaction with THEIR user_id
     */
    public List<org.financetracker.financetracker_api.model.Transaction> syncTransactions(User currentUser) throws IOException {

        // Step 1: find this user's PlaidItem (their saved access token)
        // findFirstByUserId returns an Optional — meaning it might not exist
        // if the user hasn't connected a bank yet, throw a clear error
        PlaidItem plaidItem = plaidItemRepository
                .findFirstByUserId(currentUser.getId())
                .orElseThrow(() -> new IOException("No bank connected for this user"));

        // Step 2: get the raw Plaid transactions using the saved access token
        // these come back as Plaid's own Transaction objects, not ours
        List<Transaction> plaidTransactions = pullTransactions(plaidItem.getAccessToken());

        // Step 3: convert each Plaid transaction into OUR Transaction model
        // this list will hold the converted + saved versions
        List<org.financetracker.financetracker_api.model.Transaction> saved = new ArrayList<>();

        // loop through every raw Plaid transaction one by one
        for (Transaction plaidTx : plaidTransactions) {

            // create a blank OUR Transaction object to fill in
            org.financetracker.financetracker_api.model.Transaction tx =
                    new org.financetracker.financetracker_api.model.Transaction();

            // Plaid stores amount as a decimal — same type as ours
            // note: Plaid amounts are POSITIVE for money leaving
            // the account (spending), which matches our convention
            tx.setAmount(plaidTx.getAmount());

            // Plaid gives us a list of categories e.g. ["Food and Drink", "Restaurants"]
            // we take the FIRST one as our single category string
            // if there are no categories at all, we fall back to "Uncategorized"
            if (plaidTx.getCategory() != null && !plaidTx.getCategory().isEmpty()) {
                tx.setCategory(plaidTx.getCategory().get(0)); // first category in Plaid's list
            } else {
                tx.setCategory("Uncategorized"); // safe fallback if Plaid sends nothing
            }

            // Plaid gives us the merchant name as the description
            // e.g. "Starbucks", "Walmart", "Netflix"
            // if there is no merchant name, fall back to "No description"
            String merchantName = plaidTx.getMerchantName();
            tx.setDescription(merchantName != null ? merchantName : "No description");

            // Plaid's date comes back as a LocalDate object
            // we convert it to a String using toString() which gives "2026-07-14" format
            // this matches the date format we already use in our own transactions
            tx.setDate(plaidTx.getDate() != null ? plaidTx.getDate().toString() : "Unknown date");

            // stamp this transaction with the logged-in user's id
            // same ownership pattern used on every other entity
            tx.setUser(currentUser);

            // save this one converted transaction to OUR database
            // transactionRepository.save() returns the saved version with its generated id
            org.financetracker.financetracker_api.model.Transaction savedTx =
                    transactionRepository.save(tx);

            // add the saved transaction to our results list
            saved.add(savedTx);
        }

        // return all the saved transactions so the controller
        // can send them back to React
        return saved;
    }
}