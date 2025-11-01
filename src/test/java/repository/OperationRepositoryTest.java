package repository;

import model.Function;
import model.Operation;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OperationRepositoryTest extends BaseRepositoryTest {

    private UserRepository userRepository;
    private FunctionRepository functionRepository;
    private OperationRepository operationRepository;
    private Long testUserId;
    private Long testFunctionId;

    @BeforeEach
    void setUp() throws SQLException {
        userRepository = new UserRepository();
        functionRepository = new FunctionRepository();
        operationRepository = new OperationRepository();

        // Создание тестового пользователя и функции
        User user = new User("operation_test_user", "test_password");
        testUserId = userRepository.createUser(user);

        Function function = new Function(testUserId, "analytic", "test_operation_func", "x^2");
        testFunctionId = functionRepository.createFunction(function);
    }

    @Test
    void testCreateOperation_ShouldCreateNewOperation() throws SQLException {
        // Подготовка
        Operation operation = new Operation(testFunctionId, 1);

        // Действие
        Long operationId = operationRepository.createOperation(operation);

        // Проверка
        assertNotNull(operationId, "ID операции не должен быть null");
        assertTrue(operationId > 0, "ID операции должен быть положительным числом");

        Optional<Operation> foundOperation = operationRepository.findById(operationId, testFunctionId);
        assertTrue(foundOperation.isPresent(), "Операция должна быть найдена в базе");
        assertEquals(1, foundOperation.get().getOperationsTypeId(), "Тип операции должен совпадать");
        assertEquals(testFunctionId, foundOperation.get().getFunctionId(), "Function ID должен совпадать");
    }

    @Test
    void testCreateMultipleOperations_ShouldStoreAllOperations() throws SQLException {
        // Подготовка
        Operation op1 = new Operation(testFunctionId, 1);
        Operation op2 = new Operation(testFunctionId, 2);
        Operation op3 = new Operation(testFunctionId, 3);

        // Действие
        Long id1 = operationRepository.createOperation(op1);
        Long id2 = operationRepository.createOperation(op2);
        Long id3 = operationRepository.createOperation(op3);

        // Проверка
        assertNotNull(id1, "Первая операция должна быть создана");
        assertNotNull(id2, "Вторая операция должна быть создана");
        assertNotNull(id3, "Третья операция должна быть создана");

        assertNotEquals(id1, id2, "ID операций должны быть разными");
        assertNotEquals(id1, id3, "ID операций должны быть разными");
        assertNotEquals(id2, id3, "ID операций должны быть разными");
    }

    @Test
    void testFindById_ShouldReturnOperation_WhenOperationExists() throws SQLException {
        // Подготовка
        Operation operation = new Operation(testFunctionId, 5);
        Long operationId = operationRepository.createOperation(operation);

        // Действие
        Optional<Operation> foundOperation = operationRepository.findById(operationId, testFunctionId);

        // Проверка
        assertTrue(foundOperation.isPresent(), "Операция должна быть найдена");
        assertEquals(operationId, foundOperation.get().getId(), "ID операции должен совпадать");
        assertEquals(5, foundOperation.get().getOperationsTypeId(), "Тип операции должен совпадать");
        assertEquals(testFunctionId, foundOperation.get().getFunctionId(), "Function ID должен совпадать");
    }

    @Test
    void testFindById_ShouldReturnEmpty_WhenOperationNotExists() throws SQLException {
        // Действие
        Optional<Operation> foundOperation = operationRepository.findById(999999L, testFunctionId);

        // Проверка
        assertFalse(foundOperation.isPresent(), "Операция не должна быть найдена");
    }

    @Test
    void testFindById_ShouldReturnEmpty_WhenWrongFunctionId() throws SQLException {
        // Подготовка
        Operation operation = new Operation(testFunctionId, 1);
        Long operationId = operationRepository.createOperation(operation);

        // Действие - ищем с неправильным function_id
        Optional<Operation> foundOperation = operationRepository.findById(operationId, 999999L);

        // Проверка
        assertFalse(foundOperation.isPresent(), "Операция не должна быть найдена при неправильном function_id");
    }

    @Test
    void testUpdateOperation_ShouldUpdateOperationType() throws SQLException {
        // Подготовка
        Operation operation = new Operation(testFunctionId, 1);
        Long operationId = operationRepository.createOperation(operation);
        operation.setId(operationId);

        // Действие
        operation.setOperationsTypeId(2);
        boolean updated = operationRepository.updateOperation(operation);

        // Проверка
        assertTrue(updated, "Обновление должно быть успешным");

        Optional<Operation> foundOperation = operationRepository.findById(operationId, testFunctionId);
        assertTrue(foundOperation.isPresent(), "Операция должна существовать после обновления");
        assertEquals(2, foundOperation.get().getOperationsTypeId(), "Тип операции должен быть обновлен");
    }

    @Test
    void testUpdateOperation_ShouldReturnFalse_WhenOperationNotExists() throws SQLException {
        // Подготовка
        Operation nonExistentOperation = new Operation(testFunctionId, 1);
        nonExistentOperation.setId(999999L);

        // Действие
        boolean updated = operationRepository.updateOperation(nonExistentOperation);

        // Проверка
        assertFalse(updated, "Обновление несуществующей операции должно вернуть false");
    }

    @Test
    void testDeleteOperation_ShouldDeleteOperation() throws SQLException {
        // Подготовка
        Operation operation = new Operation(testFunctionId, 1);
        Long operationId = operationRepository.createOperation(operation);

        // Проверка что операция создана
        assertTrue(operationRepository.findById(operationId, testFunctionId).isPresent(),
                "Операция должна существовать до удаления");

        // Действие
        boolean deleted = operationRepository.deleteOperation(operationId, testFunctionId);

        // Проверка
        assertTrue(deleted, "Удаление должно быть успешным");
        assertFalse(operationRepository.findById(operationId, testFunctionId).isPresent(),
                "Операция не должна существовать после удаления");
    }

    @Test
    void testDeleteOperation_ShouldReturnFalse_WhenOperationNotExists() throws SQLException {
        // Действие
        boolean deleted = operationRepository.deleteOperation(999999L, testFunctionId);

        // Проверка
        assertFalse(deleted, "Удаление несуществующей операции должно вернуть false");
    }

    @Test
    void testDeleteOperation_ShouldReturnFalse_WhenWrongFunctionId() throws SQLException {
        // Подготовка
        Operation operation = new Operation(testFunctionId, 1);
        Long operationId = operationRepository.createOperation(operation);

        // Действие - пытаемся удалить с неправильным function_id
        boolean deleted = operationRepository.deleteOperation(operationId, 999999L);

        // Проверка
        assertFalse(deleted, "Удаление с неправильным function_id должно вернуть false");
        // Проверяем что операция все еще существует
        assertTrue(operationRepository.findById(operationId, testFunctionId).isPresent(),
                "Операция должна все еще существовать после неудачного удаления");
    }

    @Test
    void testDeleteAllOperations_ShouldDeleteAllOperationsForFunction() throws SQLException {
        // Подготовка
        operationRepository.createOperation(new Operation(testFunctionId, 1));
        operationRepository.createOperation(new Operation(testFunctionId, 2));
        operationRepository.createOperation(new Operation(testFunctionId, 3));

        // Создаем другую функцию и операции для нее
        Function anotherFunction = new Function(testUserId, "analytic", "another_func", "sin(x)");
        Long anotherFunctionId = functionRepository.createFunction(anotherFunction);
        operationRepository.createOperation(new Operation(anotherFunctionId, 4));

        // Действие - удаляем операции только для testFunctionId
        boolean deleted = operationRepository.deleteAllOperations(testFunctionId);

        // Проверка
        assertTrue(deleted, "Удаление всех операций должно быть успешным");

        // Проверяем что операции для anotherFunctionId все еще существуют
        // (нужно создать метод findOperationsByFunctionId или использовать другой способ проверки)
    }


    @Test
    void testOperationWithDifferentTypes() throws SQLException {
        // Подготовка
        Operation op1 = new Operation(testFunctionId, 1); // тип 1
        Operation op2 = new Operation(testFunctionId, 2); // тип 2
        Operation op3 = new Operation(testFunctionId, 999); // произвольный тип

        // Действие
        Long id1 = operationRepository.createOperation(op1);
        Long id2 = operationRepository.createOperation(op2);
        Long id3 = operationRepository.createOperation(op3);

        // Проверка
        assertNotNull(id1, "Операция с типом 1 должна быть создана");
        assertNotNull(id2, "Операция с типом 2 должна быть создана");
        assertNotNull(id3, "Операция с произвольным типом должна быть создана");

        Optional<Operation> foundOp1 = operationRepository.findById(id1, testFunctionId);
        Optional<Operation> foundOp2 = operationRepository.findById(id2, testFunctionId);
        Optional<Operation> foundOp3 = operationRepository.findById(id3, testFunctionId);

        assertTrue(foundOp1.isPresent(), "Операция 1 должна быть найдена");
        assertEquals(1, foundOp1.get().getOperationsTypeId(), "Тип операции 1 должен быть 1");

        assertTrue(foundOp2.isPresent(), "Операция 2 должна быть найдена");
        assertEquals(2, foundOp2.get().getOperationsTypeId(), "Тип операции 2 должен быть 2");

        assertTrue(foundOp3.isPresent(), "Операция 3 должна быть найдена");
        assertEquals(999, foundOp3.get().getOperationsTypeId(), "Тип операции 3 должен быть 999");
    }
}