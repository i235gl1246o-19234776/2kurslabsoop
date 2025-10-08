package operations;

import exception.InconsistentFunctionsException;
import functions.*;
import functions.factory.*;
import functions.factory.TabulatedFunctionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedFunctionOperationServiceTest {

    @Test
    @DisplayName("asPoints должен корректно преобразовывать ArrayTabulatedFunction")
    void testAsPointsWithArrayTabulatedFunction() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Point[] points = TabulatedFunctionOperationService.asPoints(function);

        assertNotNull(points);
        assertEquals(4, points.length);

        assertEquals(1.0, points[0].x, 0.0001);
        assertEquals(10.0, points[0].y, 0.0001);
        assertEquals(2.0, points[1].x, 0.0001);
        assertEquals(20.0, points[1].y, 0.0001);
        assertEquals(3.0, points[2].x, 0.0001);
        assertEquals(30.0, points[2].y, 0.0001);
        assertEquals(4.0, points[3].x, 0.0001);
        assertEquals(40.0, points[3].y, 0.0001);
    }

    @Test
    @DisplayName("asPoints должен корректно преобразовывать LinkedListTabulatedFunction")
    void testAsPointsWithLinkedListTabulatedFunction() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        Point[] points = TabulatedFunctionOperationService.asPoints(function);

        assertNotNull(points);
        assertEquals(3, points.length);

        assertEquals(1.0, points[0].x, 0.0001);
        assertEquals(10.0, points[0].y, 0.0001);
        assertEquals(2.0, points[1].x, 0.0001);
        assertEquals(20.0, points[1].y, 0.0001);
        assertEquals(3.0, points[2].x, 0.0001);
        assertEquals(30.0, points[2].y, 0.0001);
    }

    @Test
    @DisplayName("asPoints должен бросать исключение для null функции")
    void testAsPointsWithNullFunction() {
        assertThrows(IllegalArgumentException.class, () -> {
            TabulatedFunctionOperationService.asPoints(null);
        });
    }

    @Test
    @DisplayName("asPoints должен корректно работать с минимальным количеством точек (2 точки)")
    void testAsPointsWithMinimumPoints() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0};

        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);

        Point[] arrayPoints = TabulatedFunctionOperationService.asPoints(arrayFunction);
        Point[] linkedListPoints = TabulatedFunctionOperationService.asPoints(linkedListFunction);

        assertEquals(2, arrayPoints.length);
        assertEquals(2, linkedListPoints.length);

        assertEquals(1.0, arrayPoints[0].x, 0.0001);
        assertEquals(10.0, arrayPoints[0].y, 0.0001);
        assertEquals(2.0, arrayPoints[1].x, 0.0001);
        assertEquals(20.0, arrayPoints[1].y, 0.0001);

        assertEquals(1.0, linkedListPoints[0].x, 0.0001);
        assertEquals(10.0, linkedListPoints[0].y, 0.0001);
        assertEquals(2.0, linkedListPoints[1].x, 0.0001);
        assertEquals(20.0, linkedListPoints[1].y, 0.0001);
    }

    @Test
    @DisplayName("asPoints должен создавать новые независимые объекты Point")
    void testAsPointsCreatesNewObjects() {
        double[] xVal = {1.0, 2.0};
        double[] yVal = {10.0, 20.0};
        TabulatedFunction function = new ArrayTabulatedFunction(xVal, yVal);

        Point[] points = TabulatedFunctionOperationService.asPoints(function);

        // Проверяем, что это новые объекты (не те же ссылки)
        for (int i = 0; i < points.length; i++) {
            assertNotSame(function.iterator().next(), points[i]);
        }

        // Проверяем, что значения корректны
        assertEquals(1.0, points[0].x, 0.0001);
        assertEquals(10.0, points[0].y, 0.0001);
        assertEquals(2.0, points[1].x, 0.0001);
        assertEquals(20.0, points[1].y, 0.0001);
    }

    @Test
    @DisplayName("asPoints должен корректно работать с отрицательными значениями")
    void testAsPointsWithNegativeValues() {
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues = {4.0, 1.0, 0.0, 1.0, 4.0};

        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        Point[] points = TabulatedFunctionOperationService.asPoints(arrayFunction);

        assertEquals(5, points.length);
        assertEquals(-2.0, points[0].x, 0.0001);
        assertEquals(4.0, points[0].y, 0.0001);
        assertEquals(0.0, points[2].x, 0.0001);
        assertEquals(0.0, points[2].y, 0.0001);
        assertEquals(2.0, points[4].x, 0.0001);
        assertEquals(4.0, points[4].y, 0.0001);
    }

    @Test
    @DisplayName("asPoints должен корректно работать с десятичными значениями")
    void testAsPointsWithDecimalValues() {
        double[] xValues = {0.1, 0.2, 0.3, 0.4, 0.5};
        double[] yValues = {0.01, 0.04, 0.09, 0.16, 0.25};

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        Point[] points = TabulatedFunctionOperationService.asPoints(function);

        assertEquals(5, points.length);
        assertEquals(0.1, points[0].x, 0.0000001);
        assertEquals(0.01, points[0].y, 0.0000001);
        assertEquals(0.3, points[2].x, 0.0000001);
        assertEquals(0.09, points[2].y, 0.0000001);
        assertEquals(0.5, points[4].x, 0.0000001);
        assertEquals(0.25, points[4].y, 0.0000001);
    }

    @Test
    @DisplayName("asPoints должен корректно работать с большими массивами")
    void testAsPointsWithLargeArrays() {
        int size = 100;
        double[] xValues = new double[size];
        double[] yValues = new double[size];

        for (int i = 0; i < size; i++) {
            xValues[i] = i;
            yValues[i] = i * 10;
        }

        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        Point[] points = TabulatedFunctionOperationService.asPoints(arrayFunction);

        assertEquals(size, points.length);
        assertEquals(0.0, points[0].x, 0.0001);
        assertEquals(0.0, points[0].y, 0.0001);
        assertEquals(99.0, points[99].x, 0.0001);
        assertEquals(990.0, points[99].y, 0.0001);
    }

    @Test
    @DisplayName("asPoints должен согласовываться с итератором функции")
    void testAsPointsConsistentWithIterator() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};

        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);

        Point[] arrayPoints = TabulatedFunctionOperationService.asPoints(arrayFunction);
        Point[] linkedListPoints = TabulatedFunctionOperationService.asPoints(linkedListFunction);

        int i = 0;
        for (Point point : arrayFunction) {
            assertEquals(point.x, arrayPoints[i].x, 0.0001);
            assertEquals(point.y, arrayPoints[i].y, 0.0001);
            i++;
        }

        i = 0;
        for (Point point : linkedListFunction) {
            assertEquals(point.x, linkedListPoints[i].x, 0.0001);
            assertEquals(point.y, linkedListPoints[i].y, 0.0001);
            i++;
        }
    }

    private static final double DELTA = 1e-10;

    private static final double[] X1 = {0.0, 1.0, 2.0, 3.0};
    private static final double[] Y1 = {1.0, 3.0, 5.0, 7.0}; // f(x) = 2x + 1
    private static final double[] Y2 = {0.0, 1.0, 4.0, 9.0}; // f(x) = x^2

    private static Stream<TabulatedFunctionFactory> provideFactories() {
        return Stream.of(new ArrayTabulatedFunctionFactory(), new LinkedListTabulatedFunctionFactory());
    }

    @ParameterizedTest
    @MethodSource("provideFactories")
    @DisplayName("Сложение двух одинаковых функций")
    void testAddSameTypeFunctions(TabulatedFunctionFactory factory) {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(factory);

        TabulatedFunction f1 = factory.create(X1, Y1);
        TabulatedFunction f2 = factory.create(X1, Y2);

        TabulatedFunction result = service.add(f1, f2);

        assertEquals(X1.length, result.getCount());
        for (int i = 0; i < X1.length; i++) {
            double expected = Y1[i] + Y2[i];
            assertEquals(expected, result.getY(i), DELTA, "Неверный результат сложения в точке " + i);
        }

        // Проверка типа результата
        if (factory instanceof ArrayTabulatedFunctionFactory) {
            assertTrue(result instanceof ArrayTabulatedFunction);
        } else if (factory instanceof LinkedListTabulatedFunctionFactory) {
            assertTrue(result instanceof LinkedListTabulatedFunction);
        }
    }

    @Test
    @DisplayName("Вычитание функций разных типов (Array - LinkedList)")
    void testSubtractDifferentTypes() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction f1 = new ArrayTabulatedFunction(X1, Y1);
        TabulatedFunction f2 = new LinkedListTabulatedFunction(X1, Y2);

        TabulatedFunction result = service.subtract(f1, f2);

        assertEquals(X1.length, result.getCount());
        for (int i = 0; i < X1.length; i++) {
            double expected = Y1[i] - Y2[i];
            assertEquals(expected, result.getY(i), DELTA, "Неверный результат вычитания в точке " + i);
        }

        // По умолчанию используется Array фабрика
        assertTrue(result instanceof ArrayTabulatedFunction);
    }

    @Test
    @DisplayName("Сложение функций разных типов (LinkedList + Array)")
    void testAddDifferentTypes() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(factory);

        TabulatedFunction f1 = new LinkedListTabulatedFunction(X1, Y1);
        TabulatedFunction f2 = new ArrayTabulatedFunction(X1, Y2);

        TabulatedFunction result = service.add(f1, f2);

        assertEquals(X1.length, result.getCount());
        for (int i = 0; i < X1.length; i++) {
            double expected = Y1[i] + Y2[i];
            assertEquals(expected, result.getY(i), DELTA);
        }

        assertTrue(result instanceof LinkedListTabulatedFunction);
    }

    @Test
    @DisplayName("Исключение при разном количестве точек")
    void testInconsistentCountThrowsException() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction f1 = new ArrayTabulatedFunction(X1, Y1);
        TabulatedFunction f2 = new ArrayTabulatedFunction(new double[]{0.0, 1.0}, // только 2 точки
                new double[]{1.0, 2.0});

        InconsistentFunctionsException exception = assertThrows(InconsistentFunctionsException.class, () -> service.add(f1, f2), "Должно бросаться исключение при разном количестве точек");

    }

    @Test
    @DisplayName("Исключение при несовпадении X-значений")
    void testInconsistentXValuesThrowsException() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction f1 = new ArrayTabulatedFunction(X1, Y1);
        double[] x2 = {0.0, 1.0, 2.0, 4.0}; // Последнее значение отличается!
        TabulatedFunction f2 = new ArrayTabulatedFunction(x2, Y2);

        assertThrows(InconsistentFunctionsException.class, () -> service.add(f1, f2), "Должно бросаться исключение при несовпадении X-значений");


    }

    @Test
    @DisplayName("Исключение при передаче null")
    void testNullFunctionThrowsException() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction f = new ArrayTabulatedFunction(X1, Y1);

        assertThrows(IllegalArgumentException.class, () -> service.add(null, f), "Должно бросаться исключение при null первой функции");

        assertThrows(IllegalArgumentException.class, () -> service.add(f, null), "Должно бросаться исключение при null второй функции");
    }

    @Test
    @DisplayName("Проверка сеттера и геттера фабрики")
    void testFactorySetterGetter() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();
        service.setFactory(newFactory);

        assertSame(newFactory, service.getFactory(), "Геттер должен возвращать установленную фабрику");
    }

    @Test
    @DisplayName("Конструктор по умолчанию использует ArrayTabulatedFunctionFactory")
    void testDefaultConstructorUsesArrayFactory() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertTrue(service.getFactory() instanceof ArrayTabulatedFunctionFactory, "По умолчанию должна использоваться Array фабрика");
    }

    private TabulatedFunctionOperationService service;
    private TabulatedFunction a;
    private TabulatedFunction b;

    @BeforeEach
    void setUp() {
        service = new TabulatedFunctionOperationService();
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValuesA = {2.0, 4.0, 6.0, 8.0};
        double[] yValuesB = {1.0, 2.0, 3.0, 4.0};

        a = new ArrayTabulatedFunction(xValues, yValuesA);
        b = new ArrayTabulatedFunction(xValues, yValuesB);
    }

    @Test
    @DisplayName("Умножение двух функций (Array)")
    void testMultiplyArrayFunctions() {
        TabulatedFunction result = service.multiply(a, b);

        assertEquals(4, result.getCount());
        assertEquals(2.0, result.getY(0), 0.0001);  // 2 * 1 = 2
        assertEquals(8.0, result.getY(1), 0.0001);  // 4 * 2 = 8
        assertEquals(18.0, result.getY(2), 0.0001); // 6 * 3 = 18
        assertEquals(32.0, result.getY(3), 0.0001); // 8 * 4 = 32
    }

    @Test
    @DisplayName("Умножение двух функций (LinkedList)")
    void testMultiplyLinkedListFunctions() {
        service.setFactory(new LinkedListTabulatedFunctionFactory());
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValuesA = {3.0, 6.0, 9.0};
        double[] yValuesB = {2.0, 4.0, 6.0};

        TabulatedFunction a = new LinkedListTabulatedFunction(xValues, yValuesA);
        TabulatedFunction b = new LinkedListTabulatedFunction(xValues, yValuesB);

        TabulatedFunction result = service.multiply(a, b);

        assertTrue(result instanceof LinkedListTabulatedFunction);
        assertEquals(6.0, result.getY(0), 0.0001);  // 3 * 2 = 6
        assertEquals(24.0, result.getY(1), 0.0001); // 6 * 4 = 24
        assertEquals(54.0, result.getY(2), 0.0001); // 9 * 6 = 54
    }

    @Test
    @DisplayName("Деление двух функций")
    void testDivideFunctions() {
        TabulatedFunction result = service.divide(a, b);

        assertEquals(4, result.getCount());
        assertEquals(2.0, result.getY(0), 0.0001);  // 2 / 1 = 2
        assertEquals(2.0, result.getY(1), 0.0001);  // 4 / 2 = 2
        assertEquals(2.0, result.getY(2), 0.0001);  // 6 / 3 = 2
        assertEquals(2.0, result.getY(3), 0.0001);  // 8 / 4 = 2
    }

    @Test
    @DisplayName("Деление с дробными результатами")
    void testDivideWithFractionalResults() {
        double[] yValuesC = {1.0, 3.0, 5.0, 7.0};
        TabulatedFunction c = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0, 4.0}, yValuesC);

        TabulatedFunction result = service.divide(c, b);

        assertEquals(1.0, result.getY(0), 0.0001);  // 1 / 1 = 1
        assertEquals(1.5, result.getY(1), 0.0001);  // 3 / 2 = 1.5
        assertEquals(1.6667, result.getY(2), 0.001); // 5 / 3 ≈ 1.6667
        assertEquals(1.75, result.getY(3), 0.0001);  // 7 / 4 = 1.75
    }

    @Test
    @DisplayName("Деление на ноль должно бросать исключение")
    void testDivideByZeroThrowsException() {
        double[] yValuesZero = {1.0, 0.0, 3.0, 4.0};
        TabulatedFunction zeroFunction = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0, 4.0}, yValuesZero);

        assertThrows(ArithmeticException.class, () -> {
            service.divide(a, zeroFunction);
        });
    }

    @Test
    @DisplayName("Умножение функций с отрицательными значениями")
    void testMultiplyWithNegativeValues() {
        double[] yValuesNeg = {-1.0, -2.0, -3.0, -4.0};
        TabulatedFunction negFunction = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0, 4.0}, yValuesNeg);

        TabulatedFunction result = service.multiply(a, negFunction);

        assertEquals(-2.0, result.getY(0), 0.0001);  // 2 * -1 = -2
        assertEquals(-8.0, result.getY(1), 0.0001);  // 4 * -2 = -8
        assertEquals(-18.0, result.getY(2), 0.0001); // 6 * -3 = -18
        assertEquals(-32.0, result.getY(3), 0.0001); // 8 * -4 = -32
    }

    @Test
    @DisplayName("Умножение и деление с разными фабриками")
    void testOperationsWithDifferentFactories() {
        // Тестируем с Array фабрикой
        service.setFactory(new ArrayTabulatedFunctionFactory());
        TabulatedFunction result1 = service.multiply(a, b);
        assertTrue(result1 instanceof ArrayTabulatedFunction);

        service.setFactory(new LinkedListTabulatedFunctionFactory());
        TabulatedFunction result2 = service.divide(a, b);
        assertTrue(result2 instanceof LinkedListTabulatedFunction);
    }

    @Test
    @DisplayName("Несовпадение размеров функций при умножении/делении")
    void testInconsistentSizesForMultiplyDivide() {
        TabulatedFunction smallFunction = new ArrayTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 2.0});

        assertThrows(InconsistentFunctionsException.class, () -> {
            service.multiply(a, smallFunction);
        });

        assertThrows(InconsistentFunctionsException.class, () -> {
            service.divide(a, smallFunction);
        });
    }

    @Test
    @DisplayName("Несовпадение X-координат при умножении/делении")
    void testInconsistentXValuesForMultiplyDivide() {
        TabulatedFunction differentX = new ArrayTabulatedFunction(new double[]{1.0, 2.5, 3.0, 4.0}, new double[]{1.0, 2.0, 3.0, 4.0});

        assertThrows(InconsistentFunctionsException.class, () -> {
            service.multiply(a, differentX);
        });

        assertThrows(InconsistentFunctionsException.class, () -> {
            service.divide(a, differentX);
        });
    }

    @Test
    @DisplayName("Комбинированные операции: (a * b) / a = b")
    void testCombinedOperations() {
        TabulatedFunction product = service.multiply(a, b);
        TabulatedFunction result = service.divide(product, a);

        // Проверяем, что (a * b) / a = b
        for (int i = 0; i < b.getCount(); i++) {
            assertEquals(b.getY(i), result.getY(i), 0.0001);
        }
    }

    @Test
    @DisplayName("Умножение на нулевую функцию")
    void testMultiplyByZeroFunction() {
        double[] yValuesZero = {0.0, 0.0, 0.0, 0.0};
        TabulatedFunction zeroFunction = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0, 4.0}, yValuesZero);

        TabulatedFunction result = service.multiply(a, zeroFunction);

        for (int i = 0; i < result.getCount(); i++) {
            assertEquals(0.0, result.getY(i), 0.0001);
        }
    }


    @ParameterizedTest
    @MethodSource("provideFactories")
    @DisplayName("createUnmodifiable возвращает обёрнутую в UnmodifiableTabulatedFunction")
    void testCreateUnmodifiableReturnsUnmodifiable(TabulatedFunctionFactory factory) {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {4.0, 5.0, 6.0};

        var result = factory.createUnmodifiable(x, y);

        assertTrue(result instanceof UnmodifiableTabulatedFunction, "Результат должен быть обёрнут в UnmodifiableTabulatedFunction");
    }

    @ParameterizedTest
    @MethodSource("provideFactories")
    @DisplayName("createUnmodifiable сохраняет значения x и y")
    void testCreateUnmodifiablePreservesValues(TabulatedFunctionFactory factory) {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {1.0, 3.0, 5.0};

        var unmodifiable = factory.createUnmodifiable(x, y);

        assertEquals(3, unmodifiable.getCount());
        for (int i = 0; i < 3; i++) {
            assertEquals(x[i], unmodifiable.getX(i), 1e-10);
            assertEquals(y[i], unmodifiable.getY(i), 1e-10);
        }
    }

    @ParameterizedTest
    @MethodSource("provideFactories")
    @DisplayName("createUnmodifiable запрещает изменение через setY")
    void testCreateUnmodifiableBlocksModification(TabulatedFunctionFactory factory) {
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 4.0};

        var unmodifiable = factory.createUnmodifiable(x, y);

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> unmodifiable.setY(0, 999.0), "Должно запрещать изменение");

        assertEquals("Нельзя это использовать", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideFactories")
    @DisplayName("createUnmodifiable работает с пустыми массивами")
    void testCreateUnmodifiableWithEmptyArrays(TabulatedFunctionFactory factory) {
        double[] x = {};
        double[] y = {};
        assertThrows(IllegalArgumentException.class, () -> factory.createUnmodifiable(x, y));
    }

    @ParameterizedTest
    @MethodSource("provideFactories")
    @DisplayName("createUnmodifiable бросает исключение при null-массивах")
    void testCreateUnmodifiableThrowsOnNullArrays(TabulatedFunctionFactory factory) {
        double[] valid = {1.0, 2.0};

        assertThrows(IllegalArgumentException.class, () -> factory.createUnmodifiable(null, valid), "Должно бросаться исключение при null xValues");

        assertThrows(IllegalArgumentException.class, () -> factory.createUnmodifiable(valid, null), "Должно бросаться исключение при null yValues");
    }

    @ParameterizedTest
    @MethodSource("provideFactories")
    @DisplayName("createUnmodifiable бросает исключение при разных длинах массивов")
    void testCreateUnmodifiableThrowsOnDifferentLengths(TabulatedFunctionFactory factory) {
        double[] x = {1.0, 2.0};
        double[] y = {3.0};

        assertThrows(IllegalArgumentException.class, () -> factory.createUnmodifiable(x, y), "Должно бросаться исключение при несовпадении длин");
    }


    @Test
    void constructorThrowsOnNullFactory() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new TabulatedFunctionOperationService(null));
        assertEquals("Factory cannot be null", exception.getMessage());
    }

    @Test
    void setFactoryThrowsOnNull() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory() // или любая валидная фабрика
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.setFactory(null));
        assertEquals("Factory cannot be null", exception.getMessage());
    }
}
