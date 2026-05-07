package SmartEntrySystem;

import java.util.Scanner;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║          🔐  SMART ENTRY SECURITY SYSTEM  — Main Runner          ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * Entry point.  Wires all components together and drives the
 * console-based menu loop.
 *
 * Components:
 *   SecurityManager  — user registry + alert log
 *   OTPService       — OTP generation & validation
 *   SessionManager   — tracks who has authenticated
 *   TimeTracker      — records entry / exit times
 *   FineCalculator   — evaluates overstays and stores fine records
 */
public class Main {

    // ══════════════════════════════════════════════════════════════════
    //  Configuration constants
    // ══════════════════════════════════════════════════════════════════
    private static final long   ALLOWED_MINUTES  = 60;    // 1 hour limit
    private static final double FINE_PER_MINUTE  = 5.00;  // ৳5 per extra minute
    private static final int    MAX_OTP_ATTEMPTS = 3;     // before user is blocked

    // ══════════════════════════════════════════════════════════════════
    //  Component instances
    // ══════════════════════════════════════════════════════════════════
    private static final SecurityManager securityManager = new SecurityManager();
    private static final OTPService      otpService      = new OTPService();
    private static final SessionManager  sessionManager  = new SessionManager();
    private static final TimeTracker     timeTracker     = new TimeTracker();
    private static final FineCalculator  fineCalculator  =
            new FineCalculator(ALLOWED_MINUTES, FINE_PER_MINUTE);
    private static final Scanner         scanner         = new Scanner(System.in);

    // ══════════════════════════════════════════════════════════════════
    //  main()
    // ══════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        printWelcomeBanner();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleRegisterUser();
                case "2" -> handleLoginWithOTP();
                case "3" -> handleEnterSystem();
                case "4" -> handleExitSystem();
                case "5" -> fineCalculator.printFineReport();
                case "6" -> securityManager.printAlerts();
                case "7" -> printSystemStatus();
                case "0" -> {
                    System.out.println("\n  👋  Goodbye! System shutting down safely.\n");
                    running = false;
                }
                default  -> System.out.println("  ❌  Invalid choice. Please enter 0-7.\n");
            }
        }
        scanner.close();
    }

    // ══════════════════════════════════════════════════════════════════
    //  Menu Handlers
    // ══════════════════════════════════════════════════════════════════

    // ── 1. Register User ──────────────────────────────────────────────
    private static void handleRegisterUser() {
        System.out.println("\n┌──────────────────────────────┐");
        System.out.println("│       REGISTER NEW USER      │");
        System.out.println("└──────────────────────────────┘");

        System.out.print("  Enter full name       : ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("  ❌  Name cannot be empty."); return; }

        System.out.print("  Enter phone number     : ");
        String phone = scanner.nextLine().trim();
        if (!phone.matches("\\d{10,15}")) {
            System.out.println("  ❌  Invalid phone number (digits only, 10-15 chars).");
            return;
        }

        System.out.print("  Enter NID number       : ");
        String nid = scanner.nextLine().trim();
        if (nid.isEmpty()) { System.out.println("  ❌  NID cannot be empty."); return; }

        securityManager.registerUser(name, phone, nid);
        System.out.println();
    }

    // ── 2. Login with OTP ─────────────────────────────────────────────
    private static void handleLoginWithOTP() {
        System.out.println("\n┌──────────────────────────────┐");
        System.out.println("│         LOGIN WITH OTP       │");
        System.out.println("└──────────────────────────────┘");

        System.out.print("  Enter your phone number : ");
        String phone = scanner.nextLine().trim();

        // Step 1 — validate identity
        User user = securityManager.validateUser(phone);
        if (user == null) return;   // alert already logged inside validateUser()

        // Step 2 — generate & display OTP (simulation)
        otpService.generateOTP(phone);

        // Step 3 — let user attempt OTP (up to MAX_OTP_ATTEMPTS)
        boolean authenticated = false;
        while (user.getFailedOtpAttempts() < MAX_OTP_ATTEMPTS) {
            System.out.print("  Enter the OTP : ");
            String entered = scanner.nextLine().trim();

            if (otpService.validateOTP(phone, entered)) {
                System.out.printf("%n  ✅  OTP verified!  Welcome, %s!%n", user.getName());
                otpService.clearOTP(phone);
                user.resetFailedAttempts();
                sessionManager.markAuthenticated(user);
                authenticated = true;
                break;
            } else {
                user.incrementFailedAttempts();
                int remaining = MAX_OTP_ATTEMPTS - user.getFailedOtpAttempts();
                if (user.isBlocked()) {
                    System.out.println("\n  🔒  Too many wrong attempts. Your account is now BLOCKED.");
                    securityManager.addAlert(new SecurityManager.BlockedUserAlert(user.getName(), phone));
                } else {
                    System.out.printf("  ❌  Wrong OTP. %d attempt(s) remaining.%n", remaining);
                }
            }
        }

        if (!authenticated && !user.isBlocked()) {
            System.out.println("  ❌  Login failed.");
        }
        System.out.println();
    }

    // ── 3. Enter System ───────────────────────────────────────────────
    private static void handleEnterSystem() {
        System.out.println("\n┌──────────────────────────────┐");
        System.out.println("│         ENTER SYSTEM         │");
        System.out.println("└──────────────────────────────┘");

        System.out.print("  Enter your phone number : ");
        String phone = scanner.nextLine().trim();

        // Must have authenticated via OTP first
        if (!sessionManager.isAuthenticated(phone)) {
            System.out.println("  ❌  You must login with OTP before entering.");
            System.out.println();
            return;
        }

        User user = sessionManager.getAuthenticatedUser(phone);

        // Must not already be inside
        if (timeTracker.isInsideSystem(phone)) {
            System.out.printf("  ⚠  %s is already inside the system.%n", user.getName());
            System.out.println();
            return;
        }

        System.out.printf("  🔓  Access granted.  Hello, %s!%n", user.getName());
        timeTracker.recordEntry(phone);
        System.out.printf("  ℹ  Allowed time : %d minutes.%n", ALLOWED_MINUTES);
        System.out.println();
    }

    // ── 4. Exit System ────────────────────────────────────────────────
    private static void handleExitSystem() {
        System.out.println("\n┌──────────────────────────────┐");
        System.out.println("│          EXIT SYSTEM         │");
        System.out.println("└──────────────────────────────┘");

        System.out.print("  Enter your phone number : ");
        String phone = scanner.nextLine().trim();

        // Retrieve user
        User user = securityManager.findUser(phone);
        if (user == null) {
            System.out.println("  ❌  User not found.");
            System.out.println();
            return;
        }

        if (!timeTracker.isInsideSystem(phone)) {
            System.out.printf("  ⚠  %s is not currently inside the system.%n", user.getName());
            System.out.println();
            return;
        }

        // Record exit and evaluate fine
        TimeTracker.Session session = timeTracker.recordExit(phone);
        if (session != null) {
            fineCalculator.evaluateSession(user, session);
        }

        // Clear authentication so user must login again next time
        sessionManager.clearSession(phone);
        System.out.printf("  👋  Goodbye, %s!  Session ended.%n", user.getName());
        System.out.println();
    }

    // ── 7. System Status ──────────────────────────────────────────────
    private static void printSystemStatus() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║               📊  SYSTEM STATUS                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.printf( "  Registered users  : %d%n", securityManager.getTotalUsers());
        System.out.printf( "  Security alerts   : %d%n", securityManager.getTotalAlerts());
        System.out.printf( "  Allowed stay      : %d minutes%n", ALLOWED_MINUTES);
        System.out.printf( "  Fine rate         : ৳%.2f / extra minute%n", FINE_PER_MINUTE);
        System.out.println();
    }

    // ══════════════════════════════════════════════════════════════════
    //  UI Helpers
    // ══════════════════════════════════════════════════════════════════

    private static void printMenu() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║       🔐  SMART ENTRY SECURITY SYSTEM            ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  1.  Register User                               ║");
        System.out.println("║  2.  Login with OTP                              ║");
        System.out.println("║  3.  Enter System                                ║");
        System.out.println("║  4.  Exit System                                 ║");
        System.out.println("║  5.  Show Fine Report                            ║");
        System.out.println("║  6.  Show Security Alerts                        ║");
        System.out.println("║  7.  System Status                               ║");
        System.out.println("║  0.  Quit                                        ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.print("  Your choice : ");
    }

    private static void printWelcomeBanner() {
        System.out.println();
        System.out.println("  ╔═══════════════════════════════════════════════╗");
        System.out.println("  ║                                               ║");
        System.out.println("  ║     🔐  SMART ENTRY SECURITY SYSTEM  🔐       ║");
        System.out.println("  ║          Core Java  |  OOP Project            ║");
        System.out.println("  ║                                               ║");
        System.out.println("  ╚═══════════════════════════════════════════════╝");
        System.out.println();
    }
}
