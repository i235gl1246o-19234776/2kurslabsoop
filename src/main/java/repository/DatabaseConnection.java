package repository;

import java.sql.*;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());

    // Конфигурация базы данных - настройте под вашу среду
    private static final String URL = "jdbc:postgresql://localhost:5432/functions_database";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    private static Connection connection;

    static {
        try {
            // Регистрируем драйвер PostgreSQL
            Class.forName("org.postgresql.Driver");
            logger.info("PostgreSQL JDBC Driver registered successfully");
        } catch (ClassNotFoundException e) {
            logger.severe("PostgreSQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("Failed to register PostgreSQL JDBC Driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                logger.info("Database connection established successfully to: " + URL);
            } catch (SQLException e) {
                logger.severe("Failed to establish database connection: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }
}