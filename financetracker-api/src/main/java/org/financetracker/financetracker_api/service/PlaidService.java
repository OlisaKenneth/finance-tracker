package org.financetracker.financetracker_api.service;

import com.plaid.client.request.PlaidApi;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.Products;
import com.plaid.client.model.CountryCode;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/*
 * PlaidService — THE WORKER
 *
 * This class's ONE job right now: ask Plaid for a
 * "link_token"
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

    public PlaidService(PlaidApi plaidClient) {
        this.plaidClient = plaidClient;
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
}