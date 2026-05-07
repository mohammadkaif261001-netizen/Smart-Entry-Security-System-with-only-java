package SmartEntrySystem;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Calculates and stores fine records for users who exceed the allowed time limit.
 */
public class FineCalculator {

    // ── Inner Class: Fine Record ──────────────────────────────────────────────────
    public static class FineRecord {
        private final String userName;
        private final String phoneNumber;
        private final long   minutesSpent;
        private final long   allowedMinutes;
        private final long   extraMinutes;
        private final double fineAmount;
        private final String exitTime;

        public FineRecord(String userName, String phoneNumber,
                          long minutesSpent, long allowedMinutes,
                          double fineAmount, String exitTime) {
            this.userName       = userName;
            this.phoneNumber    = phoneNumber;
            this.minutesSpent   = minutesSpent;
            this.allowedMinutes = allowedMinutes;
            this.extraMinutes   = minutesSpent - allowedMinutes;
            this.fineAmount     = fineAmount;
            this.exitTime       = exitTime;
        }

        // Getters
        public String getUserName()       { return userName; }
        public String getPhoneNumber()    { return phoneNumber; }
        public long   getMinutesSpent()   { return minutesSpent; }
        public long   getAllowedMinutes()  { return allowedMinutes; }
        public long   getExtraMinutes()   { return extraMinutes; }
        public double getFineAmount()     { return fineAmount; }
        public String getExitTime()       { return exitTime; }

        @Override
        public String toString() {
            return String.format(
                "  %-20s | Phone: %-14s | Allowed: %3d min | Spent: %3d min | Extra: %3d min | Fine: ৳%.2f | Exit: %s",
                userName, phoneNumber, allowedMinutes, minutesSpent, extraMinutes, fineAmount, exitTime
            );
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────────
    private final long   allowedMinutes;   // maximum allowed time inside
    private final double finePerMinute;    // fine amount per extra minute (in BDT ৳)
    private final ArrayList<FineRecord> fineRecords;
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── Constructor ───────────────────────────────────────────────────────────────
    public FineCalculator(long allowedMinutes, double finePerMinute) {
        this.allowedMinutes = allowedMinutes;
        this.finePerMinute  = finePerMinute;
        this.fineRecords    = new ArrayList<>();
    }

    // ── Public Methods ────────────────────────────────────────────────────────────

    /**
     * Evaluates a completed session and calculates a fine if time was exceeded.
     * Adds a FineRecord to history if a fine applies.
     */
    public void evaluateSession(User user, TimeTracker.Session session) {
        long minutesSpent = session.getMinutesSpent();
        String exitTimeStr = session.getExitTime().format(formatter);

        if (minutesSpent <= allowedMinutes) {
            System.out.println("✅  Time within limit. No fine applied.");
            System.out.printf( "    (Used %d of %d allowed minutes)%n", minutesSpent, allowedMinutes);
            return;
        }

        long   extraMinutes = minutesSpent - allowedMinutes;
        double fineAmount   = extraMinutes * finePerMinute;

        System.out.println("\n⚠  ══════════════════ FINE NOTICE ══════════════════");
        System.out.printf( "    User          : %s%n", user.getName());
        System.out.printf( "    Allowed time  : %d minutes%n", allowedMinutes);
        System.out.printf( "    Time spent    : %d minutes%n", minutesSpent);
        System.out.printf( "    Extra time    : %d minutes%n", extraMinutes);
        System.out.printf( "    Fine rate     : ৳%.2f per minute%n", finePerMinute);
        System.out.printf( "    FINE AMOUNT   : ৳%.2f%n", fineAmount);
        System.out.println("    ══════════════════════════════════════════════════\n");

        FineRecord record = new FineRecord(
                user.getName(), user.getPhoneNumber(),
                minutesSpent, allowedMinutes,
                fineAmount, exitTimeStr
        );
        fineRecords.add(record);
    }

    /** Prints all fine records collected so far. */
    public void printFineReport() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    📋  FINE REPORT                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        if (fineRecords.isEmpty()) {
            System.out.println("  No fines recorded yet.");
        } else {
            double total = 0;
            for (int i = 0; i < fineRecords.size(); i++) {
                System.out.printf("  [%d] %s%n", i + 1, fineRecords.get(i));
                total += fineRecords.get(i).getFineAmount();
            }
            System.out.println("  ──────────────────────────────────────────────────────────────");
            System.out.printf( "  Total fines collected : ৳%.2f%n", total);
        }
        System.out.println();
    }

    // Getters
    public long   getAllowedMinutes() { return allowedMinutes; }
    public double getFinePerMinute()  { return finePerMinute; }
}
