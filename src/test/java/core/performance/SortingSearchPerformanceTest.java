package core.performance;

import core.entity.FunctionEntity;
import core.entity.OperationEntity;
import core.entity.TabulatedFunctionEntity;
import core.entity.UserEntity;
import core.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SortingSearchPerformanceTest {

    private static final int ITERATIONS = 100;
    private static final String CSV_FILE = "sort-performance.csv";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Autowired
    private OperationRepository operationRepository;

    private static boolean dataPrepared = false;
    private static Long existingUserId;
    private static Long existingFunctionId;
    private static String existingUserName;
    private static Double testXVal;
    private static Double testYVal;

    @BeforeAll
    static void setupCsv() throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(CSV_FILE))) {
            w.write("Метод,\"Время, мс\"");
            w.newLine();
        }
    }

    private void appendResult(String method, double avgMs) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(CSV_FILE, true))) {
            w.write(String.format(Locale.US, "%s,%.2f", method, avgMs));
            w.newLine();
        } catch (IOException ignore) {}
    }

    private void prepareDataOnce() {
        if (dataPrepared) return;

        operationRepository.deleteAllInBatch();
        tabulatedFunctionRepository.deleteAllInBatch();
        functionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        Random rand = new Random(42); // фиксированный seed для воспроизводимости
        for (int i = 0; i < 10_000; i++) { // Изменено с 500 на 10_000
            UserEntity user = new UserEntity("user_sort_" + i, "hash_" + i);
            user = userRepository.save(user);

            // 2 функции на пользователя
            for (int j = 0; j < 2; j++) {
                boolean isTabular = (i + j) % 2 == 0;
                FunctionEntity func = new FunctionEntity(
                        user,
                        isTabular ? FunctionEntity.FunctionType.tabular : FunctionEntity.FunctionType.analytic,
                        "func_" + i + "_" + j,
                        isTabular ? null : "x^2"
                );
                func = functionRepository.save(func);

                if (isTabular) {
                    for (int k = 0; k < 10; k++) {
                        double x = rand.nextDouble() * 1000;
                        double y = x * x;
                        if (i == 0 && j == 0 && k == 0) {
                            testXVal = x;
                            testYVal = y;
                        }
                        tabulatedFunctionRepository.save(new TabulatedFunctionEntity(func, x, y));
                    }
                } else {
                    operationRepository.save(new OperationEntity(func, 1));
                }

                if (i == 0 && j == 0) {
                    existingFunctionId = func.getId();
                }
            }

            if (i == 0) {
                existingUserId = user.getId();
                existingUserName = user.getName();
            }
        }

        dataPrepared = true;
    }

    @Test
    @Order(1)
    void full_scan() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            functionRepository.findAll();
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("full_scan", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(2)
    void search_by_name_exact() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            userRepository.findByName(existingUserName);
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("search_by_name_exact", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(3)
    void search_by_name_pattern() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            List<UserEntity> all = userRepository.findAll();
            all.stream().filter(u -> u.getName().contains("sort_10")).findFirst();
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("search_by_name_pattern", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(4)
    void search_by_type_analytic() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            List<FunctionEntity> all = functionRepository.findAll();
            all.stream().filter(f -> f.getTypeFunction() == FunctionEntity.FunctionType.analytic).collect(Collectors.toList());
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("search_by_type_analytic", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(5)
    void search_by_user_existing() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            functionRepository.findByUser_Id(existingUserId);
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("search_by_user_existing", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(6)
    void search_by_user_not_found() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            functionRepository.findByUser_Id(-1L); // несуществующий ID
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("search_by_user_not_found", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(7)
    void sort_by_name_asc() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            userRepository.findAll().stream()
                    .sorted((u1, u2) -> u1.getName().compareTo(u2.getName()))
                    .collect(Collectors.toList());
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("sort_by_name_asc", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(8)
    void sort_by_id_desc() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            userRepository.findAll().stream()
                    .sorted((u1, u2) -> u2.getId().compareTo(u1.getId()))
                    .collect(Collectors.toList());
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("sort_by_id_desc", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(9)
    void search_by_x_val() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(existingFunctionId);
            points.stream().filter(p -> p.getXVal().equals(testXVal)).findFirst();
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("search_by_x_val", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(10)
    void search_by_y_val() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(existingFunctionId);
            points.stream().filter(p -> p.getYVal().equals(testYVal)).findFirst();
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("search_by_y_val", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(11)
    void search_by_operations_type() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            operationRepository.findAll().stream()
                    .filter(op -> op.getOperationsTypeId() == 1)
                    .collect(Collectors.toList());
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("search_by_operations_type", Math.round((double) totalMs / ITERATIONS));
    }

    @Test
    @Order(12)
    void search_combined_user_type_x() {
        prepareDataOnce();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            // Найти все табулярные функции пользователя → их точки с X = testXVal
            List<FunctionEntity> funcs = functionRepository.findByUser_Id(existingUserId).stream()
                    .filter(f -> f.getTypeFunction() == FunctionEntity.FunctionType.tabular)
                    .collect(Collectors.toList());
            for (FunctionEntity f : funcs) {
                tabulatedFunctionRepository.findByFunction_Id(f.getId()).stream()
                        .filter(p -> p.getXVal().equals(testXVal))
                        .findFirst();
            }
        }
        long totalMs = Duration.between(start, Instant.now()).toMillis();
        appendResult("search_combined_user_type_x", Math.round((double) totalMs / ITERATIONS));
    }
}