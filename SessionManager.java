package SmartEntrySystem;

import java.util.HashMap;

/**
 * Tracks which users have successfully logged in (completed OTP) and
 * are authorised to enter/exit the physical space.
 *
 * Separates "authentication" (OTP success) from "presence" (TimeTracker)
 * so each concern has a single owner.
 */
public class SessionManager {

    // ── Fields ────────────────────────────────────────────────────────────────────
    /** phoneNumber → User (authenticated users awaiting / inside entry) */
    private final HashMap<String, User> authenticatedUsers;

    // ── Constructor ───────────────────────────────────────────────────────────────
    public SessionManager() {
        this.authenticatedUsers = new HashMap<>();
    }

    // ── Public Methods ────────────────────────────────────────────────────────────

    /** Mark a user as authenticated after successful OTP. */
    public void markAuthenticated(User user) {
        authenticatedUsers.put(user.getPhoneNumber(), user);
    }

    /** Returns true if the given phone number has a live authenticated session. */
    public boolean isAuthenticated(String phoneNumber) {
        return authenticatedUsers.containsKey(phoneNumber);
    }

    /** Remove a user's authenticated session (after they exit the building). */
    public void clearSession(String phoneNumber) {
        authenticatedUsers.remove(phoneNumber);
    }

    /** Returns the authenticated User object, or null. */
    public User getAuthenticatedUser(String phoneNumber) {
        return authenticatedUsers.get(phoneNumber);
    }
}
