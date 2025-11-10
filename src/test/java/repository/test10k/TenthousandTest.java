package repository.test10k;

import model.entity.Function;
import model.entity.Operation;
import model.entity.TabulatedFunction;
import model.entity.User;
import repository.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TenthousandTest {
    private static final int TOTAL_RECORDS = 10_000;
    private static final String TEST_DB_NAME = "test_10k_db";
    private static final String ADMIN_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    private static UserRepository userRepository;
    private static FunctionRepository functionRepository;
    private static TabulatedFunctionRepository tabulatedFunctionRepository;
    private static OperationRepository operationRepository;

    private static List<Long> userIds = new ArrayList<>();
    private static List<Long> functionIds = new ArrayList<>();
    private static List<Long> operationIds = new ArrayList<>();

    private static Random random = new Random();

    public static void main(String[] args) {
        try {
            System.out.println("=== БД " + TOTAL_RECORDS + " ЗАПИСЯМИ ===");

            System.out.println("Тестовая БД не найдена. Создаем новую...");
            setupTestEnvironment();
            generateTestData();


            System.out.println("=== ТЕСТ ЗАВЕРШЕН ===");

        } catch (Exception e) {
            System.err.println("Ошибка при выполнении теста: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static boolean isTestDatabaseExists() throws SQLException {
        try (Connection conn = DriverManager.getConnection(ADMIN_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB_NAME + "'"
            );
            return rs.next();
        }
    }

    private static void setupTestEnvironment() throws SQLException {
        System.out.println("=== НАСТРОЙКА ИЗОЛИРОВАННОЙ ТЕСТОВОЙ СРЕДЫ ===");

        createTestDatabase();
        createTables();
        createStatisticsTable();
        initializeRepositories();

        System.out.println("Тестовая среда готова.");
    }

    private static void createTestDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(ADMIN_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.execute("UPDATE pg_database SET datallowconn = 'false' WHERE datname = '" + TEST_DB_NAME + "'");
            stmt.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '" + TEST_DB_NAME + "' AND pid <> pg_backend_pid()");
            stmt.execute("DROP DATABASE IF EXISTS " + TEST_DB_NAME);

            stmt.execute("CREATE DATABASE " + TEST_DB_NAME);
            System.out.println("Создана тестовая БД: " + TEST_DB_NAME);
        }
    }

    private static void createStatisticsTable() throws SQLException {
        String testUrl = "jdbc:postgresql://localhost:5432/" + TEST_DB_NAME;
        try (Connection conn = DriverManager.getConnection(testUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS performance_statistics (
                id SERIAL PRIMARY KEY,
                test_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                metric_category VARCHAR(50) NOT NULL,
                metric_name VARCHAR(100) NOT NULL,
                metric_value VARCHAR(255) NOT NULL,
                numeric_value DOUBLE PRECISION,
                records_count INTEGER,
                execution_time_ms BIGINT
            )
            """);

            stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_statistics_timestamp 
            ON performance_statistics(test_timestamp)
            """);

            System.out.println("Таблица для статистики создана/проверена");
        }
    }

    private static void createTables() throws SQLException {
        String testUrl = "jdbc:postgresql://localhost:5432/" + TEST_DB_NAME;
        try (Connection conn = DriverManager.getConnection(testUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE users (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL
                )
                """);

            stmt.execute("""
                CREATE TABLE functions (
                    id SERIAL PRIMARY KEY,
                    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    type_function VARCHAR(20) CHECK (type_function IN ('tabular', 'analytic')),
                    function_name VARCHAR(255) NOT NULL,
                    function_expression TEXT
                )
                """);

            stmt.execute("""
                CREATE TABLE tabulated_functions (
                    id SERIAL PRIMARY KEY,
                    function_id INTEGER NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
                    x_val DOUBLE PRECISION NOT NULL,
                    y_val DOUBLE PRECISION NOT NULL
                )
                """);

            stmt.execute("""
                CREATE TABLE operations (
                    id SERIAL PRIMARY KEY,
                    function_id INTEGER NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
                    operations_type_id INTEGER NOT NULL
                )
                """);

            System.out.println("Таблицы созданы в БД: " + TEST_DB_NAME);
        }
    }

    private static void initializeRepositories() {
        DatabaseConnection.setTestConnection(
                "jdbc:postgresql://localhost:5432/" + TEST_DB_NAME,
                USER,
                PASSWORD
        );

        userRepository = new UserRepository();
        functionRepository = new FunctionRepository();
        tabulatedFunctionRepository = new TabulatedFunctionRepository();
        operationRepository = new OperationRepository();
    }

    private static void generateTestData() throws SQLException {
        System.out.println("=== ГЕНЕРАЦИЯ ТЕСТОВЫХ ДАННЫХ ===");

        long userGenerationTime = generateUsers();
        System.out.printf("Генерация пользователей: %,d ms%n", userGenerationTime);

        long functionGenerationTime = generateFunctions();
        System.out.printf("Генерация функций: %,d ms%n", functionGenerationTime);

        long tabulatedGenerationTime = generateTabulatedFunctions();
        System.out.printf("Генерация точек: %,d ms%n", tabulatedGenerationTime);

        long operationGenerationTime = generateOperations();
        System.out.printf("Генерация операций: %,d ms%n", operationGenerationTime);

        System.out.println("Генерация данных завершена");
    }

    private static long generateUsers() throws SQLException {
        userIds.clear();
        System.out.println("Генерация " + TOTAL_RECORDS + " пользователей...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_RECORDS; i++) {
            User user = new User("user_" + i, "hash_" + i);
            Long userId = userRepository.createUser(user);
            userIds.add(userId);
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static long generateFunctions() throws SQLException {
        functionIds.clear();
        System.out.println("Генерация " + TOTAL_RECORDS + " функций...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_RECORDS; i++) {
            Long userId = userIds.get(i % userIds.size());
            String type = (i % 2 == 0) ? "analytic" : "tabular";
            String name = "func_" + i;
            String expression = (i % 2 == 0) ? "x^" + (i % 5 + 1) : null;

            Function function = new Function(userId, type, name, expression);
            Long functionId = functionRepository.createFunction(function);
            functionIds.add(functionId);
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static long generateTabulatedFunctions() throws SQLException {
        System.out.println("Генерация точек табулированных функций...");
        long startTime = System.currentTimeMillis();

        int pointsPerFunction = 1;
        for (int i = 0; i < functionIds.size(); i++) {
            Long functionId = functionIds.get(i);
            for (int j = 0; j < pointsPerFunction; j++) {
                double x = j * 0.5;
                double y = Math.sin(x) + random.nextDouble() * 0.1;
                TabulatedFunction point = new TabulatedFunction(functionId, x, y);
                tabulatedFunctionRepository.createTabulatedFunction(point);
            }
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static long generateOperations() throws SQLException {
        operationIds.clear();
        System.out.println("Генерация операций...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_RECORDS; i++) {
            Long functionId = functionIds.get(i % functionIds.size());
            int operationType = random.nextInt(10) + 1;
            Operation operation = new Operation(functionId, operationType);
            Long opId = operationRepository.createOperation(operation);
            operationIds.add(opId);
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static void loadExistingIds() throws SQLException {
        System.out.println("Загрузка существующих ID из базы данных...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT id FROM users ORDER BY id");
            while (rs.next()) {
                userIds.add(rs.getLong("id"));
            }

            rs = stmt.executeQuery("SELECT id FROM functions ORDER BY id");
            while (rs.next()) {
                functionIds.add(rs.getLong("id"));
            }

            rs = stmt.executeQuery("SELECT id FROM operations ORDER BY id");
            while (rs.next()) {
                operationIds.add(rs.getLong("id"));
            }

            System.out.printf("Загружено: %,d пользователей, %,d функций, %,d операций%n",
                    userIds.size(), functionIds.size(), operationIds.size());
        }
    }

}
