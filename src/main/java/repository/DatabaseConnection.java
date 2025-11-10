package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseConnection{
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());

    private static final ThreadLocal<String> url = ThreadLocal.withInitial(() -> "jdbc:postgresql://localhost:5432/functions_database");
    private static final ThreadLocal<String> user = ThreadLocal.withInitial(() -> "postgres");
    private static final ThreadLocal<String> password = ThreadLocal.withInitial(() -> "1234");
    private static final ThreadLocal<Boolean> isTestMode = ThreadLocal.withInitial(() -> false);

    public static void setTestConnection(String testUrl, String testUser, String testPassword) {
        url.set(testUrl);
        user.set(testUser);
        password.set(testPassword);
        isTestMode.set(true);
        logger.info("Установлены тестовые параметры подключения: " + testUrl + " для потока: " + Thread.currentThread().getName());
    }


    public static Connection getConnection() throws SQLException {
        try {
            String currentUrl = url.get();
            String currentUser = user.get();
            String currentPassword = password.get();

            logger.info("Подключение для потока " + Thread.currentThread().getName() +
                    ": " + currentUrl + ", user: " + currentUser);

            Connection conn = DriverManager.getConnection(currentUrl, currentUser, currentPassword);
            return conn;
        } catch (SQLException e) {
            logger.severe("Ошибка подключения к базе данных в потоке " + Thread.currentThread().getName() +
                    ": " + e.getMessage());
            throw e;
        }
    }

    public boolean isTestMode() {
        return isTestMode.get();
    }

    public String getConnectionInfo() {
        return String.format("URL: %s, User: %s, Mode: %s, Thread: %s",
                url.get(), user.get(), isTestMode.get() ? "test" : "production",
                Thread.currentThread().getName());
    }


    public static void setConnectionParams(String connectionUrl, String connectionUser, String connectionPassword, boolean testMode) {
        url.set(connectionUrl);
        user.set(connectionUser);
        password.set(connectionPassword);
        isTestMode.set(testMode);
        logger.info("Установлены параметры подключения: " + connectionUrl + " (mode: " + (testMode ? "test" : "production") + ") для потока: " + Thread.currentThread().getName());
    }

    protected static void cleanup() {
    }


}