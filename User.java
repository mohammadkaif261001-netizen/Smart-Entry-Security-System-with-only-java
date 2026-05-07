package SmartEntrySystem;

/**
 * Represents a registered user in the Smart Entry Security System.
 * Demonstrates ENCAPSULATION by keeping fields private with public getters/setters.
 */
public class User {

    // ── Private Fields (Encapsulation) ──────────────────────────────────────────
    private String name;
    private String phoneNumber;
    private String nidNumber;
    private boolean isBlocked;
    private int failedOtpAttempts;

    // ── Constructor ──────────────────────────────────────────────────────────────
    public User(String name, String phoneNumber, String nidNumber) {
        this.name             = name;
        this.phoneNumber      = phoneNumber;
        this.nidNumber        = nidNumber;
        this.isBlocked        = false;
        this.failedOtpAttempts = 0;
    }

    // ── Getters ──────────────────────────────────────────────────────────────────
    public String getName()        { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getNidNumber()   { return nidNumber; }
    public boolean isBlocked()     { return isBlocked; }
    public int getFailedOtpAttempts() { return failedOtpAttempts; }

    // ── Setters ──────────────────────────────────────────────────────────────────
    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }

    // ── Business Methods ─────────────────────────────────────────────────────────

    /** Increment wrong-OTP counter; auto-block after 3 failures. */
    public void incrementFailedAttempts() {
        failedOtpAttempts++;
        if (failedOtpAttempts >= 3) {
            isBlocked = true;
        }
    }

    /** Reset OTP failure counter (called after successful login). */
    public void resetFailedAttempts() {
        failedOtpAttempts = 0;
    }

    @Override
    public String toString() {
        return String.format("User{name='%s', phone='%s', nid='%s', blocked=%s}",
                name, phoneNumber, nidNumber, isBlocked);
    }
}
