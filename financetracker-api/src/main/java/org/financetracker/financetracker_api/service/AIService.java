package org.financetracker.financetracker_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
 * AIService — THE BRAIN
 *
 * This service has ONE job:
 * take a transaction description like "TIM HORTONS #4521"
 * and ask Claude to categorize it for us.
 *
 * Claude responds with a single category like "Food and Drink"
 * which we then save on the transaction automatically.
 *
 * This is what makes your app genuinely different from a
 * basic finance tracker — AI-powered categorization that
 * gets smarter the more transactions it sees.
 */
@Service
public class AIService {

    // reads ANTHROPIC_API_KEY from Railway environment variables
    // same pattern as PLAID_CLIENT_ID and ENCRYPTION_KEY
    @Value("${ANTHROPIC_API_KEY}")
    private String anthropicApiKey;

    // the Claude model we want to use — Sonnet is fast and smart
    private static final String MODEL = "claude-sonnet-4-6";

    // Anthropic's API endpoint for sending messages to Claude
    private static final String API_URL = "https://api.anthropic.com/v1/messages";

    // the categories your app supports — Claude will pick one of these
    // these match the categories Plaid uses so they stay consistent
    private static final String CATEGORIES =
            "Food and Drink, Travel, Transfer, Payment, Recreation, " +
                    "Shops, Entertainment, Healthcare, Education, Utilities, " +
                    "Personal Care, Uncategorized";

    /*
     * Asks Claude: "what category does this transaction belong to?"
     *
     * description — the merchant name or transaction description
     *               e.g. "TIM HORTONS #4521" or "NETFLIX.COM"
     *
     * Returns a single category string e.g. "Food and Drink"
     * Falls back to "Uncategorized" if anything goes wrong
     */
    public String categorize(String description) {
        try {
            // build the prompt we send to Claude
            // we give it the categories to choose from so it
            // doesn't invent new ones that don't match your system
            String prompt = "Categorize this bank transaction into exactly one of these categories: " +
                    CATEGORIES + ". " +
                    "Transaction: \"" + description + "\". " +
                    "Reply with ONLY the category name, nothing else. No explanation, no punctuation.";

            // build the JSON body that Anthropic's API expects
            // this is the same format as the Claude API docs
            String requestBody = """
                    {
                        "model": "%s",
                        "max_tokens": 50,
                        "messages": [
                            {
                                "role": "user",
                                "content": "%s"
                            }
                        ]
                    }
                    """.formatted(MODEL, prompt.replace("\"", "\\\""));

            // build the HTTP request to send to Anthropic
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    // x-api-key = how Anthropic authenticates your request
                    .header("x-api-key", anthropicApiKey)
                    // anthropic-version = tells Anthropic which API version to use
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // send the request and wait for Claude's response
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // parse the category out of Claude's JSON response
            // Claude's response looks like:
            // {"content": [{"type": "text", "text": "Food and Drink"}]}
            String responseBody = response.body();
            return extractCategory(responseBody);

        } catch (Exception e) {
            // if anything goes wrong, fall back to Uncategorized
            // we never want AI errors to crash the transaction sync
            System.out.println("AI categorization failed for: " + description + " — " + e.getMessage());
            return "Uncategorized";
        }
    }

    /*
     * Pulls just the category text out of Claude's JSON response
     *
     * Claude returns something like:
     * {"content": [{"type": "text", "text": "Food and Drink"}], ...}
     *
     * We extract "Food and Drink" from that structure
     */
    private String extractCategory(String responseBody) {
        try {
            // find the "text" field in Claude's response
            // simple string parsing — no need for a full JSON library
            int textIndex = responseBody.indexOf("\"text\":\"");
            if (textIndex == -1) {
                return "Uncategorized";
            }

            // move past the "text":" prefix to get to the actual value
            int start = textIndex + 8;

            // find the closing quote of the text value
            int end = responseBody.indexOf("\"", start);
            if (end == -1) {
                return "Uncategorized";
            }

            // extract just the category text and trim any whitespace
            String category = responseBody.substring(start, end).trim();

            // return the category if it's not empty, otherwise Uncategorized
            return category.isEmpty() ? "Uncategorized" : category;

        } catch (Exception e) {
            return "Uncategorized";
        }
    }
}