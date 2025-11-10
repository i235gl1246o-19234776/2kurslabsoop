package repository;

import model.entity.Function;
import model.entity.TabulatedFunction;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedFunctionRepositoryTest extends BaseRepositoryTest {

    private UserRepository userRepository;
    private FunctionRepository functionRepository;
    private TabulatedFunctionRepository tabulatedFunctionRepository;
    private Long testUserId;
    private Long testFunctionId;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection dbt = new DatabaseTestConnection();
        userRepository = new UserRepository(dbt);
        functionRepository = new FunctionRepository(dbt);
        tabulatedFunctionRepository = new TabulatedFunctionRepository(dbt);

        // Создание тестового пользователя и функции
        User user = new User("tabulated_test_user", "test_password");
        testUserId = userRepository.createUser(user);

        Function function = new Function(testUserId, "tabular", "test_tabular_func", null);
        testFunctionId = functionRepository.createFunction(function);
    }

    @Test
    void testCreateTabulatedFunction_ShouldCreateNewPoint() throws SQLException {
        // Подготовка
        TabulatedFunction point = new TabulatedFunction(testFunctionId, 1.0, 2.5);

        // Действие
        Long pointId = tabulatedFunctionRepository.createTabulatedFunction(point);

        // Проверка
        assertNotNull(pointId, "ID точки не должен быть null");
        assertTrue(pointId > 0, "ID точки должен быть положительным числом");

        List<TabulatedFunction> points = tabulatedFunctionRepository.findAllByFunctionId(testFunctionId);
        assertEquals(1, points.size(), "Должна быть найдена одна точка");
        assertEquals(1.0, points.get(0).getXVal(), 0.001, "Значение X должно совпадать");
        assertEquals(2.5, points.get(0).getYVal(), 0.001, "Значение Y должно совпадать");
    }

    @Test
    void testCreateMultiplePoints_ShouldStoreAllPoints() throws SQLException {
        // Подготовка
        TabulatedFunction point1 = new TabulatedFunction(testFunctionId, 0.0, 0.0);
        TabulatedFunction point2 = new TabulatedFunction(testFunctionId, 1.0, 1.0);
        TabulatedFunction point3 = new TabulatedFunction(testFunctionId, 2.0, 4.0);

        // Действие
        tabulatedFunctionRepository.createTabulatedFunction(point1);
        tabulatedFunctionRepository.createTabulatedFunction(point2);
        tabulatedFunctionRepository.createTabulatedFunction(point3);

        // Проверка
        List<TabulatedFunction> points = tabulatedFunctionRepository.findAllByFunctionId(testFunctionId);
        assertEquals(3, points.size(), "Должно быть найдено 3 точки");

        // Проверяем что точки отсортированы по X
        assertEquals(0.0, points.get(0).getXVal(), 0.001, "Первая точка должна иметь X=0");
        assertEquals(1.0, points.get(1).getXVal(), 0.001, "Вторая точка должна иметь X=1");
        assertEquals(2.0, points.get(2).getXVal(), 0.001, "Третья точка должна иметь X=2");
    }

    @Test
    void testFindAllByFunctionId_ShouldReturnEmptyList_WhenNoPoints() throws SQLException {
        // Действие
        List<TabulatedFunction> points = tabulatedFunctionRepository.findAllByFunctionId(testFunctionId);

        // Проверка
        assertTrue(points.isEmpty(), "Список точек должен быть пустым");
    }

    @Test
    void testFindByXValue_ShouldReturnPoint_WhenPointExists() throws SQLException {
        // Подготовка
        TabulatedFunction point = new TabulatedFunction(testFunctionId, 5.0, 25.0);
        tabulatedFunctionRepository.createTabulatedFunction(point);

        // Действие
        Optional<TabulatedFunction> foundPoint = tabulatedFunctionRepository.findByXValue(testFunctionId, 5.0);

        // Проверка
        assertTrue(foundPoint.isPresent(), "Точка должна быть найдена");
        assertEquals(5.0, foundPoint.get().getXVal(), 0.001, "Значение X должно совпадать");
        assertEquals(25.0, foundPoint.get().getYVal(), 0.001, "Значение Y должно совпадать");
    }

    @Test
    void testFindByXValue_ShouldReturnEmpty_WhenPointNotExists() throws SQLException {
        // Действие
        Optional<TabulatedFunction> foundPoint = tabulatedFunctionRepository.findByXValue(testFunctionId, 999.0);

        // Проверка
        assertFalse(foundPoint.isPresent(), "Точка не должна быть найдена");
    }

    @Test
    void testFindByXValue_ShouldReturnEmpty_WhenWrongFunctionId() throws SQLException {
        // Подготовка
        TabulatedFunction point = new TabulatedFunction(testFunctionId, 5.0, 25.0);
        tabulatedFunctionRepository.createTabulatedFunction(point);

        // Действие - ищем с неправильным function_id
        Optional<TabulatedFunction> foundPoint = tabulatedFunctionRepository.findByXValue(999999L, 5.0);

        // Проверка
        assertFalse(foundPoint.isPresent(), "Точка не должна быть найдена при неправильном function_id");
    }

    @Test
    void testFindBetweenXValues_ShouldReturnPointsInRange() throws SQLException {
        // Подготовка
        tabulatedFunctionRepository.createTabulatedFunction(new TabulatedFunction(testFunctionId, 1.0, 1.0));
        tabulatedFunctionRepository.createTabulatedFunction(new TabulatedFunction(testFunctionId, 2.0, 4.0));
        tabulatedFunctionRepository.createTabulatedFunction(new TabulatedFunction(testFunctionId, 3.0, 9.0));
        tabulatedFunctionRepository.createTabulatedFunction(new TabulatedFunction(testFunctionId, 4.0, 16.0));
        tabulatedFunctionRepository.createTabulatedFunction(new TabulatedFunction(testFunctionId, 5.0, 25.0));

        // Действие
        List<TabulatedFunction> points = tabulatedFunctionRepository.findBetweenXValues(testFunctionId, 2.0, 4.0);

        // Проверка
        assertEquals(3, points.size(), "Должно быть найдено 3 точки в диапазоне [2, 4]");
        assertTrue(points.stream().allMatch(p -> p.getXVal() >= 2.0 && p.getXVal() <= 4.0),
                "Все точки должны быть в диапазоне [2, 4]");

        // Проверяем конкретные значения
        assertTrue(points.stream().anyMatch(p -> p.getXVal() == 2.0), "Должна быть точка с X=2");
        assertTrue(points.stream().anyMatch(p -> p.getXVal() == 3.0), "Должна быть точка с X=3");
        assertTrue(points.stream().anyMatch(p -> p.getXVal() == 4.0), "Должна быть точка с X=4");
    }

    @Test
    void testFindBetweenXValues_ShouldReturnEmpty_WhenNoPointsInRange() throws SQLException {
        // Подготовка
        tabulatedFunctionRepository.createTabulatedFunction(new TabulatedFunction(testFunctionId, 1.0, 1.0));
        tabulatedFunctionRepository.createTabulatedFunction(new TabulatedFunction(testFunctionId, 2.0, 4.0));

        // Действие
        List<TabulatedFunction> points = tabulatedFunctionRepository.findBetweenXValues(testFunctionId, 5.0, 10.0);

        // Проверка
        assertTrue(points.isEmpty(), "Список точек должен быть пустым для диапазона без точек");
    }

    @Test
    void testUpdateTabulatedFunction_ShouldUpdatePoint() throws SQLException {
        // Подготовка
        TabulatedFunction point = new TabulatedFunction(testFunctionId, 1.0, 1.0);
        Long pointId = tabulatedFunctionRepository.createTabulatedFunction(point);
        point.setId(pointId);

        // Действие
        point.setXVal(2.0);
        point.setYVal(8.0);
        boolean updated = tabulatedFunctionRepository.updateTabulatedFunction(point);

        // Проверка
        assertTrue(updated, "Обновление должно быть успешным");

        Optional<TabulatedFunction> foundPoint = tabulatedFunctionRepository.findByXValue(testFunctionId, 2.0);
        assertTrue(foundPoint.isPresent(), "Точка должна быть найдена после обновления");
        assertEquals(8.0, foundPoint.get().getYVal(), 0.001, "Значение Y должно быть обновлено");
    }

    @Test
    void testUpdateTabulatedFunction_ShouldReturnFalse_WhenPointNotExists() throws SQLException {
        // Подготовка
        TabulatedFunction nonExistentPoint = new TabulatedFunction(testFunctionId, 1.0, 1.0);
        nonExistentPoint.setId(999999L);

        // Действие
        boolean updated = tabulatedFunctionRepository.updateTabulatedFunction(nonExistentPoint);

        // Проверка
        assertFalse(updated, "Обновление несуществующей точки должно вернуть false");
    }

    @Test
    void testDeleteTabulatedFunction_ShouldDeletePoint() throws SQLException {
        // Подготовка
        TabulatedFunction point = new TabulatedFunction(testFunctionId, 1.0, 1.0);
        Long pointId = tabulatedFunctionRepository.createTabulatedFunction(point);

        // Проверка что точка создана
        List<TabulatedFunction> pointsBefore = tabulatedFunctionRepository.findAllByFunctionId(testFunctionId);
        assertEquals(1, pointsBefore.size(), "Точка должна существовать до удаления");

        // Действие
        boolean deleted = tabulatedFunctionRepository.deleteTabulatedFunction(pointId);

        // Проверка
        assertTrue(deleted, "Удаление должно быть успешным");

        List<TabulatedFunction> pointsAfter = tabulatedFunctionRepository.findAllByFunctionId(testFunctionId);
        assertEquals(0, pointsAfter.size(), "Точка не должна существовать после удаления");
    }

    @Test
    void testDeleteTabulatedFunction_ShouldReturnFalse_WhenPointNotExists() throws SQLException {
        // Действие
        boolean deleted = tabulatedFunctionRepository.deleteTabulatedFunction(999999L);

        // Проверка
        assertFalse(deleted, "Удаление несуществующей точки должно вернуть false");
    }

    @Test
    void testDeleteAllTabulatedFunctions_ShouldDeleteAllPoints() throws SQLException {
        // Подготовка
        tabulatedFunctionRepository.createTabulatedFunction(new TabulatedFunction(testFunctionId, 1.0, 1.0));
        tabulatedFunctionRepository.createTabulatedFunction(new TabulatedFunction(testFunctionId, 2.0, 4.0));
        tabulatedFunctionRepository.createTabulatedFunction(new TabulatedFunction(testFunctionId, 3.0, 9.0));

        // Проверка что точки созданы
        List<TabulatedFunction> pointsBefore = tabulatedFunctionRepository.findAllByFunctionId(testFunctionId);
        assertEquals(3, pointsBefore.size(), "Должно быть 3 точки до удаления");

        // Действие
        boolean deleted = tabulatedFunctionRepository.deleteAllTabulatedFunctions(testFunctionId);

        // Проверка
        assertTrue(deleted, "Удаление всех точек должно быть успешным");

        List<TabulatedFunction> pointsAfter = tabulatedFunctionRepository.findAllByFunctionId(testFunctionId);
        assertEquals(0, pointsAfter.size(), "Не должно быть точек после удаления");
    }

}