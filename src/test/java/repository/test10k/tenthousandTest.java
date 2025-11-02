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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class tenthousandTest {

    private static final int TOTAL_RECORDS = 10_000;

    private static final String TEST_DB_NAME = "test_10k_db";
    private static final String ADMIN_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    private UserRepository userRepository;
    private FunctionRepository functionRepository;
    private TabulatedFunctionRepository tabulatedFunctionRepository;
    private OperationRepository operationRepository;

    // Поля класса — используются во всех методах
    private List<Long> userIds = new ArrayList<>();
    private List<Long> functionIds = new ArrayList<>();
    private List<Long> operationIds = new ArrayList<>();

    private Random random = new Random();

    @BeforeAll
    void setUp() throws SQLException {
        System.out.println("=== НАСТРОЙКА ИЗОЛИРОВАННОЙ ТЕСТОВОЙ СРЕДЫ ===");

        createTestDatabase();
        createTables();
        createStatisticsTable();
        initializeRepositories();

        System.out.println("Тестовая среда готова.");
    }

    private void createTestDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(ADMIN_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Завершаем активные подключения перед DROP
            stmt.execute("UPDATE pg_database SET datallowconn = 'false' WHERE datname = '" + TEST_DB_NAME + "'");
            stmt.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '" + TEST_DB_NAME + "' AND pid <> pg_backend_pid()");
            stmt.execute("DROP DATABASE IF EXISTS " + TEST_DB_NAME);

            stmt.execute("CREATE DATABASE " + TEST_DB_NAME);
            System.out.println("Создана тестовая БД: " + TEST_DB_NAME);
        }
    }
    private void createStatisticsTable() throws SQLException {
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

            // Создаем индекс для быстрого поиска по времени теста
            stmt.execute("""
            CREATE INDEX IF NOT EXISTS idx_statistics_timestamp 
            ON performance_statistics(test_timestamp)
            """);

            System.out.println("Таблица для статистики создана/проверена");
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

    @AfterAll
    void tearDown() {
        System.out.println("=== ОТКЛЮЧЕНИЕ ОТ ТЕСТОВОЙ БД ===");
        DatabaseConnection.resetToDefaultConnection();
        System.out.println("Подключение сброшено. (БД НЕ удаляется для анализа)");
    }

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

        testDatabaseStatistics();

        System.out.println("=== ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ЗАВЕРШЕН ===");
    }

    private long generateUsers() throws SQLException {
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

    private long generateFunctions() throws SQLException {
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

    private long generateTabulatedFunctions() throws SQLException {
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

    private long generateOperations() throws SQLException {
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

    // =============== ТЕСТЫ ПРОИЗВОДИТЕЛЬНОСТИ ===============

    private long testUserQueryPerformance() throws SQLException {
        if (userIds.isEmpty()) return 0;
        System.out.println("\n--- ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ЗАПРОСОВ ПОЛЬЗОВАТЕЛЕЙ ---");

        long startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            int index = random.nextInt(userIds.size()); // ✅ безопасно
            userRepository.findById(userIds.get(index));
        }
        long endTime = System.nanoTime();
        System.out.printf("Поиск 100 пользователей по ID: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 100);

        startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            int index = random.nextInt(Math.min(10, userIds.size())); // имена user_0..user_9
            userRepository.findByName("user_" + index);
        }
        endTime = System.nanoTime();
        System.out.printf("Поиск 100 пользователей по имени: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 100);

        startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            int index = random.nextInt(Math.min(1000, userIds.size() + 100)); // проверка существования
            userRepository.userNameExists("user_" + index);
        }
        endTime = System.nanoTime();
        System.out.printf("Проверка 100 имен пользователей: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 100);
        return (endTime - startTime);
    }

    private long testFunctionQueryPerformance() throws SQLException {
        if (userIds.isEmpty() || functionIds.isEmpty()) return 0;
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
        return (endTime - startTime);
    }

    private long testTabulatedFunctionQueryPerformance() throws SQLException {
        if (functionIds.isEmpty()) return 0;
        System.out.println("\n--- ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ЗАПРОСОВ ТОЧЕК ---");

        long startTime = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            int index = random.nextInt(functionIds.size());
            tabulatedFunctionRepository.findAllByFunctionId(functionIds.get(index));
        }
        long endTime = System.nanoTime();
        System.out.printf("Получение точек 20 функций: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 20);

        startTime = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            int index = random.nextInt(functionIds.size());
            tabulatedFunctionRepository.findBetweenXValues(functionIds.get(index), 1.0, 3.0);
        }
        endTime = System.nanoTime();
        System.out.printf("Поиск точек в диапазоне 20 функций: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 20);

        startTime = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            int index = random.nextInt(functionIds.size());
            tabulatedFunctionRepository.findByXValue(functionIds.get(index), 2.0);
        }
        endTime = System.nanoTime();
        System.out.printf("Поиск точек по X=2.0 для 20 функций: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 20);
        return (endTime - startTime);
    }

    private long testOperationQueryPerformance() throws SQLException {
        if (operationIds.isEmpty() || functionIds.isEmpty()) return 0;
        System.out.println("\n--- ТЕСТ ПРОИЗВОДИТЕЛЬНОСТИ ЗАПРОСОВ ОПЕРАЦИЙ ---");

        long startTime = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            int opIndex = random.nextInt(operationIds.size());
            int funcIndex = random.nextInt(functionIds.size());
            // Предполагается, что findById принимает ID операции и функции
            // Если логика другая — адаптируйте
            operationRepository.findById(operationIds.get(opIndex), functionIds.get(funcIndex));
        }
        long endTime = System.nanoTime();
        System.out.printf("Поиск 50 операций по ID: %,d ns (%,d ns/запрос)%n",
                (endTime - startTime), (endTime - startTime) / 50);
        return (endTime - startTime);
    }
    private void testDatabaseStatistics() throws SQLException {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ПОЛНАЯ СТАТИСТИКА БАЗЫ ДАННЫХ");
        System.out.println("=".repeat(50));

        // Сохраняем временную метку теста
        Timestamp testTimestamp = new Timestamp(System.currentTimeMillis());

        try (var conn = DatabaseConnection.getConnection()) {

            // Очищаем старые записи для этого теста
            clearOldStatistics(conn, testTimestamp);

            // Сохраняем основную информацию о тесте
            saveStatistic(conn, testTimestamp, "TEST_INFO", "total_records", String.valueOf(TOTAL_RECORDS), TOTAL_RECORDS, TOTAL_RECORDS, 0);
            saveStatistic(conn, testTimestamp, "TEST_INFO", "test_timestamp", testTimestamp.toString(), 0, 0, 0);

            // Общая статистика по таблицам
            System.out.println("\n--- ОБЩАЯ СТАТИСТИКА ---");

            String[] tables = {"users", "functions", "tabulated_functions", "operations"};
            for (String table : tables) {
                long count = getSingleResult(conn, "SELECT COUNT(*) FROM " + table);
                System.out.printf("%-20s: %,9d записей%n", table, count);
                saveStatistic(conn, testTimestamp, "TABLE_STATS", table + "_count", String.valueOf(count), count, count, 0);
            }

            // Детальная статистика функций
            System.out.println("\n--- СТАТИСТИКА ФУНКЦИЙ ---");
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("""
                SELECT 
                    COUNT(*) as total,
                    SUM(CASE WHEN type_function = 'analytic' THEN 1 ELSE 0 END) as analytic,
                    SUM(CASE WHEN type_function = 'tabular' THEN 1 ELSE 0 END) as tabular,
                    COUNT(DISTINCT user_id) as unique_users
                FROM functions
                """)) {
                if (rs.next()) {
                    long total = rs.getLong("total");
                    long analytic = rs.getLong("analytic");
                    long tabular = rs.getLong("tabular");
                    long uniqueUsers = rs.getLong("unique_users");

                    System.out.printf("Всего функций: %,d%n", total);
                    System.out.printf("Аналитические: %,d (%.1f%%)%n",
                            analytic, total > 0 ? (analytic * 100.0 / total) : 0);
                    System.out.printf("Табулированные: %,d (%.1f%%)%n",
                            tabular, total > 0 ? (tabular * 100.0 / total) : 0);
                    System.out.printf("Уникальных пользователей с функциями: %,d%n", uniqueUsers);

                    saveStatistic(conn, testTimestamp, "FUNCTIONS", "total_functions", String.valueOf(total), total, total, 0);
                    saveStatistic(conn, testTimestamp, "FUNCTIONS", "analytic_functions", String.valueOf(analytic), analytic, analytic, 0);
                    saveStatistic(conn, testTimestamp, "FUNCTIONS", "tabular_functions", String.valueOf(tabular), tabular, tabular, 0);
                    saveStatistic(conn, testTimestamp, "FUNCTIONS", "unique_users_with_functions", String.valueOf(uniqueUsers), uniqueUsers, uniqueUsers, 0);
                }
            }

            // Статистика точек
            System.out.println("\n--- СТАТИСТИКА ТОЧЕК ---");
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("""
                SELECT 
                    COUNT(*) as total_points,
                    COUNT(DISTINCT function_id) as functions_with_points,
                    AVG(points_per_function) as avg_points_per_function
                FROM (
                    SELECT function_id, COUNT(*) as points_per_function
                    FROM tabulated_functions 
                    GROUP BY function_id
                ) as point_counts
                """)) {
                if (rs.next()) {
                    long totalPoints = rs.getLong("total_points");
                    long functionsWithPoints = rs.getLong("functions_with_points");
                    double avgPoints = rs.getDouble("avg_points_per_function");

                    System.out.printf("Всего точек: %,d%n", totalPoints);
                    System.out.printf("Функций с точками: %,d%n", functionsWithPoints);
                    System.out.printf("Среднее точек на функцию: %.1f%n", avgPoints);

                    saveStatistic(conn, testTimestamp, "POINTS", "total_points", String.valueOf(totalPoints), totalPoints, totalPoints, 0);
                    saveStatistic(conn, testTimestamp, "POINTS", "functions_with_points", String.valueOf(functionsWithPoints), functionsWithPoints, functionsWithPoints, 0);
                    saveStatistic(conn, testTimestamp, "POINTS", "avg_points_per_function", String.format("%.2f", avgPoints), avgPoints, 0, 0);
                }
            }

            // Статистика операций
            System.out.println("\n--- СТАТИСТИКА ОПЕРАЦИЙ ---");
            long totalOperations = getSingleResult(conn, "SELECT COUNT(*) FROM operations");
            long functionsWithOperations = getSingleResult(conn, "SELECT COUNT(DISTINCT function_id) FROM operations");

            System.out.printf("Всего операций: %,d%n", totalOperations);
            System.out.printf("Функций с операциями: %,d%n", functionsWithOperations);

            saveStatistic(conn, testTimestamp, "OPERATIONS", "total_operations", String.valueOf(totalOperations), totalOperations, totalOperations, 0);
            saveStatistic(conn, testTimestamp, "OPERATIONS", "functions_with_operations", String.valueOf(functionsWithOperations), functionsWithOperations, functionsWithOperations, 0);

            // Распределение по типам операций
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("""
                SELECT operations_type_id, COUNT(*) as count
                FROM operations 
                GROUP BY operations_type_id 
                ORDER BY count DESC
                """)) {
                System.out.println("Распределение по типам операций:");
                while (rs.next()) {
                    int typeId = rs.getInt("operations_type_id");
                    long count = rs.getLong("count");
                    double percentage = totalOperations > 0 ? (count * 100.0 / totalOperations) : 0;
                    System.out.printf("  Тип %2d: %,6d операций (%.1f%%)%n", typeId, count, percentage);

                    saveStatistic(conn, testTimestamp, "OPERATION_TYPES",
                            "operation_type_" + typeId,
                            String.format("%,d операций", count),
                            count, count, 0);
                }
            }

            // Размеры таблиц и индексы - ИСПРАВЛЕННАЯ ВЕРСИЯ
            System.out.println("\n--- РАЗМЕРЫ ТАБЛИЦ И ИНДЕКСЫ ---");
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("""
                SELECT 
                    tablename,
                    pg_size_pretty(pg_total_relation_size('"' || tablename || '"')) as total_size_pretty,
                    pg_size_pretty(pg_relation_size('"' || tablename || '"')) as table_size_pretty,
                    pg_size_pretty(pg_total_relation_size('"' || tablename || '"') - pg_relation_size('"' || tablename || '"')) as index_size_pretty,
                    pg_total_relation_size('"' || tablename || '"') as total_bytes,
                    pg_relation_size('"' || tablename || '"') as table_bytes,
                    (pg_total_relation_size('"' || tablename || '"') - pg_relation_size('"' || tablename || '"')) as index_bytes
                FROM pg_tables 
                WHERE schemaname = 'public' 
                ORDER BY pg_total_relation_size('"' || tablename || '"') DESC
                """)) {
                while (rs.next()) {
                    String tableName = rs.getString("tablename");
                    String totalSizePretty = rs.getString("total_size_pretty");
                    String tableSizePretty = rs.getString("table_size_pretty");
                    String indexSizePretty = rs.getString("index_size_pretty");
                    long totalBytes = rs.getLong("total_bytes");
                    long tableBytes = rs.getLong("table_bytes");
                    long indexBytes = rs.getLong("index_bytes");

                    System.out.printf("%-20s: %8s (таблица: %s, индексы: %s)%n",
                            tableName, totalSizePretty, tableSizePretty, indexSizePretty);

                    // Сохраняем форматированные значения как строки
                    saveStatistic(conn, testTimestamp, "TABLE_SIZES", tableName + "_total_size", totalSizePretty, totalBytes, 0, 0);
                    saveStatistic(conn, testTimestamp, "TABLE_SIZES", tableName + "_table_size", tableSizePretty, tableBytes, 0, 0);
                    saveStatistic(conn, testTimestamp, "TABLE_SIZES", tableName + "_index_size", indexSizePretty, indexBytes, 0, 0);

                    // Дополнительно сохраняем числовые значения в байтах
                    saveStatistic(conn, testTimestamp, "TABLE_SIZES_BYTES", tableName + "_total_bytes", String.valueOf(totalBytes), totalBytes, 0, 0);
                    saveStatistic(conn, testTimestamp, "TABLE_SIZES_BYTES", tableName + "_table_bytes", String.valueOf(tableBytes), tableBytes, 0, 0);
                    saveStatistic(conn, testTimestamp, "TABLE_SIZES_BYTES", tableName + "_index_bytes", String.valueOf(indexBytes), indexBytes, 0, 0);
                }
            }

            // Информация об индексах
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("""
                SELECT 
                    indexname,
                    tablename,
                    pg_size_pretty(pg_relation_size('"' || indexname || '"')) as index_size_pretty,
                    pg_relation_size('"' || indexname || '"') as index_bytes
                FROM pg_indexes 
                WHERE schemaname = 'public'
                ORDER BY tablename, indexname
                """)) {
                System.out.println("\nИндексы:");
                while (rs.next()) {
                    String indexName = rs.getString("indexname");
                    String tableName = rs.getString("tablename");
                    String indexSizePretty = rs.getString("index_size_pretty");
                    long indexBytes = rs.getLong("index_bytes");

                    System.out.printf("  %-20s на %-15s: %s%n", indexName, tableName, indexSizePretty);
                    saveStatistic(conn, testTimestamp, "INDEXES", indexName,
                            String.format("на %s: %s", tableName, indexSizePretty), indexBytes, 0, 0);
                }
            }

            // Загрузка ID для тестов производительности
            loadIdsForPerformanceTests(conn);

            // Запуск тестов производительности и сохранение результатов
            System.out.println("\n--- ТЕСТЫ ПРОИЗВОДИТЕЛЬНОСТИ ---");
            long userPerfTime = testUserQueryPerformance();
            long functionPerfTime = testFunctionQueryPerformance();
            long tabulatedPerfTime = testTabulatedFunctionQueryPerformance();
            long operationPerfTime = testOperationQueryPerformance();

            // Сохраняем результаты производительности
            saveStatistic(conn, testTimestamp, "PERFORMANCE", "user_queries_100_time_ns",
                    String.format("%,d ns", userPerfTime), userPerfTime, 100, userPerfTime);
            saveStatistic(conn, testTimestamp, "PERFORMANCE", "function_queries_50_time_ns",
                    String.format("%,d ns", functionPerfTime), functionPerfTime, 50, functionPerfTime);
            saveStatistic(conn, testTimestamp, "PERFORMANCE", "tabulated_queries_20_time_ns",
                    String.format("%,d ns", tabulatedPerfTime), tabulatedPerfTime, 20, tabulatedPerfTime);
            saveStatistic(conn, testTimestamp, "PERFORMANCE", "operation_queries_50_time_ns",
                    String.format("%,d ns", operationPerfTime), operationPerfTime, 50, operationPerfTime);

            // Сохраняем среднее время на запрос
            saveStatistic(conn, testTimestamp, "PERFORMANCE", "avg_user_query_time_ns",
                    String.format("%,d ns", userPerfTime / 100), userPerfTime / 100, 100, userPerfTime);
            saveStatistic(conn, testTimestamp, "PERFORMANCE", "avg_function_query_time_ns",
                    String.format("%,d ns", functionPerfTime / 50), functionPerfTime / 50, 50, functionPerfTime);
            saveStatistic(conn, testTimestamp, "PERFORMANCE", "avg_tabulated_query_time_ns",
                    String.format("%,d ns", tabulatedPerfTime / 20), tabulatedPerfTime / 20, 20, tabulatedPerfTime);
            saveStatistic(conn, testTimestamp, "PERFORMANCE", "avg_operation_query_time_ns",
                    String.format("%,d ns", operationPerfTime / 50), operationPerfTime / 50, 50, operationPerfTime);

            // Общее время выполнения всех тестов
            long totalTestTime = userPerfTime + functionPerfTime + tabulatedPerfTime + operationPerfTime;
            saveStatistic(conn, testTimestamp, "PERFORMANCE", "total_performance_test_time_ns",
                    String.format("%,d ns", totalTestTime), totalTestTime, 220, totalTestTime);

            System.out.println("\n" + "=".repeat(50));
            System.out.println("ВСЯ СТАТИСТИКА СОХРАНЕНА В БАЗЕ ДАННЫХ");
            System.out.println("=".repeat(50));

            // Показываем сохраненную статистику
            showSavedStatistics(conn, testTimestamp);
        }
    }

// ДОБАВЬТЕ ЭТИ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ:

    private void checkTableStructure(Connection conn, Timestamp timestamp) throws SQLException {
        String[] tables = {"users", "functions", "tabulated_functions", "operations"};

        for (String table : tables) {
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery(
                         "SELECT column_name, data_type FROM information_schema.columns " +
                                 "WHERE table_name = '" + table + "' ORDER BY ordinal_position")) {

                System.out.println("Таблица " + table + ":");
                List<String> columns = new ArrayList<>();
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String dataType = rs.getString("data_type");
                    System.out.println("  " + columnName + " (" + dataType + ")");
                    columns.add(columnName);
                }

                // Сохраняем информацию о структуре
                saveStatistic(conn, timestamp, "TABLE_STRUCTURE", table + "_columns",
                        String.join(", ", columns), columns.size(), columns.size(), 0);
            }
        }
    }

    private boolean checkColumnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = """
        SELECT COUNT(*) as count 
        FROM information_schema.columns 
        WHERE table_name = ? AND column_name = ?
        """;

        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            try (var rs = stmt.executeQuery()) {
                return rs.next() && rs.getLong("count") > 0;
            }
        }
    }

// Вспомогательные методы (добавьте их в класс):

    private void clearOldStatistics(Connection conn, Timestamp timestamp) throws SQLException {
        String sql = "DELETE FROM performance_statistics WHERE test_timestamp = ?";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, timestamp);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                System.out.printf("Удалено %d старых записей статистики%n", deleted);
            }
        }
    }

    private void saveStatistic(Connection conn, Timestamp timestamp, String category,
                               String name, String value, double numericValue,
                               long recordsCount, long executionTime) throws SQLException {
        String sql = """
        INSERT INTO performance_statistics 
        (test_timestamp, metric_category, metric_name, metric_value, numeric_value, records_count, execution_time_ms)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, timestamp);
            stmt.setString(2, category);
            stmt.setString(3, name);
            stmt.setString(4, value);
            stmt.setDouble(5, numericValue);
            stmt.setLong(6, recordsCount);
            stmt.setLong(7, executionTime);
            stmt.executeUpdate();
        }
    }

    private void showSavedStatistics(Connection conn, Timestamp timestamp) throws SQLException {
        System.out.println("\n--- СОХРАНЕННАЯ СТАТИСТИКА В БАЗЕ ---");

        String sql = """
        SELECT metric_category, COUNT(*) as metric_count,
               SUM(records_count) as total_records,
               SUM(execution_time_ms) as total_time_ns
        FROM performance_statistics 
        WHERE test_timestamp = ?
        GROUP BY metric_category
        ORDER BY metric_category
        """;

        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, timestamp);
            try (var rs = stmt.executeQuery()) {
                long totalMetrics = 0;
                long totalRecords = 0;
                long totalTime = 0;

                while (rs.next()) {
                    String category = rs.getString("metric_category");
                    long count = rs.getLong("metric_count");
                    long records = rs.getLong("total_records");
                    long time = rs.getLong("total_time_ns");

                    System.out.printf("%-20s: %,d метрик", category, count);
                    if (records > 0) {
                        System.out.printf(", %,d записей", records);
                    }
                    if (time > 0) {
                        System.out.printf(", %,d ns", time);
                    }
                    System.out.println();

                    totalMetrics += count;
                    totalRecords += records;
                    totalTime += time;
                }

                System.out.printf("%nИтого: %,d метрик, %,d записей, %,d ns общего времени%n",
                        totalMetrics, totalRecords, totalTime);
            }
        }

        // Общее количество сохраненных метрик
        String countSql = "SELECT COUNT(*) FROM performance_statistics WHERE test_timestamp = ?";
        try (var stmt = conn.prepareStatement(countSql)) {
            stmt.setTimestamp(1, timestamp);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("Всего сохранено метрик: %,d%n", rs.getLong(1));
                }
            }
        }
    }

    private long getSingleResult(Connection conn, String query) throws SQLException {
        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    private void loadIdsForPerformanceTests(Connection conn) throws SQLException {
        userIds.clear();
        functionIds.clear();
        operationIds.clear();

        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT id FROM users ORDER BY id")) {
            while (rs.next()) userIds.add(rs.getLong("id"));
        }

        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT id FROM functions ORDER BY id")) {
            while (rs.next()) functionIds.add(rs.getLong("id"));
        }

        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT id FROM operations ORDER BY id")) {
            while (rs.next()) operationIds.add(rs.getLong("id"));
        }

        System.out.printf("Загружено ID: %,d пользователей, %,d функций, %,d операций%n",
                userIds.size(), functionIds.size(), operationIds.size());

        // Сохраняем информацию о загруженных ID
        try (var conn2 = DatabaseConnection.getConnection()) {
            saveStatistic(conn2, new Timestamp(System.currentTimeMillis()),
                    "LOADED_IDS", "users_count", String.valueOf(userIds.size()), userIds.size(), userIds.size(), 0);
            saveStatistic(conn2, new Timestamp(System.currentTimeMillis()),
                    "LOADED_IDS", "functions_count", String.valueOf(functionIds.size()), functionIds.size(), functionIds.size(), 0);
            saveStatistic(conn2, new Timestamp(System.currentTimeMillis()),
                    "LOADED_IDS", "operations_count", String.valueOf(operationIds.size()), operationIds.size(), operationIds.size(), 0);
        }
    }
}