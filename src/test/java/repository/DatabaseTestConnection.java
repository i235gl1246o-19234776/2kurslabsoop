package repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseTestConnection extends DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseTestConnection.class.getName());

    private static final String DEFAULT_TEST_URL = "jdbc:postgresql://localhost:5432/test_functions_database";
    private static final String DEFAULT_TEST_USER = "postgres";
    private static final String DEFAULT_TEST_PASSWORD = "1234";

    private final String testUrl;
    private final String testUser;
    private final String testPassword;

    public DatabaseTestConnection() {
        this(DEFAULT_TEST_URL, DEFAULT_TEST_USER, DEFAULT_TEST_PASSWORD);
    }

    public DatabaseTestConnection(String testUrl, String testUser, String testPassword) {
        super();

        this.testUrl = testUrl;
        this.testUser = testUser;
        this.testPassword = testPassword;

        setupTestConnection();
    }

    private void setupTestConnection() {
        setTestConnection(testUrl, testUser, testPassword);
        logger.info("DatabaseTestConnection инициализирован для: " + testUrl);
    }

    public static Connection getConnection() throws SQLException {
        logger.fine("DatabaseTestConnection: получение соединения с тестовой БД");
        return DatabaseConnection.getConnection(); // Вызов родительского статического метода
    }

    public void switchBackToTest() {
        setTestConnection(testUrl, testUser, testPassword);
        logger.info("DatabaseTestConnection переключен обратно на тестовую БД");
    }

}