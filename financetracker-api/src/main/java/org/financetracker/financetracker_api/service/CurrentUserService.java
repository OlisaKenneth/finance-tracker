package org.financetracker.financetracker_api.service;

import org.financetracker.financetracker_api.model.User;
import org.financetracker.financetracker_api.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/*
 * CurrentUserService — THE ID CHECKER
 *
 * Every controller that needs to know "who is making this
 * request right now?" calls this class instead of repeating
 * the same lookup logic in four different places.
 *
 * How it knows who's logged in:
 * 1. JwtAuthFilter already ran on this request (before the
 *    controller ever sees it) and stored the logged-in user's
 *    EMAIL inside Spring's SecurityContextHolder.
 * 2. This class reads that email back out.
 * 3. It looks up the matching User row in the database to
 *    get their numeric ID — which is what we actually need
 *    to filter budgets/transactions/goals by ownership.
 */
@Service
public class CurrentUserService {

    private UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * Returns the User object for whoever is currently logged in.
     *
     * SecurityContextHolder.getContext().getAuthentication()
     *   → gives us the "authentication ticket" JwtAuthFilter
     *     created earlier in this same request
     *
     * .getName()
     *   → pulls the email back out of that ticket
     *     (remember: JwtAuthFilter stored the email as the
     *     "principal" when it built the UsernamePasswordAuthenticationToken)
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found in database"));
    }

    /*
     * Convenience method — most of the time we only need the ID,
     * not the whole User object, so this saves writing
     * .getCurrentUser().getId() everywhere.
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}