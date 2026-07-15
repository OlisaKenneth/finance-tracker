package org.financetracker.financetracker_api.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Convert;
import org.financetracker.financetracker_api.config.AccessTokenConverter;

/*
 * PlaidItem — THE SAFE, LABELED KEY HOLDER
 *
 * Every time a user successfully connects a bank through
 * Plaid, we get back a PERMANENT key (access_token). This
 * class's ONE job is to store that key safely in our
 * database, linked to the user who owns it.
 */
@Entity
@Table(name = "plaid_items")
public class PlaidItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The PERMANENT key Plaid gave us — encrypted before saving
    // @Convert tells JPA to run AccessTokenConverter automatically
    // on every read and write — encryption is invisible to the rest of the code
    @Convert(converter = AccessTokenConverter.class)
    private String accessToken;

    // Plaid's receipt number for this specific bank connection
    // useful later if we need to disconnect this bank
    private String itemId;

    // which bank this connection is for e.g. "TD Bank"
    private String institutionName;

    /*
     * NEW — THE SYNC FLAG
     *
     * tracks whether we have already pulled transactions
     * for this specific bank connection
     *
     * false = never synced yet, go ahead and pull transactions
     * true  = already synced, skip — don't pull again
     *
     * This solves the sandbox duplicate problem:
     * every time you click "Connect my bank" in sandbox,
     * Plaid creates a brand new item with brand new transaction IDs
     * so our ID-based duplicate check thinks they are all new.
     *
     * By marking each PlaidItem as synced after the first pull,
     * we guarantee transactions are only pulled once per connection.
     * In production with a real bank, this works correctly because
     * users connect their bank once and never need to connect again.
     */
    private boolean synced = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    // returns true if transactions have already been pulled for this connection
    public boolean isSynced() {
        return synced;
    }

    // sets the synced flag — called after successfully pulling transactions
    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}