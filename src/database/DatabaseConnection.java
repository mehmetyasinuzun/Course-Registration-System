package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class DatabaseConnection {
    private static Connection connection = null;
    private static final String DB_PATH = "data/university.db";

    // Get connection (Singleton pattern)
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Create data directory if it does not exist
                File dataDir = new File("data");
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                }

                // Load SQLite driver
                Class.forName("org.sqlite.JDBC");

                // Create connection
                connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
                System.out.println("✓ Database connection established successfully!");

            } catch (ClassNotFoundException e) {
                System.out.println("✗ SQLite JDBC driver not found!");
                System.out.println("  Please add sqlite-jdbc-*.jar to classpath.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("✗ Database connection error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }

    // Close connection
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("✓ Database connection closed.");
            } catch (SQLException e) {
                System.out.println("✗ Error while closing connection: " + e.getMessage());
            }
        }
    }

    // Is connection open?
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
