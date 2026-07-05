package org.financetracker.financetracker_api.service;

import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.Products;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.ItemPublicTokenExchangeRequest;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import org.financetracker.financetracker_api.model.PlaidItem;
import org.financetracker.financetracker_api.model.User;
import org.financetracker.financetracker_api.repository.PlaidItemRepository;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/*
 * PlaidService — THE WORKER
 *
 * This class's ONE job right now: ask Plaid for a
 * "link_token" — the empty, ready-to-go claim ticket
 * from our coat-check story.
 *
 * Nothing about a real bank happens yet. This step just
 * prepares an empty ticket that React will use to open
 * Plaid's popup window.
 */
@Service
public class PlaidService {

    // The finished PlaidApi connector we built in
    // PlaidConfig.java — Spring hands it to us automatically
    private final PlaidApi plaidClient;

    // Lets us SAVE the permanent access_token safely once
    // we get it back from Plaid
    private final PlaidItemRepository plaidItemRepository;

    public PlaidService(PlaidApi plaidClient, PlaidItemRepository plaidItemRepository) {
        this.plaidClient = plaidClient;
        this.plaidItemRepository = plaidItemRepository;
    }

    /*
     * Asks Plaid: "please prepare a link_token for this user."
     *
     * userId — identifies WHICH of our own users this ticket
     *          is being prepared for (so later we know whose
     *          bank connection this becomes)
     *
     * Returns the link_token as a plain String, ready to be
     * sent straight back to the frontend.
     */
    public String createLinkToken(String userId) throws IOException {

        // Step 1: tell Plaid which of OUR users this is for.
        // Plaid doesn't know or care about our database —
        // this is just a label so we can match things up later.
        LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser()
                .clientUserId(userId);

        // Step 2: describe what kind of data we want access to.
        // Products.TRANSACTIONS is Plaid's own built-in label
        // for "the actual list of purchases/spending"
        List<Products> products = Arrays.asList(Products.TRANSACTIONS);

        // Step 3: build the actual request object Plaid expects.
        // clientName = the name shown to the user inside the
        // Plaid popup (e.g. "Finance Tracker wants to connect
        // to your bank")
        LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                .user(user)
                .clientName("Finance Tracker")
                .products(products)
                .countryCodes(Arrays.asList(CountryCode.US))
                .language("en");

        // Step 4: actually send the request to Plaid and wait
        // for the response
        Response<LinkTokenCreateResponse> response =
                plaidClient.linkTokenCreate(request).execute();

        // Step 5: if something went wrong, fail loudly with a
        // clear message instead of silently returning nothing
        if (!response.isSuccessful()) {
            throw new IOException("Plaid link token creation failed: " + response.errorBody());
        }

        // Step 6: pull just the link_token string out of the
        // response and hand it back
        return response.body().getLinkToken();
    }

    /*
     * THE TRADE-IN COUNTER
     *
     * Takes the TEMPORARY public_token (the claim ticket)
     * and trades it with Plaid for a PERMANENT access_token
     * (the real key). Then saves that key safely, linked
     * to whichever user just connected their bank.
     */
    public void exchangePublicToken(String publicToken, User currentUser) throws IOException {

        // Step 1: build the request Plaid expects — just the
        // claim ticket we want to trade in
        ItemPublicTokenExchangeRequest request =
                new ItemPublicTokenExchangeRequest().publicToken(publicToken);

        // Step 2: actually send it to Plaid and wait for
        // the permanent key back
        Response<ItemPublicTokenExchangeResponse> response =
                plaidClient.itemPublicTokenExchange(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Plaid token exchange failed: " + response.errorBody());
        }

        // Step 3: pull the permanent access_token and item_id
        // out of the response
        String accessToken = response.body().getAccessToken();
        String itemId = response.body().getItemId();

        // Step 4: save that permanent key safely in our
        // database, linked to the logged-in user
        PlaidItem plaidItem = new PlaidItem();
        plaidItem.setAccessToken(accessToken);
        plaidItem.setItemId(itemId);
        plaidItem.setUser(currentUser);
        plaidItemRepository.save(plaidItem);
    }
}