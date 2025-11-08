package core.performance;

import core.entity.*;
import core.repository.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTest.class);
    private static final int USER_COUNT = 100;
    private static final int FUNCTIONS_PER_USER = 100; // Итого: 10 000 функций
    private static final String CSV_FILE = "framework-test-perfomance.csv";

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

    private void appendResult(String method, double timeMs) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(CSV_FILE, true))) {
            w.write(String.format("%s,%.2f", method, timeMs));
            w.newLine();
        } catch (IOException e) {
            log.error("Failed to write CSV", e);
        }
    }

    // ————————————————————————
    // 1. Наполнение БД (~10k функций → >10k записей)
    // ————————————————————————
    @Test
    @Order(1)
    void populateDatabase() {
        log.info("Populating DB: {} users × {} functions", USER_COUNT, FUNCTIONS_PER_USER);
        Instant start = Instant.now();

        for (int i = 0; i < USER_COUNT; i++) {
            UserEntity user = new UserEntity("user" + i, "hash" + i);
            UserEntity savedUser = userRepository.save(user);
            users.add(savedUser);

            for (int j = 0; j < FUNCTIONS_PER_USER; j++) {
                boolean isTabular = (j % 2 == 0);
                FunctionEntity.FunctionType type = isTabular ? FunctionEntity.FunctionType.tabular : FunctionEntity.FunctionType.analytic;
                FunctionEntity func = new FunctionEntity(
                        savedUser,
                        type,
                        "func_" + i + "_" + j,
                        isTabular ? null : "x^2 + " + j
                );
                FunctionEntity savedFunc = functionRepository.save(func);
                functions.add(savedFunc);

                if (isTabular) {
                    // 5 точек на табулярную функцию → +250k записей
                    for (int k = 0; k < 5; k++) {
                        tabulatedFunctionRepository.save(new TabulatedFunctionEntity(
                                savedFunc,
                                (double) k,
                                (double) (k * k + i)
                        ));
                    }
                } else {
                    operationRepository.save(new OperationEntity(savedFunc, j % 5 + 1));
                }
            }
        }

        long ms = Duration.between(start, Instant.now()).toMillis();
        log.info("Populated DB in {} ms", ms);
    }

    // ————————————————————————
    // 2. Замеры — точные вызовы репозиториев
    // ————————————————————————

    @Test
    @Order(2)
    void user_createUser() {
        UserEntity u = new UserEntity("temp_u", "hash");
        Instant t0 = Instant.now();
        userRepository.save(u);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        userRepository.delete(u);
        appendResult("user_createUser", ms);
    }

    @Test
    @Order(3)
    void user_findById() {
        UserEntity target = users.get(0);
        Instant t0 = Instant.now();
        UserEntity found = userRepository.findById(target.getId()).orElse(null);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("user_findById", ms);
    }

    @Test
    @Order(4)
    void user_findByName() {
        String name = users.get(0).getName();
        Instant t0 = Instant.now();
        UserEntity found = userRepository.findByName(name);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("user_findByName", ms);
    }

    @Test
    @Order(5)
    void user_userNameExists() {
        String name = users.get(0).getName();
        Instant t0 = Instant.now();
        boolean exists = userRepository.existsByName(name);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("user_userNameExists", ms);
    }

    @Test
    @Order(6)
    void user_updateUser() {
        UserEntity u = users.get(0);
        u.setName(u.getName() + "_upd");
        Instant t0 = Instant.now();
        userRepository.save(u);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("user_updateUser", ms);
    }

    @Test
    @Order(7)
    void user_deleteUser() {
        UserEntity temp = userRepository.save(new UserEntity("to_del", "del"));
        Instant t0 = Instant.now();
        userRepository.delete(temp);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("user_deleteUser", ms);
    }

    @Test
    @Order(8)
    void user_authenticateUser() {
        UserEntity u = users.get(0);
        Instant t0 = Instant.now();
        UserEntity found = userRepository.findByName(u.getName());
        boolean ok = found != null && found.getPasswordHash().equals(u.getPasswordHash());
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("user_authenticateUser", ms);
    }

    @Test
    @Order(9)
    void function_createFunction() {
        UserEntity u = users.get(0);
        FunctionEntity f = new FunctionEntity(u, FunctionEntity.FunctionType.analytic, "tmp", "x");
        Instant t0 = Instant.now();
        functionRepository.save(f);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        functionRepository.delete(f);
        appendResult("function_createFunction", ms);
    }

    @Test
    @Order(10)
    void function_findById() {
        FunctionEntity f = functions.get(0);
        Instant t0 = Instant.now();
        FunctionEntity found = functionRepository.findById(f.getId()).orElse(null);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("function_findById", ms);
    }

    @Test
    @Order(11)
    void function_findByUserId() {
        Long userId = users.get(0).getId();
        Instant t0 = Instant.now();
        List<FunctionEntity> list = functionRepository.findByUser_Id(userId);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("function_findByUserId", ms);
    }

    @Test
    @Order(12)
    void function_findByName() {
        // Используем findByUser_Id + фильтр по имени — так делает сервис
        FunctionEntity target = functions.get(0);
        Long userId = target.getUser().getId();
        String name = target.getFunctionName();

        Instant t0 = Instant.now();
        List<FunctionEntity> candidates = functionRepository.findByUser_Id(userId);
        FunctionEntity match = candidates.stream()
                .filter(f -> f.getFunctionName().equals(name))
                .findFirst()
                .orElse(null);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("function_findByName", ms);
    }

    @Test
    @Order(13)
    void function_findByType() {
        Instant t0 = Instant.now();
        List<FunctionEntity> all = functionRepository.findAll(); // для нагрузки
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("function_findByType", ms);
    }

    @Test
    @Order(14)
    void function_search() {
        // Поиск по шаблону — эмулируем через Java
        Instant t0 = Instant.now();
        List<FunctionEntity> all = functionRepository.findAll();
        List<FunctionEntity> matches = all.stream()
                .filter(f -> f.getFunctionName().contains("5"))
                .limit(100)
                .toList();
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("function_search", ms);
    }

    @Test
    @Order(15)
    void function_updateFunction() {
        FunctionEntity f = functions.get(0);
        f.setFunctionName(f.getFunctionName() + "_upd");
        Instant t0 = Instant.now();
        functionRepository.save(f);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("function_updateFunction", ms);
    }

    @Test
    @Order(16)
    void function_deleteFunction() {
        FunctionEntity f = new FunctionEntity(users.get(0), FunctionEntity.FunctionType.analytic, "to_del", "x");
        f = functionRepository.save(f);
        Instant t0 = Instant.now();
        functionRepository.delete(f);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("function_deleteFunction", ms);
    }

    @Test
    @Order(17)
    void tabulated_createTabulatedFunction() {
        FunctionEntity tabFunc = functions.stream()
                .filter(f -> f.getTypeFunction() == FunctionEntity.FunctionType.tabular)
                .findFirst().orElseThrow();
        TabulatedFunctionEntity p = new TabulatedFunctionEntity(tabFunc, 999.0, 888.0);
        Instant t0 = Instant.now();
        tabulatedFunctionRepository.save(p);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        tabulatedFunctionRepository.delete(p);
        appendResult("tabulated_createTabulatedFunction", ms);
    }

    @Test
    @Order(18)
    void tabulated_findAllByFunctionId() {
        FunctionEntity tabFunc = functions.stream()
                .filter(f -> f.getTypeFunction() == FunctionEntity.FunctionType.tabular)
                .findFirst().orElseThrow();
        Instant t0 = Instant.now();
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(tabFunc.getId());
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("tabulated_findAllByFunctionId", ms);
    }

    @Test
    @Order(19)
    void tabulated_findByXValue() {
        TabulatedFunctionEntity p = tabulatedFunctionRepository.findAll().stream().findFirst().orElseThrow();
        Long fid = p.getFunction().getId();
        Double x = p.getXVal();

        Instant t0 = Instant.now();
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(fid);
        TabulatedFunctionEntity match = points.stream()
                .filter(pt -> pt.getXVal().equals(x))
                .findFirst()
                .orElse(null);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("tabulated_findByXValue", ms);
    }

    @Test
    @Order(20)
    void tabulated_findBetweenXValues() {
        TabulatedFunctionEntity p = tabulatedFunctionRepository.findAll().stream().findFirst().orElseThrow();
        Long fid = p.getFunction().getId();
        Double x = p.getXVal();
        Double min = x - 0.5;
        Double max = x + 0.5;

        Instant t0 = Instant.now();
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(fid);
        List<TabulatedFunctionEntity> between = points.stream()
                .filter(pt -> pt.getXVal() >= min && pt.getXVal() <= max)
                .toList();
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("tabulated_findBetweenXValues", ms);
    }

    @Test
    @Order(21)
    void tabulated_updateTabulatedFunction() {
        TabulatedFunctionEntity p = tabulatedFunctionRepository.findAll().stream().findFirst().orElseThrow();
        p.setYVal(p.getYVal() + 1000);
        Instant t0 = Instant.now();
        tabulatedFunctionRepository.save(p);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("tabulated_updateTabulatedFunction", ms);
    }

    @Test
    @Order(22)
    void tabulated_deleteTabulatedFunction() {
        TabulatedFunctionEntity p = new TabulatedFunctionEntity(
                functions.stream().filter(f -> f.getTypeFunction() == FunctionEntity.FunctionType.tabular).findFirst().orElseThrow(),
                -999.0, -888.0
        );
        p = tabulatedFunctionRepository.save(p);
        Instant t0 = Instant.now();
        tabulatedFunctionRepository.delete(p);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("tabulated_deleteTabulatedFunction", ms);
    }

    @Test
    @Order(23)
    void tabulated_deleteAllTabulatedFunctions() {
        FunctionEntity func = functions.stream()
                .filter(f -> f.getTypeFunction() == FunctionEntity.FunctionType.tabular)
                .findFirst().orElseThrow();
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(func.getId());
        Instant t0 = Instant.now();
        tabulatedFunctionRepository.deleteAll(points);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("tabulated_deleteAllTabulatedFunctions", ms);
        // Восстановим одну точку
        tabulatedFunctionRepository.save(new TabulatedFunctionEntity(func, 0.0, 0.0));
    }

    @Test
    @Order(24)
    void operation_createOperation() {
        FunctionEntity analytic = functions.stream()
                .filter(f -> f.getTypeFunction() == FunctionEntity.FunctionType.analytic)
                .findFirst().orElseThrow();
        OperationEntity op = new OperationEntity(analytic, 99);
        Instant t0 = Instant.now();
        operationRepository.save(op);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        operationRepository.delete(op);
        appendResult("operation_createOperation", ms);
    }

    @Test
    @Order(25)
    void operation_findById() {
        OperationEntity op = operationRepository.findAll().stream().findFirst().orElseThrow();
        Instant t0 = Instant.now();
        OperationEntity found = operationRepository.findById(op.getId()).orElse(null);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("operation_findById", ms);
    }

    @Test
    @Order(26)
    void operation_updateOperation() {
        OperationEntity op = operationRepository.findAll().stream().findFirst().orElseThrow();
        op.setOperationsTypeId(999);
        Instant t0 = Instant.now();
        operationRepository.save(op);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("operation_updateOperation", ms);
    }

    @Test
    @Order(27)
    void operation_deleteOperation() {
        FunctionEntity analytic = functions.stream()
                .filter(f -> f.getTypeFunction() == FunctionEntity.FunctionType.analytic)
                .findFirst().orElseThrow();
        OperationEntity op = new OperationEntity(analytic, 88);
        op = operationRepository.save(op);
        Instant t0 = Instant.now();
        operationRepository.delete(op);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("operation_deleteOperation", ms);
    }

    @Test
    @Order(28)
    void operation_deleteAllOperations() {
        FunctionEntity func = functions.stream()
                .filter(f -> f.getTypeFunction() == FunctionEntity.FunctionType.analytic)
                .findFirst().orElseThrow();
        List<OperationEntity> ops = operationRepository.findAll().stream()
                .filter(o -> o.getFunction().getId().equals(func.getId()))
                .toList();
        if (ops.isEmpty()) return;
        Instant t0 = Instant.now();
        operationRepository.deleteAll(ops);
        long ms = Duration.between(t0, Instant.now()).toMillis();
        appendResult("operation_deleteAllOperations", ms);
        operationRepository.save(new OperationEntity(func, 1));
    }
}