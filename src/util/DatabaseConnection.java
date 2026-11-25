package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "PostgreSQL JDBC driver not found. Make sure the jar in lib/ is on the classpath.", e
            );
        }
    }

    private DatabaseConnection() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        String url;
        String user;
        String password;
        try {
            url = Env.require("DB_URL");
            user = Env.require("DB_USER");
            password = Env.require("DB_PASSWORD");
        } catch (IllegalStateException exception) {
            throw new SQLException(exception.getMessage(), exception);
        }
        return DriverManager.getConnection(url, user, password);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Connected to: " + connection.getCatalog());
        } catch (SQLException e) {
            System.err.println("Connection Failed: " + e.getMessage());
        }
    }
}