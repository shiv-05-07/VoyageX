package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseManager manages SQLite JDBC database connections and schema initialization.
 * 
 * Demonstrates:
 * - JDBC Driver loading: org.sqlite.JDBC
 * - Establishing database connections via DriverManager.getConnection()
 * - Relational integrity: enabling SQLite foreign key enforcement (PRAGMA foreign_keys = ON)
 * - DDL Execution: creating tables with primary keys, foreign keys, and ON DELETE CASCADE
 * - Resource Management: try-with-resources ensuring Statement and Connection closure
 * - Configurable connection URL for seamless production and testing isolation
 */
public class DatabaseManager {

    public static final String DEFAULT_DB_URL = "jdbc:sqlite:voyagex.db";
    private static String currentDbUrl = DEFAULT_DB_URL;

    static {
        try {
            // Explicitly load the SQLite JDBC driver class
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    /**
     * Configures the active JDBC connection URL.
     * 
     * @param dbUrl the JDBC URL (e.g. "jdbc:sqlite:voyagex.db" or "jdbc:sqlite:test.db")
     */
    public static synchronized void setDatabaseUrl(String dbUrl) {
        if (dbUrl != null && !dbUrl.trim().isEmpty()) {
            currentDbUrl = dbUrl.trim();
        } else {
            currentDbUrl = DEFAULT_DB_URL;
        }
    }

    /**
     * Configures the database by file path.
     * 
     * @param fileName file name or path (e.g., "voyagex.db")
     */
    public static synchronized void setDatabaseFile(String fileName) {
        if (fileName != null && !fileName.trim().isEmpty()) {
            setDatabaseUrl("jdbc:sqlite:" + fileName.trim());
        } else {
            setDatabaseUrl(DEFAULT_DB_URL);
        }
    }

    /**
     * Gets the current database JDBC URL.
     */
    public static synchronized String getDatabaseUrl() {
        return currentDbUrl;
    }

    /**
     * Obtains a new database Connection with foreign keys enabled.
     * 
     * @return an open Connection to the SQLite database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(currentDbUrl);
        // SQLite disables foreign keys by default; explicitly enable for relational integrity
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initializes the default database schema.
     */
    public static void initializeDatabase() {
        initializeDatabase(currentDbUrl);
    }

    /**
     * Initializes database tables on the specified JDBC URL.
     * Creates 'trips', 'transport', 'activities', and 'expenses' tables if they do not exist.
     * 
     * @param dbUrl the JDBC database URL to initialize
     */
    public static synchronized void initializeDatabase(String dbUrl) {
        setDatabaseUrl(dbUrl);

        String createTripsSql = "CREATE TABLE IF NOT EXISTS trips ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "trip_name TEXT NOT NULL, "
                + "destination TEXT NOT NULL, "
                + "start_date TEXT NOT NULL, "
                + "end_date TEXT NOT NULL, "
                + "budget REAL NOT NULL"
                + ");";

        String createTransportSql = "CREATE TABLE IF NOT EXISTS transport ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "trip_id INTEGER NOT NULL UNIQUE, "
                + "transport_type TEXT NOT NULL, "
                + "source TEXT NOT NULL, "
                + "destination TEXT NOT NULL, "
                + "base_fare REAL NOT NULL, "
                + "extra_fee REAL NOT NULL DEFAULT 0.0, "
                + "is_ac INTEGER NOT NULL DEFAULT 0, "
                + "FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE"
                + ");";

        String createActivitiesSql = "CREATE TABLE IF NOT EXISTS activities ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "trip_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "date TEXT NOT NULL, "
                + "cost REAL NOT NULL, "
                + "FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE"
                + ");";

        String createExpensesSql = "CREATE TABLE IF NOT EXISTS expenses ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "trip_id INTEGER NOT NULL, "
                + "category TEXT NOT NULL, "
                + "description TEXT NOT NULL, "
                + "amount REAL NOT NULL, "
                + "FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTripsSql);
            stmt.execute(createTransportSql);
            stmt.execute(createActivitiesSql);
            stmt.execute(createExpensesSql);
        } catch (SQLException e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
        }
    }
}
