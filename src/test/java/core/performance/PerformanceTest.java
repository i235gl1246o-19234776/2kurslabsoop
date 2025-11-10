package core.performance;

import core.entity.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SpringBootTest
@ActiveProfiles("test_10k")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PerformanceTest {

    private static final int ITERATIONS = 1000;
    private static final String CSV_FILE = "framework-test-performance.csv";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Autowired
    private OperationRepository operationRepository;

    private static List<UserEntity> users = new ArrayList<>();
    private static List<FunctionEntity> functions = new ArrayList<>();

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

    @Test
    @Order(1)
    void populateDatabase() {
        // Очистка в правильном порядке
        operationRepository.deleteAllInBatch();
        tabulatedFunctionRepository.deleteAllInBatch();
        functionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        userRepository.flush();

        for (int i = 0; i < 10_000; i++) {
            UserEntity user = new UserEntity("user_" + i, "hash_" + i);
            UserEntity savedUser = userRepository.save(user);
            users.add(savedUser);

            boolean isTabular = (i % 2 == 0);
            FunctionEntity.FunctionType type = isTabular ? FunctionEntity.FunctionType.tabular : FunctionEntity.FunctionType.analytic;
            FunctionEntity func = new FunctionEntity(
                    savedUser,
                    type,
                    "func_for_user_" + i,
                    isTabular ? null : "x^2 + " + i
            );
            FunctionEntity savedFunc = functionRepository.save(func);
            functions.add(savedFunc);

            if (isTabular) {
                for (int k = 0; k < 2; k++) {
                    tabulatedFunctionRepository.save(new TabulatedFunctionEntity(
                            savedFunc,
                            (double) k,
                            (double) (k * k + i)
                    ));
                }
            } else {
                operationRepository.save(new OperationEntity(savedFunc, i % 5 + 1));
            }
        }
    }

    @Test
    @Order(2)
    void user_createUser() {
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            UserEntity u = new UserEntity("temp_create_" + i, "hash");
            userRepository.save(u);
            userRepository.delete(u);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("user_createUser", (double) ms / ITERATIONS);
    }

    @Test
    @Order(3)
    void user_findById() {
        UserEntity target = users.get(0);
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            userRepository.findById(target.getId());
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("user_findById", (double) ms / ITERATIONS);
    }

    @Test
    @Order(4)
    void user_findByName() {
        String name = users.get(0).getName();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            userRepository.findByName(name);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("user_findByName", (double) ms / ITERATIONS);
    }

    @Test
    @Order(5)
    void user_userNameExists() {
        String name = users.get(0).getName();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            userRepository.existsByName(name);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("user_userNameExists", (double) ms / ITERATIONS);
    }

    @Test
    @Order(6)
    void user_updateUser() {
        UserEntity original = users.get(0);
        String baseName = original.getName();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            original.setName(baseName + "_upd_" + i);
            userRepository.save(original);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        // Восстановим имя
        original.setName(baseName);
        userRepository.save(original);
        appendResult("user_updateUser", (double) ms / ITERATIONS);
    }

    @Test
    @Order(7)
    void user_deleteUser() {
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            UserEntity temp = new UserEntity("to_delete_" + i, "del");
            temp = userRepository.save(temp);
            userRepository.delete(temp);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("user_deleteUser", (double) ms / ITERATIONS);
    }

    @Test
    @Order(8)
    void user_authenticateUser() {
        UserEntity u = users.get(0);
        String name = u.getName();
        String hash = u.getPasswordHash();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            UserEntity found = userRepository.findByName(name);
            if (found == null || !found.getPasswordHash().equals(hash)) {
                throw new RuntimeException("Auth failed in perf test");
            }
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("user_authenticateUser", (double) ms / ITERATIONS);
    }

    @Test
    @Order(9)
    void function_createFunction() {
        UserEntity u = users.get(0);
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            FunctionEntity f = new FunctionEntity(u, FunctionEntity.FunctionType.analytic, "tmp_" + i, "x");
            f = functionRepository.save(f);
            functionRepository.delete(f);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("function_createFunction", (double) ms / ITERATIONS);
    }

    @Test
    @Order(10)
    void function_findById() {
        FunctionEntity f = functions.get(0);
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            functionRepository.findById(f.getId());
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("function_findById", (double) ms / ITERATIONS);
    }

    @Test
    @Order(11)
    void function_findByUserId() {
        Long userId = users.get(0).getId();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            functionRepository.findByUser_Id(userId);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("function_findByUserId", (double) ms / ITERATIONS);
    }

    @Test
    @Order(12)
    void function_findByName() {
        FunctionEntity target = functions.get(0);
        Long userId = target.getUser().getId();
        String name = target.getFunctionName();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            List<FunctionEntity> candidates = functionRepository.findByUser_Id(userId);
            candidates.stream()
                    .filter(f -> f.getFunctionName().equals(name))
                    .findFirst();
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("function_findByName", (double) ms / ITERATIONS);
    }

    @Test
    @Order(13)
    void function_findByType() {
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            functionRepository.findAll(); // эмуляция поиска по типу через фильтрацию в Java
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("function_findByType", (double) ms / ITERATIONS);
    }

    @Test
    @Order(14)
    void function_search() {
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            List<FunctionEntity> all = functionRepository.findAll();
            all.stream().filter(f -> f.getFunctionName().contains("5")).limit(100).toList();
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("function_search", (double) ms / ITERATIONS);
    }

    @Test
    @Order(15)
    void function_updateFunction() {
        FunctionEntity f = functions.get(0);
        String baseName = f.getFunctionName();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            f.setFunctionName(baseName + "_upd_" + i);
            functionRepository.save(f);
        }
        f.setFunctionName(baseName);
        functionRepository.save(f);
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("function_updateFunction", (double) ms / ITERATIONS);
    }

    @Test
    @Order(16)
    void function_deleteFunction() {
        UserEntity u = users.get(0);
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            FunctionEntity f = new FunctionEntity(u, FunctionEntity.FunctionType.analytic, "del_" + i, "x");
            f = functionRepository.save(f);
            functionRepository.delete(f);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("function_deleteFunction", (double) ms / ITERATIONS);
    }

    @Test
    @Order(17)
    void tabulated_createTabulatedFunction() {
        FunctionEntity tabFunc = functions.stream()
                .filter(fn -> fn.getTypeFunction() == FunctionEntity.FunctionType.tabular)
                .findFirst().orElseThrow();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            TabulatedFunctionEntity p = new TabulatedFunctionEntity(tabFunc, (double) i, (double) i * 2);
            p = tabulatedFunctionRepository.save(p);
            tabulatedFunctionRepository.delete(p);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("tabulated_createTabulatedFunction", (double) ms / ITERATIONS);
    }

    @Test
    @Order(18)
    void tabulated_findAllByFunctionId() {
        FunctionEntity tabFunc = functions.stream()
                .filter(fn -> fn.getTypeFunction() == FunctionEntity.FunctionType.tabular)
                .findFirst().orElseThrow();
        Long fid = tabFunc.getId();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            tabulatedFunctionRepository.findByFunction_Id(fid);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("tabulated_findAllByFunctionId", (double) ms / ITERATIONS);
    }

    @Test
    @Order(19)
    void tabulated_findByXValue() {
        TabulatedFunctionEntity p = tabulatedFunctionRepository.findAll().stream().findFirst().orElseThrow();
        Long fid = p.getFunction().getId();
        Double x = p.getXVal();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(fid);
            points.stream().filter(pt -> pt.getXVal().equals(x)).findFirst();
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("tabulated_findByXValue", (double) ms / ITERATIONS);
    }

    @Test
    @Order(20)
    void tabulated_findBetweenXValues() {
        TabulatedFunctionEntity p = tabulatedFunctionRepository.findAll().stream().findFirst().orElseThrow();
        Long fid = p.getFunction().getId();
        Double x = p.getXVal();
        Double min = x - 0.5;
        Double max = x + 0.5;
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(fid);
            points.stream()
                    .filter(pt -> pt.getXVal() >= min && pt.getXVal() <= max)
                    .toList();
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("tabulated_findBetweenXValues", (double) ms / ITERATIONS);
    }

    @Test
    @Order(21)
    void tabulated_updateTabulatedFunction() {
        TabulatedFunctionEntity p = tabulatedFunctionRepository.findAll().stream().findFirst().orElseThrow();
        Double baseY = p.getYVal();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            p.setYVal(baseY + i);
            tabulatedFunctionRepository.save(p);
        }
        p.setYVal(baseY);
        tabulatedFunctionRepository.save(p);
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("tabulated_updateTabulatedFunction", (double) ms / ITERATIONS);
    }

    @Test
    @Order(22)
    void tabulated_deleteTabulatedFunction() {
        FunctionEntity tabFunc = functions.stream()
                .filter(fn -> fn.getTypeFunction() == FunctionEntity.FunctionType.tabular)
                .findFirst().orElseThrow();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            TabulatedFunctionEntity p = new TabulatedFunctionEntity(tabFunc, -999.0 + i, -888.0);
            p = tabulatedFunctionRepository.save(p);
            tabulatedFunctionRepository.delete(p);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("tabulated_deleteTabulatedFunction", (double) ms / ITERATIONS);
    }

    @Test
    @Order(23)
    void tabulated_deleteAllTabulatedFunctions() {
        FunctionEntity func = functions.stream()
                .filter(fn -> fn.getTypeFunction() == FunctionEntity.FunctionType.tabular)
                .findFirst().orElseThrow();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            // Создаём 5 временных точек
            List<TabulatedFunctionEntity> batch = new ArrayList<>();
            for (int k = 0; k < 5; k++) {
                batch.add(new TabulatedFunctionEntity(func, (double) k + i, (double) k));
            }
            batch = tabulatedFunctionRepository.saveAll(batch);
            tabulatedFunctionRepository.deleteAll(batch);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("tabulated_deleteAllTabulatedFunctions", (double) ms / ITERATIONS);
    }

    @Test
    @Order(24)
    void operation_createOperation() {
        FunctionEntity analytic = functions.stream()
                .filter(fn -> fn.getTypeFunction() == FunctionEntity.FunctionType.analytic)
                .findFirst().orElseThrow();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            OperationEntity op = new OperationEntity(analytic, 99 + i);
            op = operationRepository.save(op);
            operationRepository.delete(op);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("operation_createOperation", (double) ms / ITERATIONS);
    }

    @Test
    @Order(25)
    void operation_findById() {
        OperationEntity op = operationRepository.findAll().stream().findFirst().orElseThrow();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            operationRepository.findById(op.getId());
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("operation_findById", (double) ms / ITERATIONS);
    }

    @Test
    @Order(26)
    void operation_updateOperation() {
        OperationEntity op = operationRepository.findAll().stream().findFirst().orElseThrow();
        int baseType = op.getOperationsTypeId();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            op.setOperationsTypeId(baseType + i);
            operationRepository.save(op);
        }
        op.setOperationsTypeId(baseType);
        operationRepository.save(op);
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("operation_updateOperation", (double) ms / ITERATIONS);
    }

    @Test
    @Order(27)
    void operation_deleteOperation() {
        FunctionEntity analytic = functions.stream()
                .filter(fn -> fn.getTypeFunction() == FunctionEntity.FunctionType.analytic)
                .findFirst().orElseThrow();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            OperationEntity op = new OperationEntity(analytic, 88 + i);
            op = operationRepository.save(op);
            operationRepository.delete(op);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("operation_deleteOperation", (double) ms / ITERATIONS);
    }

    @Test
    @Order(28)
    void operation_deleteAllOperations() {
        FunctionEntity func = functions.stream()
                .filter(fn -> fn.getTypeFunction() == FunctionEntity.FunctionType.analytic)
                .findFirst().orElseThrow();
        Instant start = Instant.now();
        for (int i = 0; i < ITERATIONS; i++) {
            List<OperationEntity> batch = new ArrayList<>();
            for (int k = 0; k < 3; k++) {
                batch.add(new OperationEntity(func, 100 + i + k));
            }
            batch = operationRepository.saveAll(batch);
            operationRepository.deleteAll(batch);
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        appendResult("operation_deleteAllOperations", (double) ms / ITERATIONS);
    }
}