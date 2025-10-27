package testbase;

import repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class TestBase {
    protected static final Logger logger = Logger.getLogger(TestBase.class.getName());

    protected UserRepository userRepository;
    protected FunctionRepository functionRepository;
    protected FunctionPointRepository pointRepository;
    protected FunctionOperationRepository operationRepository;
    protected OperationTypeRepository typeRepository;

    @BeforeEach
    void setUp() throws SQLException {
        initializeRepositories();
        clearTestData();
        setupTestData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        clearTestData();
    }

    private void initializeRepositories() {
        userRepository = new UserRepository();
        functionRepository = new FunctionRepository();
        pointRepository = new FunctionPointRepository();
        operationRepository = new FunctionOperationRepository();
        typeRepository = new OperationTypeRepository();
    }

    protected void clearTestData() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Очистка в правильном порядке из-за foreign keys
            stmt.execute("DELETE FROM function_operations");
            stmt.execute("DELETE FROM function_points");
            stmt.execute("DELETE FROM functions");
            stmt.execute("DELETE FROM users");

            logger.info("Test data cleared");
        }
    }

    protected void setupTestData() throws SQLException {
        // Инициализация типов операций
        typeRepository.initializePredefinedTypes();
        logger.info("Test data setup completed");
    }

    // Вспомогательные методы для создания тестовых данных
    protected Long createTestUser(String username) throws SQLException {
        return userRepository.createUser(new model.User(username, "testpass"));
    }

    protected Long createTestFunction(Long userId, String name) throws SQLException {
        model.Function function = new model.Function(userId, name, "analytical", "base", "x^2");
        return functionRepository.createFunction(function);
    }
}