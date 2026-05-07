package SmartEntrySystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Central security authority: manages user registration,
 * validates access, and maintains alert logs.
 *
 * Demonstrates INHERITANCE via the inner AlertRecord hierarchy:
 *   AlertRecord  (base)
 *       ├── UnauthorizedAlert
 *       └── BlockedUserAlert
 */
public class SecurityManager {

    // ── Inner Class Hierarchy (Inheritance) ───────────────────────────────────────

    /** Base class for all security alerts. */
    public static class AlertRecord {
        protected final String timestamp;
        protected final String message;

        public AlertRecord(String message) {
            this.message   = message;
            this.timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        public String getTimestamp() { return timestamp; }
        public String getMessage()   { return message; }

        @Override
        public String toString() {
            return String.format("[%s]  %s", timestamp, message);
        }
    }

    /** Alert for completely unknown users. */
    public static class UnauthorizedAlert extends AlertRecord {
        public UnauthorizedAlert(String phoneNumber) {
            super("🚨 ALERT: Unauthorized Access — unknown phone: " + phoneNumber);
        }
    }

    /** Alert for users who are blocked due to repeated OTP failures. */
    public static class BlockedUserAlert extends AlertRecord {
        public BlockedUserAlert(String name, String phoneNumber) {
            super("🔒 ALERT: Blocked User attempted entry — " + name + " (" + phoneNumber + ")");
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────────
    private final HashMap<String, User>   userRegistry;   // phoneNumber → User
    private final ArrayList<AlertRecord>  alertLog;

    // ── Constructor ───────────────────────────────────────────────────────────────
    public SecurityManager() {
        this.userRegistry = new HashMap<>();
        this.alertLog     = new ArrayList<>();
    }

    // ── User Registration ─────────────────────────────────────────────────────────

    /**
     * Registers a new user.
     * Returns false if phone number is already registered.
     */
    public boolean registerUser(String name, String phoneNumber, String nidNumber) {
        if (userRegistry.containsKey(phoneNumber)) {
            System.out.println("⚠  Phone number already registered.");
            return false;
        }
        User user = new User(name, phoneNumber, nidNumber);
        userRegistry.put(phoneNumber, user);
        System.out.printf("✅  User '%s' registered successfully!%n", name);
        return true;
    }

    // ── Access Validation ─────────────────────────────────────────────────────────

    /**
     * Validates whether a user can proceed with OTP login.
     * Logs alerts for unknown or blocked users.
     * Returns the User if valid, null otherwise.
     */
    public User validateUser(String phoneNumber) {
        User user = userRegistry.get(phoneNumber);

        if (user == null) {
            // Unknown user → log unauthorized alert
            String alertMsg = "Unknown phone number: " + phoneNumber;
            System.out.println("\n🚨  ALERT: Unauthorized Access Attempt!");
            System.out.println("    " + alertMsg);
            alertLog.add(new UnauthorizedAlert(phoneNumber));
            return null;
        }

        if (user.isBlocked()) {
            // Blocked user → log blocked alert
            System.out.printf(
                "\n🔒  ACCESS DENIED: User '%s' is blocked due to too many wrong OTP attempts.%n",
                user.getName()
            );
            alertLog.add(new BlockedUserAlert(user.getName(), phoneNumber));
            return null;
        }

        return user;
    }

    /**
     * Retrieves a user by phone number without any side effects (no alert logging).
     * Returns null if not found.
     */
    public User findUser(String phoneNumber) {
        return userRegistry.get(phoneNumber);
    }

    // ── Alert Management ──────────────────────────────────────────────────────────

    /** Manually add an alert to the log. */
    public void addAlert(AlertRecord alert) {
        alertLog.add(alert);
    }

    /** Print all security alerts. */
    public void printAlerts() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                  🔔  SECURITY ALERT LOG                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        if (alertLog.isEmpty()) {
            System.out.println("  No alerts recorded.");
        } else {
            for (int i = 0; i < alertLog.size(); i++) {
                System.out.printf("  [%d] %s%n", i + 1, alertLog.get(i));
            }
        }
        System.out.println();
    }

    /** Returns total number of registered users. */
    public int getTotalUsers() {
        return userRegistry.size();
    }

    /** Returns total number of alerts logged. */
    public int getTotalAlerts() {
        return alertLog.size();
    }
}
