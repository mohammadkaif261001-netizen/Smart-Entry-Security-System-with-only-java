package SmartEntrySystem;

import java.util.HashMap;
import java.util.Random;

/**
 * Handles OTP generation and validation for each user session.
 * Each phone number maps to its currently active OTP.
 */
public class OTPService {

    // ── Fields ───────────────────────────────────────────────────────────────────
    private final HashMap<String, String> otpStore;   // phoneNumber → OTP
    private final Random random;

    // ── Constructor ──────────────────────────────────────────────────────────────
    public OTPService() {
        this.otpStore = new HashMap<>();
        this.random   = new Random();
    }

    // ── Public Methods ───────────────────────────────────────────────────────────

    /**
     * Generates a fresh 6-digit OTP for the given phone number,
     * stores it internally, prints it to console (simulation), and returns it.
     */
    public String generateOTP(String phoneNumber) {
        // Build a zero-padded 6-digit number  (e.g. "047823")
        int otp = 100000 + random.nextInt(900000);   // range [100000, 999999]
        String otpString = String.valueOf(otp);

        otpStore.put(phoneNumber, otpString);

        // ── Console simulation of "SMS delivery" ──────────────────────────────
        System.out.println("\n┌─────────────────────────────────────────┐");
        System.out.println("│         📲  OTP SIMULATION (SMS)        │");
        System.out.println("│                                         │");
        System.out.printf( "│   Your OTP is :  %-6s                 │%n", otpString);
        System.out.println("│   Valid for this session only           │");
        System.out.println("└─────────────────────────────────────────┘\n");

        return otpString;
    }

    /**
     * Validates the OTP entered by the user.
     * Returns true if the OTP matches the stored one.
     */
    public boolean validateOTP(String phoneNumber, String enteredOTP) {
        String stored = otpStore.get(phoneNumber);
        if (stored == null) return false;
        return stored.equals(enteredOTP.trim());
    }

    /**
     * Clears the OTP after a successful login (one-time use).
     */
    public void clearOTP(String phoneNumber) {
        otpStore.remove(phoneNumber);
    }
}
