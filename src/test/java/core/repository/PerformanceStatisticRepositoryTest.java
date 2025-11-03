package core.repository;

import core.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.sql.Timestamp;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // Если используете H2 (рекомендуется для изолированного теста):
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate=OFF"
})
class PerformanceStatisticRepositoryTest {

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
    @DisplayName("Создание 10k записей и замер производительности (Framework)")
    void performFullPerformanceTest() {
        // === 1. Генерация данных ===
        long insertUsersTime = generateUsers();
        long insertFunctionsTime = generateFunctions();
        long insertTabulatedTime = generateTabulatedFunctions();
        long insertOperationsTime = generateOperations();

        // === 2. Тесты SELECT ===
        long selectUsersTime = testUserQueries();
        long selectFunctionsTime = testFunctionQueries();
        long selectTabulatedTime = testTabulatedQueries();
        long selectOperationsTime = testOperationQueries();

        // === 3. Сохранение результатов ===
        saveResult("INSERT", "users", insertUsersTime, TOTAL_RECORDS);
        saveResult("INSERT", "functions", insertFunctionsTime, TOTAL_RECORDS);
        saveResult("INSERT", "tabulated_functions", insertTabulatedTime, TOTAL_RECORDS);
        saveResult("INSERT", "operations", insertOperationsTime, TOTAL_RECORDS);

        saveResult("SELECT", "user_queries", selectUsersTime, 100);
        saveResult("SELECT", "function_queries", selectFunctionsTime, 50);
        saveResult("SELECT", "tabulated_queries", selectTabulatedTime, 20);
        saveResult("SELECT", "operation_queries", selectOperationsTime, 50);

        System.out.println("Все результаты сохранены в performance_statistics");

        List<PerformanceStatisticEntity> allStats = statisticRepository.findAll();
        generateMarkdownReport(allStats);
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private long generateUsers() {
        long start = System.nanoTime();
        List<UserEntity> users = new ArrayList<>(TOTAL_RECORDS);
        for (int i = 0; i < TOTAL_RECORDS; i++) {
            users.add(new UserEntity("user_" + i, "hash_" + i));
        }
        List<UserEntity> saved = userRepository.saveAll(users);
        userIds = saved.stream().map(UserEntity::getId).toList();
        return System.nanoTime() - start;
    }

    private long generateFunctions() {
        long start = System.nanoTime();
        List<FunctionEntity> functions = new ArrayList<>(TOTAL_RECORDS);
        for (int i = 0; i < TOTAL_RECORDS; i++) {
            UserEntity user = userRepository.findById(userIds.get(i % userIds.size())).orElseThrow();
            FunctionEntity.FunctionType type = (i % 2 == 0) ? FunctionEntity.FunctionType.ANALYTIC : FunctionEntity.FunctionType.TABULAR;
            functions.add(new FunctionEntity(user, type, "func_" + i, (i % 2 == 0) ? "x^2" : null));
        }
        List<FunctionEntity> saved = functionRepository.saveAll(functions);
        functionIds = saved.stream().map(FunctionEntity::getId).toList();
        return System.nanoTime() - start;
    }

    private long generateTabulatedFunctions() {
        long start = System.nanoTime();
        List<TabulatedFunctionEntity> points = new ArrayList<>();
        for (Long funcId : functionIds) {
            FunctionEntity func = functionRepository.findById(funcId).orElseThrow();
            points.add(new TabulatedFunctionEntity(func, 1.0, Math.sin(1.0)));
        }
        tabulatedFunctionRepository.saveAll(points);
        return System.nanoTime() - start;
    }

    private long generateOperations() {
        long start = System.nanoTime();
        List<OperationEntity> operations = new ArrayList<>();
        for (int i = 0; i < TOTAL_RECORDS; i++) {
            FunctionEntity func = functionRepository.findById(functionIds.get(i % functionIds.size())).orElseThrow();
            operations.add(new OperationEntity(func, random.nextInt(10) + 1));
        }
        operationRepository.saveAll(operations);
        return System.nanoTime() - start;
    }

    private long testUserQueries() {
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            userRepository.findById(userIds.get(random.nextInt(userIds.size())));
        }
        return System.nanoTime() - start;
    }

    private long testFunctionQueries() {
        long start = System.nanoTime();
        for (int i = 0; i < 50; i++) {
            functionRepository.findByUser_Id(userIds.get(random.nextInt(userIds.size())));
        }
        return System.nanoTime() - start;
    }

    private long testTabulatedQueries() {
        long start = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            tabulatedFunctionRepository.findByFunction_Id(functionIds.get(random.nextInt(functionIds.size())));
        }
        return System.nanoTime() - start;
    }

    private long testOperationQueries() {
        long start = System.nanoTime();
        List<OperationEntity> allOps = operationRepository.findAll();
        List<Long> opIds = allOps.stream().map(OperationEntity::getId).limit(50).toList();
        for (Long id : opIds) {
            operationRepository.findById(id);
        }
        return System.nanoTime() - start;
    }

    private void saveResult(String category, String name, long timeNs, int recordCount) {
        double timeMs = timeNs / 1_000_000_000.0; // наносекунды → миллисекунды (с дробной частью)
        String value = String.format("%.3f ms", timeMs);

        PerformanceStatisticEntity stat = new PerformanceStatisticEntity(
                testTimestamp, category, name, value, timeMs, recordCount, (long) timeMs
        );
        statisticRepository.save(stat);

        System.out.printf("[Framework] %s / %s: %s (%d записей)%n", category, name, value, recordCount);
    }



    private void generateMarkdownReport(List<PerformanceStatisticEntity> stats) {
        StringBuilder md = new StringBuilder();
        md.append("# Результаты производительности (Framework — Spring Data JPA)\n\n");
        md.append("| Категория | Метрика | Значение | Записей |\n");
        md.append("|----------|--------|----------|--------|\n");

        for (var stat : stats) {
            md.append(String.format("| %s | %s | %s | %d |\n",
                    stat.getMetricCategory(),
                    stat.getMetricName(),
                    stat.getMetricValue(),
                    stat.getRecordsCount()
            ));
        }

        try {
            java.nio.file.Files.writeString(
                    java.nio.file.Paths.get("framework-performance-results.md"),
                    md.toString()
            );
            System.out.println("✅ Отчёт сохранён: framework-performance-results.md");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}