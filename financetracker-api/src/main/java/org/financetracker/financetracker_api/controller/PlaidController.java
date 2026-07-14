package org.financetracker.financetracker_api.controller;

import org.financetracker.financetracker_api.model.Transaction;
import org.financetracker.financetracker_api.model.User;
import org.financetracker.financetracker_api.service.CurrentUserService;
import org.financetracker.financetracker_api.service.PlaidService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * PlaidController — THE FRONT DOOR for all Plaid requests
 *
 * Three endpoints:
 * 1. POST /api/plaid/link-token       — prepares the Plaid popup ticket
 * 2. POST /api/plaid/exchange-token   — trades the ticket for a permanent key
 * 3. POST /api/plaid/sync-transactions — NEW: pulls + saves bank transactions
 */
@RestController
@RequestMapping("/api/plaid")
public class PlaidController {

    // the worker class that handles all Plaid communication
    private final PlaidService plaidService;

    // reads who is currently logged in from the JWT token
    private final CurrentUserService currentUserService;

    // Spring automatically passes both dependencies in here
    public PlaidController(PlaidService plaidService, CurrentUserService currentUserService) {
        this.plaidService = plaidService;
        this.currentUserService = currentUserService;
    }

    /*
     * POST /api/plaid/link-token
     *
     * React calls this FIRST, before the Plaid popup opens.
     * Returns a short-lived ticket React uses to open the popup.
     *
     * Example response:
     * { "linkToken": "link-sandbox-abc123..." }
     */
    @PostMapping("/link-token")
    public Map<String, String> createLinkToken() throws Exception {

        // find out who is logged in right now
        Long userId = currentUserService.getCurrentUserId();

        // Plaid expects a String user id, so we convert our Long
        String linkToken = plaidService.createLinkToken(userId.toString());

        // send the token back to React as JSON
        return Map.of("linkToken", linkToken);
    }

    /*
     * POST /api/plaid/exchange-token
     *
     * React calls this AFTER the user picks their bank in the popup.
     * Trades the temporary public_token for a permanent access_token
     * and saves it to our database.
     *
     * Example request body:
     * { "publicToken": "public-sandbox-abc123..." }
     */
    @PostMapping("/exchange-token")
    public void exchangeToken(@RequestBody Map<String, String> body) throws Exception {

        // pull the public_token out of the request body React sent
        String publicToken = body.get("publicToken");

        // get the full User object of whoever is logged in
        var currentUser = currentUserService.getCurrentUser();

        // trade the temporary token for a permanent one and save it
        plaidService.exchangePublicToken(publicToken, currentUser);
    }

    /*
     * NEW — POST /api/plaid/sync-transactions
     *
     * React calls this after the bank is connected to pull
     * real transactions from Plaid and save them to our DB.
     *
     * Steps:
     * 1. Find out who is logged in
     * 2. Ask PlaidService to fetch, convert, and save their transactions
     * 3. Send the saved transactions back to React as JSON
     *
     * Example response:
     * [
     *   { "id": 1, "amount": 12.50, "category": "Food and Drink",
     *     "description": "Starbucks", "date": "2026-07-14" },
     *   { "id": 2, "amount": 45.00, "category": "Travel",
     *     "description": "Uber", "date": "2026-07-13" }
     * ]
     */
    @PostMapping("/sync-transactions")
    public List<Transaction> syncTransactions() throws Exception {

        // get the full User object of whoever is logged in
        // we need the full object (not just the id) because
        // PlaidService needs to stamp each transaction with this user
        User currentUser = currentUserService.getCurrentUser();

        // fetch from Plaid, convert to our format, save to DB,
        // and return the saved list — all in one call
        return plaidService.syncTransactions(currentUser);
    }
}