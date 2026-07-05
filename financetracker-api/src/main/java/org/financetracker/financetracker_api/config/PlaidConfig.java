package org.financetracker.financetracker_api.config;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/*
 * PlaidConfig — THE KEY-CUTTING MACHINE
 *
 * This file has ONE job: read our two secret Plaid keys,
 * and build ONE reusable "PlaidApi" object.
 *
 * Every other file in our app that needs to talk to Plaid
 * will just ask Spring for this PlaidApi object — nobody
 * rebuilds the connection themselves. Same idea as how
 * SecurityConfig.java builds ONE security setup that the
 * whole app shares.
 */
@Configuration // tells Spring Boot "this class builds reusable setup objects"
public class PlaidConfig {

    /*
     * @Value reads a single value out of our environment
     * variables (the ones we added in Railway).
     *
     * "${PLAID_CLIENT_ID}" means: go find something named
     * PLAID_CLIENT_ID and put its value into this variable.
     *
     * This is the SAME idea as System.getenv("PLAID_CLIENT_ID")
     * — just Spring Boot's shorthand way of writing it.
     */
    @Value("${PLAID_CLIENT_ID}")
    private String plaidClientId;

    @Value("${PLAID_SECRET}")
    private String plaidSecret;

    /*
     * @Bean means: "Spring, build this ONE time, and hand
     * it out to anyone who asks for a PlaidApi object."
     *
     * Think of this method as the actual key-cutting
     * machine turning on and doing its job.
     */
    @Bean
    public PlaidApi plaidClient() {

        // Step 1: put both secret keys into a small labeled box
        // (Plaid's library expects them in this exact shape)
        HashMap<String, String> apiKeys = new HashMap<>();
        apiKeys.put("clientId", plaidClientId);
        apiKeys.put("secret", plaidSecret);

        // Step 2: build Plaid's own internal connector using
        // that labeled box of keys
        ApiClient apiClient = new ApiClient(apiKeys);

        // Step 3: tell it "we're using the PRACTICE/FAKE bank
        // environment" — not real banks yet. This matches the
        // Sandbox secret you got from the Plaid dashboard.
        apiClient.setPlaidAdapter(ApiClient.Sandbox);

        // Step 4: hand back the finished, ready-to-use
        // PlaidApi object — this is the thing every other
        // file will borrow going forward
        return apiClient.createService(PlaidApi.class);
    }
}