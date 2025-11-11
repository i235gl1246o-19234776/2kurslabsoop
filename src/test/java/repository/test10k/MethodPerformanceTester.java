package repository.test10k;

import repository.dao.FunctionRepository;
import repository.dao.OperationRepository;
import repository.dao.TabulatedFunctionRepository;
import repository.dao.UserRepository;
import model.entity.Function;
import model.entity.Operation;
import model.entity.TabulatedFunction;
import model.entity.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Logger;

public class MethodPerformanceTester {
    private static final Logger logger = Logger.getLogger(MethodPerformanceTester.class.getName());

    private static final String TEST_DB_NAME = "test_10k_db";
    private static final String ADMIN_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    private final FunctionRepository functionRepository;
    private final OperationRepository operationRepository;
    private final TabulatedFunctionRepository tabulatedFunctionRepository;
    private final UserRepository userRepository;
    private final Map<String, List<Long>> performanceResults;
    private final Map<String, Long> simpleExecutionTimes;
    static Random random = null;

    private final String[] VALID_FUNCTION_TYPES = {"analytic", "tabular"};
    private final String[] NAME_PATTERNS = {"func", "test", "math", "user", "calc", "equation"};

    public MethodPerformanceTester() {
        this.functionRepository = new FunctionRepository();
        this.operationRepository = new OperationRepository();
        this.tabulatedFunctionRepository = new TabulatedFunctionRepository();
        this.userRepository = new UserRepository();
        this.performanceResults = new HashMap<>();
        this.simpleExecutionTimes = new HashMap<>();
        this.random = new Random();
    }

    public void initializeTestDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(ADMIN_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String checkDbSql = "SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB_NAME + "'";
            var resultSet = stmt.executeQuery(checkDbSql);

            if (!resultSet.next()) {
                String createDbSql = "CREATE DATABASE " + TEST_DB_NAME;
                stmt.executeUpdate(createDbSql);
                logger.info("База данных " + TEST_DB_NAME + " создана");
            } else {
                logger.info("База данных " + TEST_DB_NAME + " уже существует");
            }
        }

        functionRepository.createPerformanceTable();
    }

    public void testAllMethodsMultipleIterations(int iterations) throws SQLException {
        logger.info("Начало массового тестирования производительности методов (" + iterations + " итераций)");

        for (int i = 0; i < iterations; i++) {
            testFindByUserId();
            testFindByName();
            testFindByType();
            testFindById();
            testSearch();
            testCreateFunction();
            testUpdateFunction();
            testDeleteFunction();

            testCreateOperation();
            testFindOperationById();
            testUpdateOperation();
            testDeleteOperation();
            testDeleteAllOperations();

            testCreateTabulatedFunction();
            testFindAllTabulatedFunctionsByFunctionId();
            testFindTabulatedFunctionByX();
            testFindTabulatedFunctionsBetween();
            testUpdateTabulatedFunction();
            testDeleteTabulatedFunction();
            testDeleteAllTabulatedFunctions();

            testCreateUser();
            testFindUserById();
            testFindUserByName();
            testUpdateUser();
            testAuthenticateUser();
            testCheckUserNameExists();
            testDeleteUser();
        }

        printPerformanceSummary();
        functionRepository.printPerformanceStats();
    }


    private void testFindByUserId() {
        long startTime = System.nanoTime();
        try {
            Long userId = (long) random.nextInt(100) + 1;
            functionRepository.findByUserId(userId);
            recordPerformance("function_findByUserId", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("function_findByUserId", System.nanoTime() - startTime);
            logger.warning("Ошибка в testFindByUserId: " + e.getMessage());
        }
    }

    private void testFindByName() {
        long startTime = System.nanoTime();
        try {
            Long userId = (long) random.nextInt(100) + 1;
            String pattern = NAME_PATTERNS[random.nextInt(NAME_PATTERNS.length)];
            functionRepository.findByName(userId, pattern);
            recordPerformance("function_findByName", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("function_findByName", System.nanoTime() - startTime);
            logger.warning("Ошибка в testFindByName: " + e.getMessage());
        }
    }

    private void testFindByType() {
        long startTime = System.nanoTime();
        try {
            Long userId = (long) random.nextInt(100) + 1;
            String type = VALID_FUNCTION_TYPES[random.nextInt(VALID_FUNCTION_TYPES.length)];
            functionRepository.findByType(userId, type);
            recordPerformance("function_findByType", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("function_findByType", System.nanoTime() - startTime);
            logger.warning("Ошибка в testFindByType: " + e.getMessage());
        }
    }

    private void testFindById() {
        long startTime = System.nanoTime();
        try {
            Long id = (long) random.nextInt(1000) + 1;
            Long userId = (long) random.nextInt(100) + 1;
            functionRepository.findById(id, userId);
            recordPerformance("function_findById", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("function_findById", System.nanoTime() - startTime);
            logger.warning("Ошибка в testFindById: " + e.getMessage());
        }
    }

    private void testSearch() {
        long startTime = System.nanoTime();
        try {
            Long userId = (long) random.nextInt(100) + 1;
            String[] searchNamePatterns = {"func", "test", "", "user"};
            String[] searchTypes = {"analytic", "tabular", "", "exponential"}; // Используем только валидные типы

            String sortBy = "function_id";
            String sortOrder = random.nextBoolean() ? "asc" : "desc";

            functionRepository.search(
                    userId,
                    null,
                    searchNamePatterns[random.nextInt(searchNamePatterns.length)],
                    searchTypes[random.nextInt(searchTypes.length)],
                    null,
                    null,
                    null,
                    sortBy,
                    sortOrder
            );
            recordPerformance("function_search", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("function_search", System.nanoTime() - startTime);
            logger.warning("Ошибка в testSearch: " + e.getMessage());
        }
    }

    private void testCreateFunction() {
        long startTime = System.nanoTime();
        try {
            Function function = createRandomFunction();
            functionRepository.createFunction(function);
            recordPerformance("function_createFunction", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("function_createFunction", System.nanoTime() - startTime);
            logger.warning("Ошибка в testCreateFunction: " + e.getMessage());
        }
    }

    private void testUpdateFunction() {
        long startTime = System.nanoTime();
        try {
            Function originalFunction = createRandomFunction();
            Long functionId = functionRepository.createFunction(originalFunction);

            if (functionId != null) {
                Function updatedFunction = createRandomFunction();
                updatedFunction.setId(functionId);
                updatedFunction.setUserId(originalFunction.getUserId());
                functionRepository.updateFunction(updatedFunction);
            }
            recordPerformance("function_updateFunction", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("function_updateFunction", System.nanoTime() - startTime);
            logger.warning("Ошибка в testUpdateFunction: " + e.getMessage());
        }
    }

    private void testDeleteFunction() {
        long startTime = System.nanoTime();
        try {
            Function function = createRandomFunction();
            Long functionId = functionRepository.createFunction(function);

            if (functionId != null) {
                functionRepository.deleteFunction(functionId, function.getUserId());
            }
            recordPerformance("function_deleteFunction", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("function_deleteFunction", System.nanoTime() - startTime);
            logger.warning("Ошибка в testDeleteFunction: " + e.getMessage());
        }
    }


    private void testCreateOperation() {
        long startTime = System.nanoTime();
        try {
            Long functionId = (long) (random.nextInt(100) + 1);
            Operation operation = new Operation();
            operation.setFunctionId(functionId);
            operation.setOperationsTypeId(random.nextInt(10) + 1);
            operationRepository.createOperation(operation);
            recordPerformance("operation_createOperation", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("operation_createOperation", System.nanoTime() - startTime);
            logger.warning("Ошибка в testCreateOperation: " + e.getMessage());
        }
    }

    private void testFindOperationById() {
        long startTime = System.nanoTime();
        try {
            Long opId = (long) (random.nextInt(1000) + 1);
            Long functionId = (long) (random.nextInt(100) + 1);
            operationRepository.findById(opId, functionId);
            recordPerformance("operation_findById", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("operation_findById", System.nanoTime() - startTime);
            logger.warning("Ошибка в testFindOperationById: " + e.getMessage());
        }
    }

    private void testUpdateOperation() {
        long startTime = System.nanoTime();
        try {
            Operation operation = new Operation();
            operation.setId((long) (random.nextInt(1000) + 1));
            operation.setFunctionId((long) (random.nextInt(100) + 1));
            operation.setOperationsTypeId(random.nextInt(10) + 1);
            operationRepository.updateOperation(operation);
            recordPerformance("operation_updateOperation", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("operation_updateOperation", System.nanoTime() - startTime);
            logger.warning("Ошибка в testUpdateOperation: " + e.getMessage());
        }
    }

    private void testDeleteOperation() {
        long startTime = System.nanoTime();
        try {
            Long opId = (long) (random.nextInt(1000) + 1);
            Long functionId = (long) (random.nextInt(100) + 1);
            operationRepository.deleteOperation(opId, functionId);
            recordPerformance("operation_deleteOperation", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("operation_deleteOperation", System.nanoTime() - startTime);
            logger.warning("Ошибка в testDeleteOperation: " + e.getMessage());
        }
    }

    private void testDeleteAllOperations() {
        long startTime = System.nanoTime();
        try {
            Long functionId = (long) (random.nextInt(100) + 1);
            operationRepository.deleteAllOperations(functionId);
            recordPerformance("operation_deleteAllOperations", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("operation_deleteAllOperations", System.nanoTime() - startTime);
            logger.warning("Ошибка в testDeleteAllOperations: " + e.getMessage());
        }
    }

    private void testCreateTabulatedFunction() {
        long startTime = System.nanoTime();
        try {
            Long functionId = (long) (random.nextInt(100) + 1);
            TabulatedFunction tf = new TabulatedFunction();
            tf.setFunctionId(functionId);
            tf.setXVal(random.nextDouble() * 100);
            tf.setYVal(random.nextDouble() * 100);
            tabulatedFunctionRepository.createTabulatedFunction(tf);
            recordPerformance("tabulated_createTabulatedFunction", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("tabulated_createTabulatedFunction", System.nanoTime() - startTime);
            logger.warning("Ошибка в testCreateTabulatedFunction: " + e.getMessage());
        }
    }

    private void testFindAllTabulatedFunctionsByFunctionId() {
        long startTime = System.nanoTime();
        try {
            Long functionId = (long) (random.nextInt(100) + 1);
            tabulatedFunctionRepository.findAllByFunctionId(functionId);
            recordPerformance("tabulated_findAllByFunctionId", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("tabulated_findAllByFunctionId", System.nanoTime() - startTime);
            logger.warning("Ошибка в testFindAllTabulatedFunctionsByFunctionId: " + e.getMessage());
        }
    }

    private void testFindTabulatedFunctionByX() {
        long startTime = System.nanoTime();
        try {
            Long functionId = (long) (random.nextInt(100) + 1);
            Double xVal = random.nextDouble() * 100;
            tabulatedFunctionRepository.findByXValue(functionId, xVal);
            recordPerformance("tabulated_findByXValue", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("tabulated_findByXValue", System.nanoTime() - startTime);
            logger.warning("Ошибка в testFindTabulatedFunctionByX: " + e.getMessage());
        }
    }

    private void testFindTabulatedFunctionsBetween() {
        long startTime = System.nanoTime();
        try {
            Long functionId = (long) (random.nextInt(100) + 1);
            Double xMin = random.nextDouble() * 50;
            Double xMax = xMin + random.nextDouble() * 50;
            tabulatedFunctionRepository.findBetweenXValues(functionId, xMin, xMax);
            recordPerformance("tabulated_findBetweenXValues", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("tabulated_findBetweenXValues", System.nanoTime() - startTime);
            logger.warning("Ошибка в testFindTabulatedFunctionsBetween: " + e.getMessage());
        }
    }

    private void testUpdateTabulatedFunction() {
        long startTime = System.nanoTime();
        try {
            TabulatedFunction tf = new TabulatedFunction();
            tf.setId((long) (random.nextInt(1000) + 1));
            tf.setFunctionId((long) (random.nextInt(100) + 1));
            tf.setXVal(random.nextDouble() * 100);
            tf.setYVal(random.nextDouble() * 100);
            tabulatedFunctionRepository.updateTabulatedFunction(tf);
            recordPerformance("tabulated_updateTabulatedFunction", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("tabulated_updateTabulatedFunction", System.nanoTime() - startTime);
            logger.warning("Ошибка в testUpdateTabulatedFunction: " + e.getMessage());
        }
    }

    private void testDeleteTabulatedFunction() {
        long startTime = System.nanoTime();
        try {
            Long tfId = (long) (random.nextInt(1000) + 1);
            tabulatedFunctionRepository.deleteTabulatedFunction(tfId);
            recordPerformance("tabulated_deleteTabulatedFunction", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("tabulated_deleteTabulatedFunction", System.nanoTime() - startTime);
            logger.warning("Ошибка в testDeleteTabulatedFunction: " + e.getMessage());
        }
    }

    private void testDeleteAllTabulatedFunctions() {
        long startTime = System.nanoTime();
        try {
            Long functionId = (long) (random.nextInt(100) + 1);
            tabulatedFunctionRepository.deleteAllTabulatedFunctions(functionId);
            recordPerformance("tabulated_deleteAllTabulatedFunctions", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("tabulated_deleteAllTabulatedFunctions", System.nanoTime() - startTime);
            logger.warning("Ошибка в testDeleteAllTabulatedFunctions: " + e.getMessage());
        }
    }

    private void testCreateUser() {
        long startTime = System.nanoTime();
        try {
            User user = new User("test_user_" + System.currentTimeMillis(), "hash_" + System.currentTimeMillis());
            userRepository.createUser(user);
            recordPerformance("user_createUser", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("user_createUser", System.nanoTime() - startTime);
            logger.warning("Ошибка в testCreateUser: " + e.getMessage());
        }
    }

    private void testFindUserById() {
        long startTime = System.nanoTime();
        try {
            Long userId = (long) (random.nextInt(100) + 1);
            userRepository.findById(userId);
            recordPerformance("user_findById", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("user_findById", System.nanoTime() - startTime);
            logger.warning("Ошибка в testFindUserById: " + e.getMessage());
        }
    }

    private void testFindUserByName() {
        long startTime = System.nanoTime();
        try {
            String name = NAME_PATTERNS[random.nextInt(NAME_PATTERNS.length)] + "_" + random.nextInt(1000);
            userRepository.findByName(name);
            recordPerformance("user_findByName", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("user_findByName", System.nanoTime() - startTime);
            logger.warning("Ошибка в testFindUserByName: " + e.getMessage());
        }
    }

    private void testUpdateUser() {
        long startTime = System.nanoTime();
        try {
            User user = new User();
            user.setId((long) (random.nextInt(100) + 1));
            user.setName("updated_name_" + System.currentTimeMillis());
            user.setPasswordHash("updated_hash");
            userRepository.updateUser(user);
            recordPerformance("user_updateUser", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("user_updateUser", System.nanoTime() - startTime);
            logger.warning("Ошибка в testUpdateUser: " + e.getMessage());
        }
    }

    private void testAuthenticateUser() {
        long startTime = System.nanoTime();
        try {
            String name = NAME_PATTERNS[random.nextInt(NAME_PATTERNS.length)] + "_" + random.nextInt(1000);
            String passwordHash = "hash_" + random.nextInt(1000);
            userRepository.authenticateUser(name, passwordHash);
            recordPerformance("user_authenticateUser", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("user_authenticateUser", System.nanoTime() - startTime);
            logger.warning("Ошибка в testAuthenticateUser: " + e.getMessage());
        }
    }

    private void testCheckUserNameExists() {
        long startTime = System.nanoTime();
        try {
            String name = NAME_PATTERNS[random.nextInt(NAME_PATTERNS.length)] + "_" + random.nextInt(1000);
            userRepository.userNameExists(name);
            recordPerformance("user_userNameExists", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("user_userNameExists", System.nanoTime() - startTime);
            logger.warning("Ошибка в testCheckUserNameExists: " + e.getMessage());
        }
    }

    private void testDeleteUser() {
        long startTime = System.nanoTime();
        try {
            Long userId = (long) (random.nextInt(100) + 1);
            userRepository.deleteUser(userId);
            recordPerformance("user_deleteUser", System.nanoTime() - startTime);
        } catch (SQLException e) {
            recordPerformance("user_deleteUser", System.nanoTime() - startTime);
            logger.warning("Ошибка в testDeleteUser: " + e.getMessage());
        }
    }

    private Function createRandomFunction() {
        Function function = new Function();
        function.setUserId((long) random.nextInt(100) + 1);
        function.setFunctionName("test_func_" + System.currentTimeMillis() + "_" + random.nextInt(1000));
        function.setFunctionExpression(generateRandomExpression());
        function.setTypeFunction(VALID_FUNCTION_TYPES[random.nextInt(VALID_FUNCTION_TYPES.length)]);
        return function;
    }

    private String generateRandomExpression() {
        String[] expressions = {
                "x + " + (random.nextInt(10) + 1),
                "x - " + (random.nextInt(10) + 1),
                "x * " + (random.nextInt(5) + 1),
                "x^2 + " + (random.nextInt(10) + 1),
                "x^3 - " + (random.nextInt(10) + 1),
                "sin(x)",
                "cos(x)",
                "log(x + " + (random.nextInt(5) + 1) + ")",
                "exp(x)",
                "sqrt(x + " + (random.nextInt(10) + 1) + ")"
        };
        return expressions[random.nextInt(expressions.length)];
    }

    private void recordPerformance(String methodName, long durationNanos) {
        performanceResults.computeIfAbsent(methodName, k -> new ArrayList<>()).add(durationNanos);
    }

    public void testMethod(String methodName, TestMethod testMethod) {
        long startTime = System.nanoTime();
        try {
            testMethod.execute();
            long duration = System.nanoTime() - startTime;
            simpleExecutionTimes.put(methodName, duration);
            logger.info(String.format("Метод %s выполнен за %.2f мс", methodName, duration / 1_000_000.0));
        } catch (Exception e) {
            long duration = System.nanoTime() - startTime;
            simpleExecutionTimes.put(methodName + "_ERROR", duration);
            logger.warning(String.format("Метод %s завершился с ошибкой за %.2f мс: %s",
                    methodName, duration / 1_000_000.0, e.getMessage()));
        }
    }

    public void printSimpleResults() {
        logger.info("=== РЕЗУЛЬТАТЫ ПРОСТОГО ТЕСТИРОВАНИЯ ===");
        simpleExecutionTimes.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue())
                .forEach(entry -> {
                    logger.info(String.format("%s: %.2f мс", entry.getKey(), entry.getValue() / 1_000_000.0));
                });
    }

    public void printPerformanceSummary() {
        logger.info("=== СВОДКА МАССОВОГО ТЕСТИРОВАНИЯ ===");

        performanceResults.entrySet().stream()
                .sorted((e1, e2) -> {
                    double avg1 = e1.getValue().stream().mapToLong(Long::longValue).average().orElse(0);
                    double avg2 = e2.getValue().stream().mapToLong(Long::longValue).average().orElse(0);
                    return Double.compare(avg2, avg1); // Сортировка по убыванию среднего времени
                })
                .forEach(entry -> {
                    String methodName = entry.getKey();
                    List<Long> times = entry.getValue();

                    if (times.isEmpty()) return;

                    long totalNanos = times.stream().mapToLong(Long::longValue).sum();
                    double avgNanos = times.stream().mapToLong(Long::longValue).average().orElse(0);
                    long minNanos = times.stream().mapToLong(Long::longValue).min().orElse(0);
                    long maxNanos = times.stream().mapToLong(Long::longValue).max().orElse(0);

                    double totalMs = totalNanos / 1_000_000.0;
                    double avgMs = avgNanos / 1_000_000.0;
                    double minMs = minNanos / 1_000_000.0;
                    double maxMs = maxNanos / 1_000_000.0;

                    logger.info(String.format(
                            "Метод: %-30s | Вызовов: %3d | Общее время: %8.2f мс | Среднее: %6.2f мс | Min: %6.2f мс | Max: %6.2f мс",
                            methodName,
                            times.size(),
                            totalMs,
                            avgMs,
                            minMs,
                            maxMs
                    ));
                });
    }

    public Map<String, List<Long>> getPerformanceResults() {
        return Collections.unmodifiableMap(performanceResults);
    }

    public Map<String, Long> getSimpleExecutionTimes() {
        return Collections.unmodifiableMap(simpleExecutionTimes);
    }

    @FunctionalInterface
    public interface TestMethod {
        void execute() throws SQLException;
    }

    public static void main(String[] args) {
        MethodPerformanceTester tester = new MethodPerformanceTester();

        try {
            tester.initializeTestDatabase();

            logger.info("=== ЗАПУСК ПРОСТОГО ТЕСТИРОВАНИЯ ОТДЕЛЬНЫХ МЕТОДОВ ===");

            tester.testMethod("createFunction", () -> {
                Function function = new Function();
                function.setUserId(1L);
                function.setFunctionName("test_function_" + System.currentTimeMillis());
                function.setFunctionExpression("x^2 + 2*x + 1");
                function.setTypeFunction("analytic"); // Используем допустимый тип
                tester.functionRepository.createFunction(function);
            });

            tester.testMethod("findByUserId", () -> {
                tester.functionRepository.findByUserId(1L);
            });

            tester.testMethod("findByName", () -> {
                tester.functionRepository.findByName(1L, "test");
            });

            tester.testMethod("findByType", () -> {
                tester.functionRepository.findByType(1L, "analytic");
            });

            tester.testMethod("findById", () -> {
                Function function = new Function();
                function.setUserId(1L);
                function.setFunctionName("find_test_" + System.currentTimeMillis());
                function.setFunctionExpression("x + 5");
                function.setTypeFunction("analytic");
                Long functionId = tester.functionRepository.createFunction(function);

                if (functionId != null) {
                    tester.functionRepository.findById(functionId, 1L);
                }
            });

            tester.testMethod("search_basic", () -> {
                tester.functionRepository.search(1L, null, "function", null, null, null, null, "function_id", "asc");
            });

            tester.testMethod("search_with_type", () -> {
                tester.functionRepository.search(1L, null, null, "analytic", null, null, null, "function_name", "asc");
            });

            tester.testMethod("search_complex", () -> {
                tester.functionRepository.search(null, "user", "test", "analytic", 1.0, 2.0, 1L, "type_function", "desc");
            });

            tester.testMethod("updateFunction", () -> {
                Function function = new Function();
                function.setUserId(1L);
                function.setFunctionName("update_test_" + System.currentTimeMillis());
                function.setFunctionExpression("x + 1");
                function.setTypeFunction("analytic");
                Long functionId = tester.functionRepository.createFunction(function);

                if (functionId != null) {
                    Function updatedFunction = new Function();
                    updatedFunction.setId(functionId);
                    updatedFunction.setUserId(1L);
                    updatedFunction.setFunctionName("updated_" + functionId);
                    updatedFunction.setFunctionExpression("x + 2");
                    updatedFunction.setTypeFunction("analytic");
                    tester.functionRepository.updateFunction(updatedFunction);
                }
            });

            tester.testMethod("deleteFunction", () -> {
                Function function = new Function();
                function.setUserId(1L);
                function.setFunctionName("delete_test_" + System.currentTimeMillis());
                function.setFunctionExpression("x * 2");
                function.setTypeFunction("analytic");
                Long functionId = tester.functionRepository.createFunction(function);

                if (functionId != null) {
                    tester.functionRepository.deleteFunction(functionId, 1L);
                }
            });

            tester.testMethod("createUser", () -> {
                User user = new User("test_user_" + System.currentTimeMillis(), "hash_" + System.currentTimeMillis());
                tester.userRepository.createUser(user);
            });

            tester.testMethod("findUserById", () -> {
                tester.userRepository.findById(1L);
            });

            tester.testMethod("findUserByName", () -> {
                tester.userRepository.findByName("user_1");
            });

            tester.testMethod("updateUser", () -> {
                User user = new User();
                user.setId(1L);
                user.setName("updated_user_1");
                user.setPasswordHash("new_hash_1");
                tester.userRepository.updateUser(user);
            });

            tester.testMethod("deleteUser", () -> {
                User user = new User("temp_user_" + System.currentTimeMillis(), "hash_" + System.currentTimeMillis());
                Long userId = tester.userRepository.createUser(user);

                if (userId != null) {
                    tester.userRepository.deleteUser(userId);
                }
            });

            tester.testMethod("createOperation", () -> {
                Operation op = new Operation((long) (Math.random() * 100 + 1), random.nextInt(10) + 1);
                tester.operationRepository.createOperation(op);
            });

            tester.testMethod("createTabulatedFunction", () -> {
                TabulatedFunction tf = new TabulatedFunction((long) (Math.random() * 100 + 1), 1.0, 2.0);
                tester.tabulatedFunctionRepository.createTabulatedFunction(tf);
            });

            tester.testMethod("createPerformanceTable", () -> {
                tester.functionRepository.createPerformanceTable();
            });

            tester.testMethod("printPerformanceStats", () -> {
                tester.functionRepository.printPerformanceStats();
            });

            tester.printSimpleResults();

            logger.info("\n=== ЗАПУСК МАССОВОГО ТЕСТИРОВАНИЯ (5 ИТЕРАЦИЙ) ===");

            tester.testAllMethodsMultipleIterations(5);

        } catch (Exception e) {
            logger.severe("Ошибка при тестировании: " + e.getMessage());
            e.printStackTrace();
        }
    }
}