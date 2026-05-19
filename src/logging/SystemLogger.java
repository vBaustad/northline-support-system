package logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Trådsikker systemlogger.
 *
 * - Skriver alle loggoppføringer til én fil (system.log som standard).
 * - Bruker synkroniserte metoder for å sikre at hver loggoppføring skrives atomisk.
 * - Kan brukes fra alle tråder (f.eks. serverens klienthåndterere).
 *
 * Typisk bruk:
 *   SystemLogger logger = SystemLogger.getInstance();
 *   logger.info("CREATE", "requestId=42 registrar=reg01 desc=\"Can't log in\"");
 */
public class SystemLogger {

    private static final String DEFAULT_LOG_FILE = "system.log";

    // Singleton-instans
    private static final SystemLogger INSTANCE = new SystemLogger(DEFAULT_LOG_FILE);

    private final PrintWriter writer;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_INSTANT;

    /**
     * Hent global singleton-instans av loggeren.
     */
    public static SystemLogger getInstance() {
        return INSTANCE;
    }

    /**
     * Standard-konstruktør som skriver til default-fil.
     * Brukes kun til singleton-instansen.
     */
    private SystemLogger(String logFilePath) {
        try {
            // true = append; ikke overskriv eksisterende logg
            this.writer = new PrintWriter(new FileWriter(logFilePath, true), true);
        } catch (IOException e) {
            // Hvis vi ikke klarer å åpne loggfilen, er det en alvorlig feil.
            // I en prototype kan vi kaste RuntimeException.
            throw new RuntimeException("Could not open log file: " + logFilePath, e);
        }
    }

    /**
     * Alternativ konstruktør for testing (f.eks. skrive til midlertidig fil).
     * Ikke brukt av singleton-getter, men kan være nyttig i tester.
     */
    public SystemLogger(String logFilePath, boolean asNewInstance) {
        try {
            this.writer = new PrintWriter(new FileWriter(logFilePath, true), true);
        } catch (IOException e) {
            throw new RuntimeException("Could not open log file: " + logFilePath, e);
        }
    }

    /**
     * Generell loggmetode.
     *
     * Nivå: INFO, ERROR, WARN, etc.
     * Event type: CREATE, ASSIGN, CANCEL, COMPLETE, REQUEST_VALIDATION_ERROR, osv.
     * Details: fri tekst, f.eks. "requestId=42 agent=agent1 reason=\"Invalid status\""
     *
     * Denne metoden er synkronisert, slik at hver oppføring skrives atomisk.
     */
    public synchronized void log(String level, String eventType, String details) {
        String timestamp = timeFormatter.format(Instant.now());
        // Eksempel på format: 2026-05-19T10:05:12.345Z [INFO] [CREATE] requestId=42 ...
        String line = String.format(
                "%s [%s] [%s] %s",
                timestamp,
                level.toUpperCase(),
                eventType.toUpperCase(),
                details
        );
        writer.println(line);
    }

    /**
     * Kortnavn for infos.
     */
    public void info(String eventType, String details) {
        log("INFO", eventType, details);
    }

    /**
     * Kortnavn for errors.
     */
    public void error(String eventType, String details) {
        log("ERROR", eventType, details);
    }

    /**
     * Kan brukes for å lukke loggeren eksplisitt ved programslutt.
     * (Ikke strengt nødvendig i en liten prototype, men "ryddig".)
     */
    public synchronized void close() {
        writer.flush();
        writer.close();
    }
}
