// test/repository/test10k/PerformanceBenchmark.java

package repository.test10k;

import model.dto.request.SearchFunctionRequestDTO;
import model.dto.response.SearchFunctionResponseDTO;
import repository.PerformanceLogger;
import service.FunctionService;

import java.sql.SQLException;
import java.util.logging.Logger;

public class PerformanceBenchmark {
    private static final Logger logger = Logger.getLogger(PerformanceBenchmark.class.getName());

    public static void main(String[] args) {
        FunctionService service = new FunctionService();
        PerformanceLogger perfLogger = new PerformanceLogger();

        // === Тест 1: Поиск всех функций ===
        runTest("full_scan", "Поиск всех функций (все записи)",
                new SearchFunctionRequestDTO(), service, perfLogger);

        // === Тест 2: Поиск по имени функции ===
        SearchFunctionRequestDTO reqByName = new SearchFunctionRequestDTO();
        reqByName.setFunctionName("func_2");
        runTest("search_by_name_exact", "Поиск по точному имени: func_2", reqByName, service, perfLogger);

        // === Тест 3: Поиск по части имени ===
        SearchFunctionRequestDTO reqByPattern = new SearchFunctionRequestDTO();
        reqByPattern.setFunctionName("func_");
        runTest("search_by_name_pattern", "Поиск по шаблону: func_", reqByPattern, service, perfLogger);

        // === Тест 4: Поиск по типу функции ===
        SearchFunctionRequestDTO reqByType = new SearchFunctionRequestDTO();
        reqByType.setTypeFunction("analytic");
        runTest("search_by_type_analytic", "Поиск по типу: analytic", reqByType, service, perfLogger);

        // === Тест 5: Поиск по пользователю (существующий) ===
        SearchFunctionRequestDTO reqByUser = new SearchFunctionRequestDTO();
        reqByUser.setUserName("user_2");
        runTest("search_by_user_existing", "Поиск по пользователю: user_2", reqByUser, service, perfLogger);

        // === Тест 6: Поиск по пользователю (не существующий) ===
        SearchFunctionRequestDTO reqByUserNotFound = new SearchFunctionRequestDTO();
        reqByUserNotFound.setUserName("user_999");
        runTest("search_by_user_not_found", "Поиск по несуществующему пользователю: user_999", reqByUserNotFound, service, perfLogger);

        // === Тест 7: Сортировка по имени функции (asc) ===
        SearchFunctionRequestDTO reqSortAsc = new SearchFunctionRequestDTO();
        reqSortAsc.setSortBy("function_name");
        reqSortAsc.setSortOrder("asc");
        runTest("sort_by_name_asc", "Сортировка по имени (asc)", reqSortAsc, service, perfLogger);

        // === Тест 8: Сортировка по ID (desc) ===
        SearchFunctionRequestDTO reqSortDesc = new SearchFunctionRequestDTO();
        reqSortDesc.setSortBy("function_id");
        reqSortDesc.setSortOrder("desc");
        runTest("sort_by_id_desc", "Сортировка по ID (desc)", reqSortDesc, service, perfLogger);
        // === Тест 9: Поиск по значению x_val ===
        SearchFunctionRequestDTO reqByXVal = new SearchFunctionRequestDTO();
        reqByXVal.setXVal(1.0);
        runTest("search_by_x_val", "Поиск по x_val = 1.0", reqByXVal, service, perfLogger);

// === Тест 10: Поиск по значению y_val ===
        SearchFunctionRequestDTO reqByYVal = new SearchFunctionRequestDTO();
        reqByYVal.setYVal(0.8636140983757885); // точное значение из таблицы
        runTest("search_by_y_val", "Поиск по y_val ≈ 0.8636", reqByYVal, service, perfLogger);

// === Тест 11: Поиск по типу операции ===
        SearchFunctionRequestDTO reqByOpType = new SearchFunctionRequestDTO();
        reqByOpType.setOperationsTypeId(6L); // operations_type_id = 6
        runTest("search_by_operations_type", "Поиск по operations_type_id = 6", reqByOpType, service, perfLogger);

// === Тест 12: Комбинированный поиск: user + type + x_val ===
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
            System.out.printf("[⏱️ %s] %d мс, найдено: %d записей%n", testName, timeMs, count);

        } catch (SQLException e) {
            System.err.println("❌ Ошибка в тесте " + testName + ": " + e.getMessage());
        }
    }
}