package repository;

import model.Function;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FunctionRepositoryTest extends BaseRepositoryTest {

    private UserRepository userRepository;
    private FunctionRepository functionRepository;
    private Long testUserId;

    @BeforeEach
    void setUp() throws SQLException {
        userRepository = new UserRepository();
        functionRepository = new FunctionRepository();

        // Создание тестового пользователя для функций
        User user = new User("function_test_user", "test_password");
        testUserId = userRepository.createUser(user);
    }

    @Test
    void testCreateFunction_ShouldCreateNewFunction() throws SQLException {
        // Подготовка
        Function function = new Function(testUserId, "analytic", "quadratic", "x^2 + 2*x + 1");

        // Действие
        Long functionId = functionRepository.createFunction(function);

        // Проверка
        assertNotNull(functionId, "ID функции не должен быть null");
        assertTrue(functionId > 0, "ID функции должен быть положительным числом");

        Optional<Function> foundFunction = functionRepository.findById(functionId, testUserId);
        assertTrue(foundFunction.isPresent(), "Функция должна быть найдена в базе");
        assertEquals("analytic", foundFunction.get().getTypeFunction(), "Тип функции должен совпадать");
        assertEquals("quadratic", foundFunction.get().getFunctionName(), "Имя функции должно совпадать");
        assertEquals("x^2 + 2*x + 1", foundFunction.get().getFunctionExpression(), "Выражение функции должно совпадать");
        assertEquals(testUserId, foundFunction.get().getUserId(), "User ID должен совпадать");
    }

    @Test
    void testCreateFunction_ShouldCreateTabularFunction_WithNullExpression() throws SQLException {
        // Подготовка
        Function function = new Function(testUserId, "tabular", "tabular_func", null);

        // Действие
        Long functionId = functionRepository.createFunction(function);

        // Проверка
        assertNotNull(functionId, "ID функции не должен быть null");

        Optional<Function> foundFunction = functionRepository.findById(functionId, testUserId);
        assertTrue(foundFunction.isPresent(), "Табулированная функция должна быть найдена");
        assertEquals("tabular", foundFunction.get().getTypeFunction(), "Тип должен быть 'tabular'");
        assertNull(foundFunction.get().getFunctionExpression(), "Выражение должно быть null для табулированной функции");
    }

    @Test
    void testFindById_ShouldReturnFunction_WhenFunctionExists() throws SQLException {
        // Подготовка
        Function function = new Function(testUserId, "analytic", "test_func", "sin(x)");
        Long functionId = functionRepository.createFunction(function);

        // Действие
        Optional<Function> foundFunction = functionRepository.findById(functionId, testUserId);

        // Проверка
        assertTrue(foundFunction.isPresent(), "Функция должна быть найдена");
        assertEquals(functionId, foundFunction.get().getId(), "ID функции должен совпадать");
        assertEquals("test_func", foundFunction.get().getFunctionName(), "Имя функции должно совпадать");
    }

    @Test
    void testFindById_ShouldReturnEmpty_WhenFunctionNotExists() throws SQLException {
        // Действие
        Optional<Function> foundFunction = functionRepository.findById(999999L, testUserId);

        // Проверка
        assertFalse(foundFunction.isPresent(), "Функция не должна быть найдена");
    }

    @Test
    void testFindById_ShouldReturnEmpty_WhenWrongUserId() throws SQLException {
        // Подготовка
        Function function = new Function(testUserId, "analytic", "test_func", "x");
        Long functionId = functionRepository.createFunction(function);

        // Действие - ищем с неправильным user_id
        Optional<Function> foundFunction = functionRepository.findById(functionId, 999999L);

        // Проверка
        assertFalse(foundFunction.isPresent(), "Функция не должна быть найдена при неправильном user_id");
    }

    @Test
    void testFindByUserId_ShouldReturnAllUserFunctions() throws SQLException {
        // Подготовка
        Function func1 = new Function(testUserId, "analytic", "linear", "x + 1");
        Function func2 = new Function(testUserId, "tabular", "sine_wave", null);
        Function func3 = new Function(testUserId, "analytic", "cubic", "x^3");

        functionRepository.createFunction(func1);
        functionRepository.createFunction(func2);
        functionRepository.createFunction(func3);

        // Действие
        List<Function> functions = functionRepository.findByUserId(testUserId);

        // Проверка
        assertEquals(3, functions.size(), "Должно быть найдено 3 функции");
        assertTrue(functions.stream().anyMatch(f -> "linear".equals(f.getFunctionName())),
                "Должна быть найдена linear функция");
        assertTrue(functions.stream().anyMatch(f -> "sine_wave".equals(f.getFunctionName())),
                "Должна быть найдена sine_wave функция");
        assertTrue(functions.stream().anyMatch(f -> "cubic".equals(f.getFunctionName())),
                "Должна быть найдена cubic функция");
    }

    @Test
    void testFindByUserId_ShouldReturnEmptyList_WhenNoFunctions() throws SQLException {
        // Действие
        List<Function> functions = functionRepository.findByUserId(testUserId);

        // Проверка
        assertTrue(functions.isEmpty(), "Список функций должен быть пустым");
    }

    @Test
    void testFindByName_ShouldReturnMatchingFunctions() throws SQLException {
        // Подготовка
        Function func1 = new Function(testUserId, "analytic", "quadratic_function", "x^2");
        Function func2 = new Function(testUserId, "analytic", "linear_function", "x");
        Function func3 = new Function(testUserId, "analytic", "another_quadratic", "2*x^2");

        functionRepository.createFunction(func1);
        functionRepository.createFunction(func2);
        functionRepository.createFunction(func3);

        // Действие
        List<Function> functions = functionRepository.findByName(testUserId, "quadratic");

        // Проверка
        assertEquals(2, functions.size(), "Должно быть найдено 2 функции с 'quadratic' в имени");
        assertTrue(functions.stream().allMatch(f -> f.getFunctionName().contains("quadratic")),
                "Все найденные функции должны содержать 'quadratic' в имени");
    }

    @Test
    void testFindByType_ShouldReturnFunctionsByType() throws SQLException {
        // Подготовка
        Function func1 = new Function(testUserId, "analytic", "func1", "x^2");
        Function func2 = new Function(testUserId, "tabular", "func2", null);
        Function func3 = new Function(testUserId, "analytic", "func3", "x^3");
        Function func4 = new Function(testUserId, "tabular", "func4", null);

        functionRepository.createFunction(func1);
        functionRepository.createFunction(func2);
        functionRepository.createFunction(func3);
        functionRepository.createFunction(func4);

        // Действие
        List<Function> analyticFunctions = functionRepository.findByType(testUserId, "analytic");
        List<Function> tabularFunctions = functionRepository.findByType(testUserId, "tabular");

        // Проверка
        assertEquals(2, analyticFunctions.size(), "Должно быть найдено 2 аналитические функции");
        assertEquals(2, tabularFunctions.size(), "Должно быть найдено 2 табулированные функции");

        assertTrue(analyticFunctions.stream().allMatch(f -> "analytic".equals(f.getTypeFunction())),
                "Все функции должны быть типа 'analytic'");
        assertTrue(tabularFunctions.stream().allMatch(f -> "tabular".equals(f.getTypeFunction())),
                "Все функции должны быть типа 'tabular'");
    }

    @Test
    void testUpdateFunction_ShouldUpdateFunctionData() throws SQLException {
        // Подготовка
        Function function = new Function(testUserId, "analytic", "old_name", "old_expr");
        Long functionId = functionRepository.createFunction(function);
        function.setId(functionId);

        // Действие
        function.setFunctionName("updated_function_name");
        function.setFunctionExpression("new_expression");
        function.setTypeFunction("tabular");
        boolean updated = functionRepository.updateFunction(function);

        // Проверка
        assertTrue(updated, "Обновление должно быть успешным");

        Optional<Function> foundFunction = functionRepository.findById(functionId, testUserId);
        assertTrue(foundFunction.isPresent(), "Функция должна существовать после обновления");
        assertEquals("updated_function_name", foundFunction.get().getFunctionName(), "Имя должно быть обновлено");
        assertEquals("new_expression", foundFunction.get().getFunctionExpression(), "Выражение должно быть обновлено");
        assertEquals("tabular", foundFunction.get().getTypeFunction(), "Тип должен быть обновлен");
    }

    @Test
    void testUpdateFunction_ShouldReturnFalse_WhenFunctionNotExists() throws SQLException {
        // Подготовка
        Function nonExistentFunction = new Function(testUserId, "analytic", "nonexistent", "x");
        nonExistentFunction.setId(999999L);

        // Действие
        boolean updated = functionRepository.updateFunction(nonExistentFunction);

        // Проверка
        assertFalse(updated, "Обновление несуществующей функции должно вернуть false");
    }

    @Test
    void testDeleteFunction_ShouldDeleteFunction() throws SQLException {
        // Подготовка
        Function function = new Function(testUserId, "analytic", "to_delete", "x");
        Long functionId = functionRepository.createFunction(function);

        // Проверка что функция создана
        assertTrue(functionRepository.findById(functionId, testUserId).isPresent(),
                "Функция должна существовать до удаления");

        // Действие
        boolean deleted = functionRepository.deleteFunction(functionId, testUserId);

        // Проверка
        assertTrue(deleted, "Удаление должно быть успешным");
        assertFalse(functionRepository.findById(functionId, testUserId).isPresent(),
                "Функция не должна существовать после удаления");
    }

    @Test
    void testDeleteFunction_ShouldReturnFalse_WhenFunctionNotExists() throws SQLException {
        // Действие
        boolean deleted = functionRepository.deleteFunction(999999L, testUserId);

        // Проверка
        assertFalse(deleted, "Удаление несуществующей функции должно вернуть false");
    }

    @Test
    void testDeleteFunction_ShouldReturnFalse_WhenWrongUserId() throws SQLException {
        // Подготовка
        Function function = new Function(testUserId, "analytic", "test_func", "x");
        Long functionId = functionRepository.createFunction(function);

        // Действие - пытаемся удалить с неправильным user_id
        boolean deleted = functionRepository.deleteFunction(functionId, 999999L);

        // Проверка
        assertFalse(deleted, "Удаление с неправильным user_id должно вернуть false");
        // Проверяем что функция все еще существует
        assertTrue(functionRepository.findById(functionId, testUserId).isPresent(),
                "Функция должна все еще существовать после неудачного удаления");
    }
}