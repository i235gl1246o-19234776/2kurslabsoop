package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());

    // Убираем статические переменные - используем ThreadLocal для изоляции
    private static final ThreadLocal<String> url = ThreadLocal.withInitial(() -> "jdbc:postgresql://localhost:5432/test_10k_db"); //functions_database
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

    public static void resetToDefaultConnection() {
        url.set("jdbc:postgresql://localhost:5432/test_10k_db");
        user.set("postgres");
        password.set("1234");
        isTestMode.set(false);
        logger.info("Параметры подключения сброшены к рабочим настройкам для потока: " + Thread.currentThread().getName());
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

    public static boolean isTestMode() {
        return isTestMode.get();
    }

    public static String getConnectionInfo() {
        return String.format("URL: %s, User: %s, Mode: %s, Thread: %s",
                url.get(), user.get(), isTestMode.get() ? "test" : "production",
                Thread.currentThread().getName());
    }

    /**
     * Очистка ThreadLocal переменных (важно вызывать после тестов)
     */
    public static void cleanup() {
        url.remove();
        user.remove();
        password.remove();
        isTestMode.remove();
    }
}