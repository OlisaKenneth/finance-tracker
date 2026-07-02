package org.financetracker.financetracker_api.dto;

/*
 * LoginResponse — THE LABELED ENVELOPE
 *
 * Lives in the "dto" package, not "model" — because it doesn't
 * represent something stored in the database. It only exists to
 * shape what we send back over the API after a successful login.
 *
 * Before: we returned a raw String (just the token, no label)
 * After:  we return this object, which Spring automatically
 *         converts into: { "token": "eyJhbGc..." }
 *
 * Why bother wrapping it?
 * - The frontend can now safely call res.json() and read data.token
 * - Later, if we want to send back more info (like the user's name,
 *   or when the token expires), we just add a field here —
 *   nothing on the frontend breaks
 */
public class LoginResponse {

    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}