package repository.test10k;

import model.dto.request.SearchFunctionRequestDTO;
import model.dto.response.SearchFunctionResponseDTO;
import repository.PerformanceLogger;

import java.sql.SQLException;
import java.util.logging.Logger;

public class PerformanceBenchmark {
    private static final Logger logger = Logger.getLogger(PerformanceBenchmark.class.getName());

    public static void main(String[] args) {

        FunctionService service = new FunctionService();
        PerformanceLogger perfLogger = new PerformanceLogger();

        runTest("full_scan", "Поиск всех функций (все записи)",
                new SearchFunctionRequestDTO(), service, perfLogger);

        SearchFunctionRequestDTO reqByName = new SearchFunctionRequestDTO();
        reqByName.setFunctionName("func_2");
        runTest("search_by_name_exact", "Поиск по точному имени: func_2", reqByName, service, perfLogger);

        SearchFunctionRequestDTO reqByPattern = new SearchFunctionRequestDTO();
        reqByPattern.setFunctionName("func_");
        runTest("search_by_name_pattern", "Поиск по шаблону: func_", reqByPattern, service, perfLogger);

        SearchFunctionRequestDTO reqByType = new SearchFunctionRequestDTO();
        reqByType.setTypeFunction("analytic");
        runTest("search_by_type_analytic", "Поиск по типу: analytic", reqByType, service, perfLogger);

        SearchFunctionRequestDTO reqByUser = new SearchFunctionRequestDTO();
        reqByUser.setUserName("user_2");
        runTest("search_by_user_existing", "Поиск по пользователю: user_2", reqByUser, service, perfLogger);

        SearchFunctionRequestDTO reqByUserNotFound = new SearchFunctionRequestDTO();
        reqByUserNotFound.setUserName("user_999");
        runTest("search_by_user_not_found", "Поиск по несуществующему пользователю: user_999", reqByUserNotFound, service, perfLogger);

        SearchFunctionRequestDTO reqSortAsc = new SearchFunctionRequestDTO();
        reqSortAsc.setSortBy("function_name");
        reqSortAsc.setSortOrder("asc");
        runTest("sort_by_name_asc", "Сортировка по имени (asc)", reqSortAsc, service, perfLogger);

        SearchFunctionRequestDTO reqSortDesc = new SearchFunctionRequestDTO();
        reqSortDesc.setSortBy("function_id");
        reqSortDesc.setSortOrder("desc");
        runTest("sort_by_id_desc", "Сортировка по ID (desc)", reqSortDesc, service, perfLogger);

        SearchFunctionRequestDTO reqByXVal = new SearchFunctionRequestDTO();
        reqByXVal.setXVal(1.0);
        runTest("search_by_x_val", "Поиск по x_val = 1.0", reqByXVal, service, perfLogger);

        SearchFunctionRequestDTO reqByYVal = new SearchFunctionRequestDTO();
        reqByYVal.setYVal(0.8636140983757885);
        runTest("search_by_y_val", "Поиск по y_val ≈ 0.8636", reqByYVal, service, perfLogger);

        SearchFunctionRequestDTO reqByOpType = new SearchFunctionRequestDTO();
        reqByOpType.setOperationsTypeId(6L);
        runTest("search_by_operations_type", "Поиск по operations_type_id = 6", reqByOpType, service, perfLogger);

        SearchFunctionRequestDTO reqCombined = new SearchFunctionRequestDTO();
        reqCombined.setUserName("user_0");
        reqCombined.setTypeFunction("analytic");
        reqCombined.setXVal(1.0);
        runTest("search_combined_user_type_x", "Комбинированный поиск: user_0 + analytic + x=1.0", reqCombined, service, perfLogger);

        logger.info("🏁 Все тесты завершены. Результаты записаны в таблицу performance_log.");
    }

    private static void runTest(
            String testName,
            String description,
            SearchFunctionRequestDTO request,
            FunctionService service,
            PerformanceLogger logger) {

        try {
            long start = System.currentTimeMillis();
            SearchFunctionResponseDTO result = service.searchFunctions(request);
            long end = System.currentTimeMillis();

            long timeMs = end - start;
            int count = result.getTotal();

            logger.logPerformance(testName, description, timeMs, count);
            System.out.printf("[%s] %d мс, найдено: %d записей%n", testName, timeMs, count);

        } catch (SQLException e) {
            System.err.println("Ошибка в тесте " + testName + ": " + e.getMessage());
        }
    }
}
/*
class CreatePerformanceLogTable {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/test_10k_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "1234";

    public static void main(String[] args) {
        //dropTable();

        createTable();
    }

    private static void dropTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String dropTableSQL = "DROP TABLE IF EXISTS performance_log;";

            try (Statement statement = connection.createStatement()) {
                statement.execute(dropTableSQL);
                System.out.println("Старая таблица 'performance_log' удалена (если существовала).");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении таблицы:");
            e.printStackTrace();
        }
    }

    private static void createTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String createTableSQL = """
                CREATE TABLE performance_log (
                    id SERIAL PRIMARY KEY,
                    test_name VARCHAR(255) NOT NULL,
                    duration_ms BIGINT NOT NULL, -- Изменено на ожидаемое имя столбца
                    records_found INTEGER,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    query_description VARCHAR(255)
                );
                """;

            try (Statement statement = connection.createStatement()) {
                statement.execute(createTableSQL);
                System.out.println("Таблица 'performance_log' создана с правильной структурой.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при подключении к базе данных или выполнении SQL:");
            e.printStackTrace();
        }
    }
}*/