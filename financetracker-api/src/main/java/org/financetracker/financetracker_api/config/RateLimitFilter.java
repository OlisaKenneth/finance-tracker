package org.financetracker.financetracker_api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * RateLimitFilter — THE BOUNCER
 *
 * This filter sits in front of your API and checks:
 * "has this IP address made too many requests recently?"
 *
 * If yes  → block the request, return 429 Too Many Requests
 * If no   → let the request through normally
 *
 * We only rate limit the login endpoint because that's where
 * brute force attacks happen — someone trying thousands of
 * passwords to guess yours.
 *
 * How the bucket works:
 * - Each IP gets its own bucket with 5 tokens
 * - Every login attempt uses 1 token
 * - The bucket refills 5 tokens every 1 minute
 * - When the bucket is empty → blocked until it refills
 *
 * So: max 5 login attempts per minute per IP address
 *
 * OncePerRequestFilter = Spring guarantees this filter
 * only runs once per request, never twice
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    /*
     * A map that stores one bucket per IP address
     * ConcurrentHashMap = thread-safe map for multiple
     * simultaneous requests from different users
     *
     * Key   = IP address e.g. "192.168.1.1"
     * Value = that IP's token bucket
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /*
     * Creates a new bucket for an IP address that we've
     * never seen before
     *
     * Settings:
     * - capacity: 5 tokens maximum
     * - refill: 5 tokens per 1 minute (greedy = refill gradually)
     *
     * So a new user gets 5 attempts immediately, then 5 per minute
     */
    private Bucket createNewBucket() {
        // Refill.greedy = tokens are added back gradually over the minute
        // not all at once at the end of the minute
        Refill refill = Refill.greedy(5, Duration.ofMinutes(1));

        // Bandwidth = the rules for this bucket
        // classic() = standard bucket with a fixed capacity
        Bandwidth limit = Bandwidth.classic(5, refill);

        // build and return the bucket with our rules
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /*
     * This method runs on EVERY incoming request
     * We check if it's a login request and if so,
     * apply rate limiting
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // only rate limit the login endpoint
        // all other endpoints pass through freely
        if (!request.getRequestURI().equals("/api/auth/login")) {
            // not a login request — let it through normally
            filterChain.doFilter(request, response);
            return;
        }

        // get the IP address of whoever is making this request
        // getRemoteAddr() returns the IP e.g. "192.168.1.1"
        String ipAddress = request.getRemoteAddr();

        // get this IP's bucket, or create a new one if first time
        // computeIfAbsent = "get existing, or create new if missing"
        Bucket bucket = buckets.computeIfAbsent(ipAddress, k -> createNewBucket());

        // tryConsume(1) = "try to take 1 token from the bucket"
        // returns true  = token available, request allowed
        // returns false = bucket empty, request blocked
        if (bucket.tryConsume(1)) {
            // token available — let the request through
            filterChain.doFilter(request, response);
        } else {
            // bucket empty — block this request
            // 429 = "Too Many Requests" — the standard HTTP code for rate limiting
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Too many login attempts. Please wait 1 minute and try again.\"}"
            );
        }
    }
}