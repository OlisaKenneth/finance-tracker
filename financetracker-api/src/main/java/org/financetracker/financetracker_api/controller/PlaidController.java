package org.financetracker.financetracker_api.controller;

import org.financetracker.financetracker_api.service.CurrentUserService;
import org.financetracker.financetracker_api.service.PlaidService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
 * PlaidController — THE FRONT DOOR for all Plaid requests
 *
 * Right now it only has ONE endpoint:
 * POST /api/plaid/link-token
 *
 * This is what React calls FIRST, before the Plaid popup
 * can even open. Same pattern as every other controller
 * you've built — receive the request, ask a service to
 * do the real work, hand back the result.
 */
@RestController
@RequestMapping("/api/plaid")
public class PlaidController {

    private final PlaidService plaidService;
    private final CurrentUserService currentUserService;

    public PlaidController(PlaidService plaidService, CurrentUserService currentUserService) {
        this.plaidService = plaidService;
        this.currentUserService = currentUserService;
    }

    /*
     * Handles POST requests to /api/plaid/link-token
     *
     * Steps:
     * 1. Figure out WHO is logged in (same CurrentUserService
     *    trick we already use for budgets/transactions)
     * 2. Ask PlaidService to create a link_token for that user
     * 3. Send the link_token back to React as JSON
     *
     * Example response:
     * { "linkToken": "link-sandbox-abc123..." }
     */
    @PostMapping("/link-token")
    public Map<String, String> createLinkToken() throws Exception {
        Long userId = currentUserService.getCurrentUserId();

        // Plaid expects a String, so we convert our numeric
        // user id into text
        String linkToken = plaidService.createLinkToken(userId.toString());

        return Map.of("linkToken", linkToken);
    }

    /*
     * Handles POST requests to /api/plaid/exchange-token
     *
     * Steps:
     * 1. Figure out WHO is logged in
     * 2. Take the public_token React just sent us
     * 3. Ask PlaidService to trade it for a permanent key
     *    and save it
     *
     * Example request body:
     * { "publicToken": "public-sandbox-abc123..." }
     */
    @PostMapping("/exchange-token")
    public void exchangeToken(@RequestBody Map<String, String> body) throws Exception {
        String publicToken = body.get("publicToken");
        var currentUser = currentUserService.getCurrentUser();

        plaidService.exchangePublicToken(publicToken, currentUser);
    }
}