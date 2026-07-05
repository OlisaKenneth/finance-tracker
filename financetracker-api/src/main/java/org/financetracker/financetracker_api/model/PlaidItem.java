package org.financetracker.financetracker_api.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * PlaidItem — THE SAFE, LABELED KEY HOLDER
 *
 * Every time a user successfully connects a bank through
 * Plaid, we get back a PERMANENT key (access_token). This
 * class's ONE job is to store that key safely in our
 * database, linked to the user who owns it.
 *
 * Same @ManyToOne pattern as Budget, Transaction, and
 * SavingsGoal — "MANY plaid items can belong to ONE user"
 * (in practice, usually just one per user for now, but the
 * pattern allows someone to connect multiple banks later).
 */
@Entity
@Table(name = "plaid_items")
public class PlaidItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The PERMANENT key Plaid gave us — this is the
    // powerful one that must NEVER be sent to the frontend.
    // That's why we mark it @JsonIgnore below, same reason
    // we hide the User object on Budget/Transaction.
    private String accessToken;

    // Plaid also gives us an "itemId" — think of it as a
    // receipt number for THIS specific bank connection.
    // Useful later if we ever need to disconnect this bank.
    private String itemId;

    // Which bank this connection is for (e.g. "TD Bank") —
    // just a friendly label to show the user later.
    private String institutionName;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}