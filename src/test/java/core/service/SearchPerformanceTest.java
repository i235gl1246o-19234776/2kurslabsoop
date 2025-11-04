package core.service;

import core.entity.*;
import core.repository.*;
import core.service.SingleSearchService;
import core.service.MultipleSearchService;
import core.service.SortedSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate=OFF"
})
@ComponentScan(basePackages = "core.service")
public class SearchPerformanceTest {

    // === Репозитории для генерации данных ===
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FunctionRepository functionRepository;
    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;
    @Autowired
    private OperationRepository operationRepository;
    @Autowired
    private PerformanceStatisticRepository statisticRepository;

    // === Сервисы поиска ===
    @Autowired
    private SingleSearchService singleSearchService;
    @Autowired
    private MultipleSearchService multipleSearchService;
    @Autowired
    private SortedSearchService sortedSearchService;

    private static final int TOTAL_RECORDS = 10_000;
    private final Random random = new Random();

    private List<Long> userIds = new ArrayList<>();
    private List<Long> functionIds = new ArrayList<>();

    private Timestamp testTimestamp;

    @BeforeEach
    void initTimestamp() {
        testTimestamp = new Timestamp(System.currentTimeMillis());
    }

    @Test
    @DisplayName("Тест производительности трёх видов поиска (через сервисы)")
    void performSearchPerformanceTest() {
        // === 1. Генерация данных ===
        generateData();

        // === 2. Тесты поиска через сервисы ===
        long singleSearchTime = testSingleSearch();
        long multipleSearchTime = testMultipleSearch();
        long sortedSearchTime = testSortedSearch();

        // === 3. Сохранение результатов ===
        saveResult("SEARCH", "single_find_by_id", singleSearchTime, 100);
        saveResult("SEARCH", "multiple_functions", multipleSearchTime, 100);
        saveResult("SEARCH", "sorted_functions", sortedSearchTime, 100);

        System.out.println("✅ Все результаты поиска сохранены в performance_statistics");

        // === 4. Экспорт в Markdown ===
        List<PerformanceStatisticEntity> allStats = statisticRepository.findAll();
        generateMarkdownReport(allStats);
    }

    // === ГЕНЕРАЦИЯ ДАННЫХ ===
    private void generateData() {
        System.out.println("Генерация 10 000 записей...");

        // Пользователи
        List<UserEntity> users = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            users.add(new UserEntity("user_" + i, "hash_" + i));
        }
        List<UserEntity> savedUsers = userRepository.saveAll(users);
        userIds = savedUsers.stream().map(UserEntity::getId).collect(Collectors.toList());

        // Функции (100 на пользователя → 10 000)
        List<FunctionEntity> functions = new ArrayList<>(TOTAL_RECORDS);
        for (int i = 0; i < TOTAL_RECORDS; i++) {
            Long userId = userIds.get(i % userIds.size());
            UserEntity user = userRepository.findById(userId).orElseThrow();
            FunctionEntity.FunctionType type = (i % 2 == 0) ? FunctionEntity.FunctionType.ANALYTIC : FunctionEntity.FunctionType.TABULAR;
            functions.add(new FunctionEntity(user, type, "func_" + i, (i % 2 == 0) ? "x^2" : null));
        }
        List<FunctionEntity> savedFunctions = functionRepository.saveAll(functions);
        functionIds = savedFunctions.stream().map(FunctionEntity::getId).collect(Collectors.toList());

        // Точки и операции (минимум для сортировки точек)
        for (Long funcId : functionIds.subList(0, Math.min(1000, functionIds.size()))) {
            FunctionEntity func = functionRepository.findById(funcId).orElseThrow();
            for (int i = 0; i < 10; i++) {
                tabulatedFunctionRepository.save(new TabulatedFunctionEntity(func, (double) i, Math.sin(i)));
            }
            operationRepository.save(new OperationEntity(func, 1));
        }

        System.out.println("✅ Данные сгенерированы");
    }

    private long testSingleSearch() {
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            Long id = userIds.get(random.nextInt(userIds.size()));
            singleSearchService.findUserById(id);
        }
        return System.nanoTime() - start;
    }

    private long testMultipleSearch() {
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            Long id = userIds.get(random.nextInt(userIds.size()));
            multipleSearchService.findFunctionsByUser(id);
        }
        return System.nanoTime() - start;
    }

    private long testSortedSearch() {
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            Long id = userIds.get(random.nextInt(userIds.size()));
            sortedSearchService.findFunctionsByUserSortedByName(id);
        }
        return System.nanoTime() - start;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private void saveResult(String category, String name, long timeNs, int recordCount) {
        double timeMs = timeNs / 1_000_000_000.0; // ← ИСПРАВЛЕНО: наносекунды → миллисекунды
        String value = String.format("%.3f ms", timeMs);

        PerformanceStatisticEntity stat = new PerformanceStatisticEntity(
                testTimestamp, category, name, value, timeMs, recordCount, (long) timeMs
        );
        statisticRepository.save(stat);

        System.out.printf("[Search] %s / %s: %s (%d записей)%n", category, name, value, recordCount);
    }

    private void generateMarkdownReport(List<PerformanceStatisticEntity> stats) {
        StringBuilder md = new StringBuilder();
        md.append("# Производительность трёх видов поиска (через сервисы)\n\n");
        md.append("| Категория | Метрика | Значение | Записей |\n");
        md.append("|----------|--------|----------|--------|\n");

        for (var stat : stats) {
            if ("SEARCH".equals(stat.getMetricCategory())) {
                md.append(String.format("| %s | %s | %s | %d |\n",
                        stat.getMetricCategory(),
                        stat.getMetricName(),
                        stat.getMetricValue(),
                        stat.getRecordsCount()
                ));
            }
        }

        try {
            java.nio.file.Files.writeString(
                    java.nio.file.Paths.get("search-via-services-performance.md"),
                    md.toString()
            );
            System.out.println("✅ Отчёт сохранён: search-via-services-performance.md");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

