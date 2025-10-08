package concurrent;

import functions.*;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для SynchronizedTabulatedFunction")
class SynchronizedTabulatedFunctionTest {

    private TabulatedFunction baseFunction;
    private SynchronizedTabulatedFunction syncFunction;

    @BeforeEach
    @DisplayName("Инициализация тестовых данных")
    void setUp() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0, 50.0};
        baseFunction = new LinkedListTabulatedFunctionFactory().create(xValues, yValues);
        syncFunction = new SynchronizedTabulatedFunction(baseFunction);
    }

    @Test
    @DisplayName("Конструктор должен корректно создавать объект")
    void testConstructor() {
        assertNotNull(syncFunction, "Объект должен быть создан");
        assertEquals(5, syncFunction.getCount(), "Количество точек должно соответствовать исходной функции");
    }

    @Test
    @DisplayName("Метод getCount должен возвращать правильное количество точек")
    void testGetCount() {
        assertEquals(5, syncFunction.getCount(), "Должно быть 5 точек");
    }

    @Test
    @DisplayName("Метод getX должен возвращать корректные значения X по индексу")
    void testGetX() {
        assertAll("Проверка значений X",
                () -> assertEquals(1.0, syncFunction.getX(0), 1e-9, "X[0] должен быть 1.0"),
                () -> assertEquals(3.0, syncFunction.getX(2), 1e-9, "X[2] должен быть 3.0"),
                () -> assertEquals(5.0, syncFunction.getX(4), 1e-9, "X[4] должен быть 5.0")
        );
    }

    @Test
    @DisplayName("Метод getY должен возвращать корректные значения Y по индексу")
    void testGetY() {
        assertAll("Проверка значений Y",
                () -> assertEquals(10.0, syncFunction.getY(0), 1e-9, "Y[0] должен быть 10.0"),
                () -> assertEquals(30.0, syncFunction.getY(2), 1e-9, "Y[2] должен быть 30.0"),
                () -> assertEquals(50.0, syncFunction.getY(4), 1e-9, "Y[4] должен быть 50.0")
        );
    }

    @Test
    @DisplayName("Метод setY должен корректно изменять значения Y")
    void testSetY() {
        syncFunction.setY(2, 100.0);
        assertEquals(100.0, syncFunction.getY(2), 1e-9, "Y[2] должен быть изменен на 100.0");

        syncFunction.setY(0, -5.0);
        assertEquals(-5.0, syncFunction.getY(0), 1e-9, "Y[0] должен быть изменен на -5.0");
    }

    @Test
    @DisplayName("Метод leftBound должен возвращать левую границу функции")
    void testLeftBound() {
        assertEquals(1.0, syncFunction.leftBound(), 1e-9, "Левая граница должна быть 1.0");
    }

    @Test
    @DisplayName("Метод rightBound должен возвращать правую границу функции")
    void testRightBound() {
        assertEquals(5.0, syncFunction.rightBound(), 1e-9, "Правая граница должна быть 5.0");
    }

    @Test
    @DisplayName("Метод indexOfX должен корректно находить индексы по значению X")
    void testIndexOfX() {
        assertAll("Поиск индексов по X",
                () -> assertEquals(0, syncFunction.indexOfX(1.0), "Индекс для X=1.0 должен быть 0"),
                () -> assertEquals(2, syncFunction.indexOfX(3.0), "Индекс для X=3.0 должен быть 2"),
                () -> assertEquals(-1, syncFunction.indexOfX(10.0), "Индекс для несуществующего X должен быть -1")
        );
    }

    @Test
    @DisplayName("Метод indexOfY должен корректно находить индексы по значению Y")
    void testIndexOfY() {
        assertAll("Поиск индексов по Y",
                () -> assertEquals(0, syncFunction.indexOfY(10.0), "Индекс для Y=10.0 должен быть 0"),
                () -> assertEquals(2, syncFunction.indexOfY(30.0), "Индекс для Y=30.0 должен быть 2"),
                () -> assertEquals(-1, syncFunction.indexOfY(100.0), "Индекс для несуществующего Y должен быть -1")
        );
    }

    @Test
    @DisplayName("Метод apply должен корректно вычислять значение функции")
    void testApply() {
        assertAll("Вычисление значения функции",
                () -> assertEquals(10.0, syncFunction.apply(1.0), 1e-9, "f(1.0) должна быть 10.0"),
                () -> assertEquals(30.0, syncFunction.apply(3.0), 1e-9, "f(3.0) должна быть 30.0"),
                () -> assertEquals(25.0, syncFunction.apply(2.5), 1e-9, "f(2.5) должна быть 25.0 (интерполяция)")
        );
    }

    @Test
    @DisplayName("Итератор должен корректно обходить все точки функции")
    void testIterator() {
        Iterator<Point> iterator = syncFunction.iterator();
        assertTrue(iterator.hasNext(), "Итератор должен иметь элементы");

        int count = 0;
        for (Point point : syncFunction) {
            assertNotNull(point, "Точка не должна быть null");
            assertEquals(baseFunction.getX(count), point.x, 1e-9,
                    "X координата точки " + count + " должна совпадать");
            assertEquals(baseFunction.getY(count), point.y, 1e-9,
                    "Y координата точки " + count + " должна совпадать");
            count++;
        }

        assertEquals(5, count, "Должно быть проитерировано 5 точек");
    }

    @Test
    @DisplayName("Метод doSynchronously должен выполнять атомарные операции")
    void testDoSynchronously() {
        String result = syncFunction.doSynchronously(func -> {
            func.setY(0, 100.0);
            func.setY(1, 200.0);
            return "Operation completed";
        });

        assertEquals("Operation completed", result, "Результат операции должен быть корректным");
        assertEquals(100.0, syncFunction.getY(0), 1e-9, "Y[0] должен быть изменен на 100.0");
        assertEquals(200.0, syncFunction.getY(1), 1e-9, "Y[1] должен быть изменен на 200.0");
    }

    @Test
    @DisplayName("Класс должен работать с ArrayTabulatedFunction")
    void testWithArrayTabulatedFunction() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};

        TabulatedFunction arrayFunction = new ArrayTabulatedFunctionFactory().create(xValues, yValues);
        SynchronizedTabulatedFunction syncArrayFunction = new SynchronizedTabulatedFunction(arrayFunction);

        assertAll("Проверка работы с ArrayTabulatedFunction",
                () -> assertEquals(3, syncArrayFunction.getCount(), "Количество точек должно быть 3"),
                () -> assertEquals(0.0, syncArrayFunction.apply(0.0), 1e-9, "f(0.0) должна быть 0.0"),
                () -> assertEquals(1.0, syncArrayFunction.apply(1.0), 1e-9, "f(1.0) должна быть 1.0"),
                () -> assertEquals(4.0, syncArrayFunction.apply(2.0), 1e-9, "f(2.0) должна быть 4.0")
        );
    }

    @Test
    @DisplayName("Комплексная проверка всех методов вместе")
    void testMultipleOperations() {
        assertAll("Комплексная проверка",
                () -> assertEquals(5, syncFunction.getCount(), "Количество точек"),
                () -> assertEquals(1.0, syncFunction.leftBound(), 1e-9, "Левая граница"),
                () -> assertEquals(5.0, syncFunction.rightBound(), 1e-9, "Правая граница")
        );

        syncFunction.setY(1, 999.0);
        assertAll("После изменения Y[1]",
                () -> assertEquals(999.0, syncFunction.getY(1), 1e-9, "Y[1] должен быть изменен"),
                () -> assertEquals(1, syncFunction.indexOfY(999.0), "Индекс для нового Y")
        );

        syncFunction.setY(1, 25.0);
        assertEquals(25.0, syncFunction.apply(2.0), 1e-9, "f(2.0) после изменения должна быть 25.0");
    }

    @Test
    @DisplayName("Итератор должен корректно обходить все точки в правильном порядке")
    void testIteratorTraversalInOrder() {
        Iterator<Point> iterator = syncFunction.iterator();

        assertTrue(iterator.hasNext(), "Итератор должен иметь элементы");

        Point point1 = iterator.next();
        assertEquals(1.0, point1.x, 1e-9, "Первая точка X должна быть 1.0");
        assertEquals(10.0, point1.y, 1e-9, "Первая точка Y должна быть 10.0");

        Point point2 = iterator.next();
        assertEquals(2.0, point2.x, 1e-9, "Вторая точка X должна быть 2.0");
        assertEquals(20.0, point2.y, 1e-9, "Вторая точка Y должна быть 20.0");

        Point point3 = iterator.next();
        assertEquals(3.0, point3.x, 1e-9, "Третья точка X должна быть 3.0");
        assertEquals(30.0, point3.y, 1e-9, "Третья точка Y должна быть 30.0");

        Point point4 = iterator.next();
        assertEquals(4.0, point4.x, 1e-9, "Четвертая точка X должна быть 4.0");
        assertEquals(40.0, point4.y, 1e-9, "Четвертая точка Y должна быть 40.0");

        Point point5 = iterator.next();
        assertEquals(5.0, point5.x, 1e-9, "Пятая точка X должна быть 5.0");
        assertEquals(50.0, point5.y, 1e-9, "Пятая точка Y должна быть 50.0");

        assertFalse(iterator.hasNext(), "После всех точек hasNext() должен вернуть false");
    }

    @Test
    @DisplayName("Итератор должен работать с циклом foreach")
    void testIteratorWithForEach() {
        int count = 0;
        for (Point point : syncFunction) {
            assertNotNull(point, "Точка не должна быть null");
            assertEquals(baseFunction.getX(count), point.x, 1e-9,
                    "X координата точки " + count + " должна совпадать с исходной функцией");
            assertEquals(baseFunction.getY(count), point.y, 1e-9,
                    "Y координата точки " + count + " должна совпадать с исходной функцией");
            count++;
        }

        assertEquals(5, count, "Должно быть проитерировано 5 точек");
    }

    @Test
    @DisplayName("Итератор должен бросать NoSuchElementException при next() после конца")
    void testIteratorThrowsNoSuchElementException() {
        Iterator<Point> iterator = syncFunction.iterator();

        while (iterator.hasNext()) {
            iterator.next();
        }

        assertThrows(NoSuchElementException.class, iterator::next,
                "next() после конца итерации должен бросать NoSuchElementException");
    }

    @Test
    @DisplayName("Итератор должен создавать независимую копию данных")
    void testIteratorCreatesDataCopy() {
        Iterator<Point> iterator = syncFunction.iterator();

        syncFunction.setY(0, 100.0);
        syncFunction.setY(2, 300.0);
        syncFunction.setY(4, 500.0);

        Point point1 = iterator.next();
        assertEquals(10.0, point1.y, 1e-9, "Итератор должен содержать исходное значение Y[0] = 10.0");

        iterator.next();

        Point point3 = iterator.next();
        assertEquals(30.0, point3.y, 1e-9, "Итератор должен содержать исходное значение Y[2] = 30.0");

        iterator.next();

        Point point5 = iterator.next();
        assertEquals(50.0, point5.y, 1e-9, "Итератор должен содержать исходное значение Y[4] = 50.0");
    }


    @Test
    @DisplayName("doSynchronously: операция возвращает double (составное вычисление)")
    public void testDoSynchronouslyReturnsDouble() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {1.0, 2.0, 3.0};
        TabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(base);

        Double result = syncFunc.doSynchronously(func -> {
            double first = func.getY(0);
            double last = func.getY(func.getCount() - 1);
            return first + last;
        });

        assertEquals(4.0, result, 1e-10);
    }

    @Test
    @DisplayName("doSynchronously: операция возвращает Integer (количество точек)")
    public void testDoSynchronouslyReturnsInteger() {
        double[] x = {0.0, 1.0};
        double[] y = {5.0, 10.0};
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(
                new ArrayTabulatedFunction(x, y)
        );

        Integer count = syncFunc.doSynchronously(func -> func.getCount());

        assertEquals(2, count);
    }

    @Test
    @DisplayName("doSynchronously: операция с Void (изменение значений без возврата)")
    public void testDoSynchronouslyWithVoid() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {1.0, 1.0, 1.0};
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(
                new ArrayTabulatedFunction(x, y)
        );

        Void result = syncFunc.doSynchronously(func -> {
            for (int i = 0; i < func.getCount(); i++) {
                func.setY(i, func.getY(i) * 2);
            }
            return null; // для Void
        });

        assertNull(result);

        assertEquals(2.0, syncFunc.getY(0), 1e-10);
        assertEquals(2.0, syncFunc.getY(1), 1e-10);
        assertEquals(2.0, syncFunc.getY(2), 1e-10);
    }

    @Test
    @DisplayName("doSynchronously: составная операция — безопасное изменение с проверкой")
    public void testCompositeOperationSafeUpdate() {
        double[] x = {0.0, 1.0};
        double[] y = {4.0, 6.0};
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(
                new ArrayTabulatedFunction(x, y)
        );

        Boolean updated = syncFunc.doSynchronously(func -> {
            double y0 = func.getY(0);
            double y1 = func.getY(1);
            if ((y0 + y1) % 2 == 0) {
                func.setY(0, y0 + 1);
                func.setY(1, y1 + 1);
                return true;
            }
            return false;
        });

        assertTrue(updated);
        assertEquals(5.0, syncFunc.getY(0), 1e-10);
        assertEquals(7.0, syncFunc.getY(1), 1e-10);
    }
}
