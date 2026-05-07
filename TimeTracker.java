package SmartEntrySystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

/**
 * Tracks entry and exit times for each user session.
 * Uses LocalDateTime from java.time package.
 */
public class TimeTracker {

    // ── Inner Class: Session Record ───────────────────────────────────────────────
    /**
     * Holds the entry time and (optionally) exit time for one visit.
     */
    public static class Session {
        private final LocalDateTime entryTime;
        private LocalDateTime exitTime;

        public Session(LocalDateTime entryTime) {
            this.entryTime = entryTime;
            this.exitTime  = null;
        }

        public LocalDateTime getEntryTime() { return entryTime; }
        public LocalDateTime getExitTime()  { return exitTime; }

        public void setExitTime(LocalDateTime exitTime) {
            this.exitTime = exitTime;
        }

        /** Returns total minutes spent inside; -1 if session not yet closed. */
        public long getMinutesSpent() {
            if (exitTime == null) return -1;
            return ChronoUnit.MINUTES.between(entryTime, exitTime);
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────────
    private final HashMap<String, Session> activeSessions;   // phoneNumber → Session
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");

    // ── Constructor ───────────────────────────────────────────────────────────────
    public TimeTracker() {
        this.activeSessions = new HashMap<>();
    }

    // ── Public Methods ────────────────────────────────────────────────────────────

    /**
     * Records the entry time for a user.
     * Returns false if the user already has an active session.
     */
    public boolean recordEntry(String phoneNumber) {
        if (activeSessions.containsKey(phoneNumber)) {
            System.out.println("⚠  User already has an active session inside the system.");
            return false;
        }
        Session session = new Session(LocalDateTime.now());
        activeSessions.put(phoneNumber, session);
        System.out.println("✅  Entry recorded at: " + session.getEntryTime().format(formatter));
        return true;
    }

    /**
     * Records the exit time for a user and returns the finished Session.
     * Returns null if no active session found.
     */
    public Session recordExit(String phoneNumber) {
        Session session = activeSessions.get(phoneNumber);
        if (session == null) {
            System.out.println("⚠  No active session found for this user.");
            return null;
        }
        session.setExitTime(LocalDateTime.now());
        activeSessions.remove(phoneNumber);

        System.out.println("🚪  Exit recorded at : " + session.getExitTime().format(formatter));
        System.out.printf( "⏱   Time spent       : %d minute(s)%n", session.getMinutesSpent());
        return session;
    }

    /** Returns true if the user is currently inside the system. */
    public boolean isInsideSystem(String phoneNumber) {
        return activeSessions.containsKey(phoneNumber);
    }

    /** Returns the active session for a user, or null if none. */
    public Session getActiveSession(String phoneNumber) {
        return activeSessions.get(phoneNumber);
    }
}
