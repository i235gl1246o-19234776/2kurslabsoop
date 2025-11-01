package repository.test10k;

import model.Function;
import model.Operation;
import model.TabulatedFunction;
import model.User;
import org.junit.jupiter.api.*;
import repository.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class tenthousandTest {

    private static final Logger logger = Logger.getLogger(tenthousandTest.class.getName());
    private static final int TOTAL_RECORDS = 10_000;

    // Уникальное имя тестовой БД
    private static final String TEST_DB_NAME = "test_10k_db";
    private static final String ADMIN_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    private UserRepository userRepository;
    private FunctionRepository functionRepository;
    private TabulatedFunctionRepository tabulatedFunctionRepository;
    private OperationRepository operationRepository;

    private List<Long> userIds = new ArrayList<>();
    private List<Long> functionIds = new ArrayList<>();
    private List<Long> tabulatedFunctionIds = new ArrayList<>();
    private List<Long> operationIds = new ArrayList<>();

    private Random random = new Random();

    @BeforeAll
    void setUp() throws SQLException {
        System.out.println("=== ПОДКЛЮЧЕНИЕ К СУЩЕСТВУЮЩЕЙ ТЕСТОВОЙ БД ===");

        // ❌ Не создаём БД — она уже есть
        // createTestDatabase();
        // createTables();

        // ✅ Просто подключаемся к уже существующей БД
        DatabaseConnection.setTestConnection(
                "jdbc:postgresql://localhost:5432/test_10k_db", // ← имя вашей БД из лога
                USER,
                PASSWORD
        );

        System.out.println("Подключено к существующей БД: test_10k_db");
    } /*

    @BeforeAll
    void setUp() throws SQLException {
        System.out.println("=== НАСТРОЙКА ИЗОЛИРОВАННОЙ ТЕСТОВОЙ СРЕДЫ ===");

        createTestDatabase();
        createTables();
        initializeRepositories();

        System.out.println("Тестовая среда готова.");
    }
*/
    private void createTestDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(ADMIN_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Удаляем, если существует (для повторного запуска)
            stmt.execute("DROP DATABASE IF EXISTS " + TEST_DB_NAME);

            // Создаём новую
            stmt.execute("CREATE DATABASE " + TEST_DB_NAME);
            System.out.println("Создана тестовая БД: " + TEST_DB_NAME);
        }
    }

    private void createTables() throws SQLException {
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

    private void initializeRepositories() {
        // Переключаем глобальное подключение на нашу тестовую БД
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
    /*

    @AfterAll
    void tearDown() throws SQLException {
        System.out.println("=== ОЧИСТКА ТЕСТОВОЙ СРЕДЫ ===");

        // Сбрасываем подключение
        DatabaseConnection.resetToDefaultConnection();

        // Удаляем тестовую БД
        try (Connection conn = DriverManager.getConnection(ADMIN_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP DATABASE IF EXISTS " + TEST_DB_NAME);
            System.out.println("Тестовая БД удалена: " + TEST_DB_NAME);
        }
    }*/
    @AfterAll
    void tearDown() {
        System.out.println("=== ОТКЛЮЧЕНИЕ ОТ ТЕСТОВОЙ БД ===");
        DatabaseConnection.resetToDefaultConnection();
        // ❌ НЕ удаляем DROP DATABASE — данные хотим сохранить!
        System.out.println("Подключение сброшено.");
    }

    // ----------------------------
    // ОСТАЛЬНОЙ КОД ТЕСТА — БЕЗ ИЗМЕНЕНИЙ
    // ----------------------------

    @Test
    void testPerformanceWithLargeDataset() throws SQLException {

        System.out.println("=== ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ С " + TOTAL_RECORDS + " ЗАПИСЯМИ ===");

        long userGenerationTime = generateUsers();
        System.out.printf("Генерация пользователей: %,d ms%n", userGenerationTime);

        long functionGenerationTime = generateFunctions();
        System.out.printf("Генерация функций: %,d ms%n", functionGenerationTime);

        long tabulatedGenerationTime = generateTabulatedFunctions();
        System.out.printf("Генерация точек: %,d ms%n", tabulatedGenerationTime);

        long operationGenerationTime = generateOperations();
        System.out.printf("Генерация операций: %,d ms%n", operationGenerationTime);


        System.out.println("=== ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ЗАВЕРШЕН ===");
    }

    private long generateUsers() throws SQLException {
        System.out.println("Генерация " + TOTAL_RECORDS + " пользователей...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_RECORDS; i++) {
            User user = new User("user_" + i, "hash_" + i);
            Long userId = userRepository.createUser(user);
            userIds.add(userId);

            if (i % 1000 == 0) {
                System.out.println("Создано пользователей: " + i);
            }
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long generateFunctions() throws SQLException {
        System.out.println("Генерация " + TOTAL_RECORDS + " функций...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_RECORDS; i++) {
            Long userId = userIds.get(i);
            String type = (i % 2 == 0) ? "analytic" : "tabular";
            String name = "func_" + i;
            String expression = (i % 2 == 0) ? "x^" + (i % 5 + 1) : null;

            Function function = new Function(userId, type, name, expression);
            Long functionId = functionRepository.createFunction(function);
            functionIds.add(functionId);

            if (i % 1000 == 0) {
                System.out.println("Создано функций: " + i);
            }
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long generateTabulatedFunctions() throws SQLException {
        System.out.println("Генерация точек табулированных функций...");
        long startTime = System.currentTimeMillis();

        int pointsPerFunction = 10;
        int totalPoints = 0;

        for (int i = 0; i < TOTAL_RECORDS; i += 10) {
            if (i >= functionIds.size()) break;

            Long functionId = functionIds.get(i);

            for (int j = 0; j < pointsPerFunction; j++) {
                double x = j * 0.5;
                double y = Math.sin(x) + random.nextDouble() * 0.1;

                TabulatedFunction point = new TabulatedFunction(functionId, x, y);
                Long pointId = tabulatedFunctionRepository.createTabulatedFunction(point);
                tabulatedFunctionIds.add(pointId);
                totalPoints++;
            }

            if (i % 1000 == 0) {
                System.out.println("Создано точек: " + totalPoints);
            }
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long generateOperations() throws SQLException {
        System.out.println("Генерация операций...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_RECORDS; i += 2) {
            if (i >= functionIds.size()) break;

            Long functionId = functionIds.get(i);
            int operationType = random.nextInt(10) + 1;

            Operation operation = new Operation(functionId, operationType);
            Long operationId = operationRepository.createOperation(operation);
            operationIds.add(operationId);

            if (i % 1000 == 0) {
                System.out.println("Создано операций: " + (i / 2));
            }
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private void testUserQueryPerformance() throws SQLException {
        System.out.println("\n--- ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ЗАПРОСОВ ПОЛЬЗОВАТЕЛЕЙ ---");

        long startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            int index = random.nextInt(10_000);
            userRepository.findById(userIds.get(index));
        }
        long endTime = System.nanoTime();
        System.out.printf("Поиск 100 пользователей по ID: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 100);

        startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            int index = random.nextInt(1000);
            userRepository.findByName("user_" + index);
        }
        endTime = System.nanoTime();
        System.out.printf("Поиск 100 пользователей по имени: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 100);

        startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            int index = random.nextInt(1000);
            userRepository.userNameExists("user_" + index);
        }
        endTime = System.nanoTime();
        System.out.printf("Проверка 100 имен пользователей: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 100);
    }

    private void testFunctionQueryPerformance() throws SQLException {
        System.out.println("\n--- ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ЗАПРОСОВ ФУНКЦИЙ ---");

        long startTime = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            int index = random.nextInt(userIds.size());
            functionRepository.findByUserId(userIds.get(index));
        }
        long endTime = System.nanoTime();
        System.out.printf("Поиск функций 50 пользователей: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 50);

        startTime = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            int index = random.nextInt(userIds.size());
            functionRepository.findByType(userIds.get(index), "analytic");
        }
        endTime = System.nanoTime();
        System.out.printf("Поиск 50 аналитических функций: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 50);

        startTime = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            int index = random.nextInt(userIds.size());
            functionRepository.findByName(userIds.get(index), "func_1");
        }
        endTime = System.nanoTime();
        System.out.printf("Поиск 50 функций по шаблону: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 50);
    }

    private void testTabulatedFunctionQueryPerformance() throws SQLException {
        System.out.println("\n--- ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ЗАПРОСОВ ТОЧЕК ---");

        if (functionIds.isEmpty()) return;

        long startTime = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            int index = random.nextInt(Math.min(100, functionIds.size()));
            tabulatedFunctionRepository.findAllByFunctionId(functionIds.get(index));
        }
        long endTime = System.nanoTime();
        System.out.printf("Получение точек 20 функций: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 20);

        startTime = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            int index = random.nextInt(Math.min(100, functionIds.size()));
            tabulatedFunctionRepository.findBetweenXValues(functionIds.get(index), 1.0, 3.0);
        }
        endTime = System.nanoTime();
        System.out.printf("Поиск точек в диапазоне 20 функций: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 20);

        startTime = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            int index = random.nextInt(Math.min(100, functionIds.size()));
            tabulatedFunctionRepository.findByXValue(functionIds.get(index), 2.0);
        }
        endTime = System.nanoTime();
        System.out.printf("Поиск точек по X=2.0 для 20 функций: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 20);
    }

    private void testOperationQueryPerformance() throws SQLException {
        System.out.println("\n--- ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ЗАПРОСОВ ОПЕРАЦИЙ ---");

        if (functionIds.isEmpty() || operationIds.isEmpty()) return;

        long startTime = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            int opIndex = random.nextInt(Math.min(100, operationIds.size()));
            int funcIndex = random.nextInt(Math.min(100, functionIds.size()));
            operationRepository.findById(operationIds.get(opIndex), functionIds.get(funcIndex));
        }
        long endTime = System.nanoTime();
        System.out.printf("Поиск 50 операций по ID: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 50);
    }


    @Test
    void testDatabaseStatistics() throws SQLException {
        System.out.println("\n=== СТАТИСТИКА БАЗЫ ДАННЫХ ===");

        try (var conn = DatabaseConnection.getConnection();
             var stmt = conn.createStatement()) {

            try (var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users")) {
                if (rs.next()) {
                    System.out.printf("Всего пользователей: %,d%n", rs.getLong("count"));
                }
            }

            try (var rs = stmt.executeQuery("""
                SELECT COUNT(*) as count,
                       SUM(CASE WHEN type_function = 'analytic' THEN 1 ELSE 0 END) as analytic,
                       SUM(CASE WHEN type_function = 'tabular' THEN 1 ELSE 0 END) as tabular
                FROM functions
                """)) {
                if (rs.next()) {
                    System.out.printf("Всего функций: %,d (аналитические: %,d, табулированные: %,d)%n",
                            rs.getLong("count"), rs.getLong("analytic"), rs.getLong("tabular"));
                }
            }

            try (var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM tabulated_functions")) {
                if (rs.next()) {
                    System.out.printf("Всего точек: %,d%n", rs.getLong("count"));
                }
            }

            try (var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM operations")) {
                if (rs.next()) {
                    System.out.printf("Всего операций: %,d%n", rs.getLong("count"));
                }
            }

            try (var rs = stmt.executeQuery("""
                SELECT tablename, pg_size_pretty(pg_total_relation_size('"' || tablename || '"')) as size
                FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename
                """)) {
                System.out.println("Размеры таблиц:");
                while (rs.next()) {
                    System.out.printf("  %s: %s%n", rs.getString("tablename"), rs.getString("size"));
                }
            }
            testUserQueryPerformance();
            testFunctionQueryPerformance();
            testTabulatedFunctionQueryPerformance();
            testOperationQueryPerformance();
        }
    }
}