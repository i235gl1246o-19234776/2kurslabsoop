package core.performance;

import core.Application;
import core.entity.*;
import core.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none" // Используем validate, так как таблицы уже созданы
})
public class PerformanceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Autowired
    private OperationRepository operationRepository;

    private static final int RECORD_COUNT = 10_000; // Количество записей для теста
    private static final String TEST_USER_NAME = "test_user_performance";
    private static final Random random = new Random();

    private UserEntity testUser;
    private List<FunctionEntity> testFunctions;
    private List<TabulatedFunctionEntity> testTabulatedPoints;
    private List<OperationEntity> testOperations;

    // Путь к файлу для записи результатов
    private static final String RESULTS_FILE_PATH = "performance_results_framework.csv";

    @BeforeEach
    void setUp() {
        // Очищаем данные перед каждым тестом
        cleanDatabase();
        // Создаем пользователя
        testUser = new UserEntity(TEST_USER_NAME, "password_hash_" + System.currentTimeMillis());
        testUser = userRepository.save(testUser);

        // Генерируем функции
        testFunctions = generateFunctions(testUser, RECORD_COUNT);
        // Генерируем табулированные точки
        testTabulatedPoints = generateTabulatedPoints(testFunctions);
        // Генерируем операции
        testOperations = generateOperations(testFunctions);

        System.out.println("База данных подготовлена для теста с " + RECORD_COUNT + " записями.");
    }

    @AfterEach
    void tearDown() {
        // Очищаем данные после каждого теста
        cleanDatabase();
    }

    @Test
    @DisplayName("Тест производительности CRUD операций")
    void testCRUDPerformance() {
        long startTime, endTime;
        List<Long> times = new ArrayList<>();

        // 1. Тест на создание (Create)
        System.out.println("Запуск теста на создание...");
        startTime = Instant.now().toEpochMilli();
        for (FunctionEntity func : testFunctions) {
            functionRepository.save(func);
        }
        for (TabulatedFunctionEntity point : testTabulatedPoints) {
            tabulatedFunctionRepository.save(point);
        }
        for (OperationEntity op : testOperations) {
            operationRepository.save(op);
        }
        endTime = Instant.now().toEpochMilli();
        times.add(endTime - startTime);
        System.out.println("Время создания: " + (endTime - startTime) + " мс");

        // 2. Тест на чтение (Read) - Поиск по ID
        System.out.println("Запуск теста на чтение по ID...");
        startTime = Instant.now().toEpochMilli();
        for (FunctionEntity func : testFunctions) {
            functionRepository.findById(func.getId()).orElse(null);
        }
        for (TabulatedFunctionEntity point : testTabulatedPoints) {
            tabulatedFunctionRepository.findById(point.getId()).orElse(null);
        }
        for (OperationEntity op : testOperations) {
            operationRepository.findById(op.getId()).orElse(null);
        }
        endTime = Instant.now().toEpochMilli();
        times.add(endTime - startTime);
        System.out.println("Время чтения по ID: " + (endTime - startTime) + " мс");

        // 3. Тест на чтение (Read) - Поиск по пользователю (один запрос)
        System.out.println("Запуск теста на чтение всех функций пользователя...");
        startTime = Instant.now().toEpochMilli();
        List<FunctionEntity> foundFunctions = functionRepository.findByUser_Id(testUser.getId());
        endTime = Instant.now().toEpochMilli();
        times.add(endTime - startTime);
        System.out.println("Время чтения всех функций пользователя: " + (endTime - startTime) + " мс");

        // 4. Тест на обновление (Update)
        System.out.println("Запуск теста на обновление...");
        startTime = Instant.now().toEpochMilli();
        for (FunctionEntity func : testFunctions) {
            func.setFunctionName("Updated_" + func.getFunctionName());
            functionRepository.save(func);
        }
        for (TabulatedFunctionEntity point : testTabulatedPoints) {
            point.setYVal(point.getYVal() + 1.0); // Простое изменение
            tabulatedFunctionRepository.save(point);
        }
        for (OperationEntity op : testOperations) {
            op.setOperationsTypeId(op.getOperationsTypeId() + 1); // Простое изменение
            operationRepository.save(op);
        }
        endTime = Instant.now().toEpochMilli();
        times.add(endTime - startTime);
        System.out.println("Время обновления: " + (endTime - startTime) + " мс");

        // 5. Тест на удаление (Delete)
        System.out.println("Запуск теста на удаление...");
        startTime = Instant.now().toEpochMilli();
        for (TabulatedFunctionEntity point : testTabulatedPoints) {
            tabulatedFunctionRepository.delete(point);
        }
        for (OperationEntity op : testOperations) {
            operationRepository.delete(op);
        }
        for (FunctionEntity func : testFunctions) {
            functionRepository.delete(func);
        }
        endTime = Instant.now().toEpochMilli();
        times.add(endTime - startTime);
        System.out.println("Время удаления: " + (endTime - startTime) + " мс");

        // Запись результатов в CSV
        writeResultsToCSV(times);
    }

    private void cleanDatabase() {
        // Удаляем в обратном порядке из-за внешних ключей
        operationRepository.deleteAll();
        tabulatedFunctionRepository.deleteAll();
        functionRepository.deleteAll();
        userRepository.deleteAll();
    }

    private List<FunctionEntity> generateFunctions(UserEntity user, int count) {
        List<FunctionEntity> functions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            FunctionEntity func = new FunctionEntity(
                    user,
                    i % 2 == 0 ? FunctionEntity.FunctionType.tabular : FunctionEntity.FunctionType.analytic,
                    "Function_" + i,
                    "expression_" + i
            );
            functions.add(func);
        }
        return functions;
    }

    private List<TabulatedFunctionEntity> generateTabulatedPoints(List<FunctionEntity> functions) {
        List<TabulatedFunctionEntity> points = new ArrayList<>();
        for (FunctionEntity func : functions) {
            // Для каждой функции создаем одну точку
            TabulatedFunctionEntity point = new TabulatedFunctionEntity(
                    func,
                    random.nextDouble() * 100.0, // x_val
                    random.nextDouble() * 100.0  // y_val
            );
            points.add(point);
        }
        return points;
    }

    private List<OperationEntity> generateOperations(List<FunctionEntity> functions) {
        List<OperationEntity> operations = new ArrayList<>();
        for (FunctionEntity func : functions) {
            // Для каждой функции создаем одну операцию
            OperationEntity op = new OperationEntity(
                    func,
                    random.nextInt(5) + 1 // operations_type_id от 1 до 5
            );
            operations.add(op);
        }
        return operations;
    }

    private void writeResultsToCSV(List<Long> times) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESULTS_FILE_PATH))) {
            writer.write("Операция,Время (мс)\n");
            writer.write("Создание," + times.get(0) + "\n");
            writer.write("Чтение по ID," + times.get(1) + "\n");
            writer.write("Чтение всех функций пользователя," + times.get(2) + "\n");
            writer.write("Обновление," + times.get(3) + "\n");
            writer.write("Удаление," + times.get(4) + "\n");
            System.out.println("Результаты успешно записаны в файл: " + RESULTS_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Ошибка при записи результатов в CSV: " + e.getMessage());
        }
    }
}