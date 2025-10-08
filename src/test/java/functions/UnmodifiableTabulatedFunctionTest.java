package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class UnmodifiableTabulatedFunctionTest {

    private TabulatedFunction arrayFunction;
    private TabulatedFunction linkedListFunction;
    private TabulatedFunction unmodifiableArrayFunction;
    private TabulatedFunction unmodifiableLinkedListFunction;

    @BeforeEach
    void setUp() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0, 50.0};

        arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);

        unmodifiableArrayFunction = new UnmodifiableTabulatedFunction(arrayFunction);
        unmodifiableLinkedListFunction = new UnmodifiableTabulatedFunction(linkedListFunction);
    }

    @Test
    @DisplayName("Тест получения количества точек для обёрнутых функций")
    void testGetCount() {
        assertEquals(5, unmodifiableArrayFunction.getCount());
        assertEquals(5, unmodifiableLinkedListFunction.getCount());
    }

    @Test
    @DisplayName("Тест получения значений X по индексу для обёрнутых функций")
    void testGetX() {
        assertEquals(1.0, unmodifiableArrayFunction.getX(0), 1e-10);
        assertEquals(3.0, unmodifiableArrayFunction.getX(2), 1e-10);
        assertEquals(5.0, unmodifiableArrayFunction.getX(4), 1e-10);

        assertEquals(1.0, unmodifiableLinkedListFunction.getX(0), 1e-10);
        assertEquals(3.0, unmodifiableLinkedListFunction.getX(2), 1e-10);
        assertEquals(5.0, unmodifiableLinkedListFunction.getX(4), 1e-10);
    }

    @Test
    @DisplayName("Тест получения значений Y по индексу для обёрнутых функций")
    void testGetY() {
        assertEquals(10.0, unmodifiableArrayFunction.getY(0), 1e-10);
        assertEquals(30.0, unmodifiableArrayFunction.getY(2), 1e-10);
        assertEquals(50.0, unmodifiableArrayFunction.getY(4), 1e-10);

        assertEquals(10.0, unmodifiableLinkedListFunction.getY(0), 1e-10);
        assertEquals(30.0, unmodifiableLinkedListFunction.getY(2), 1e-10);
        assertEquals(50.0, unmodifiableLinkedListFunction.getY(4), 1e-10);
    }

    @Test
    @DisplayName("Тест выбрасывания исключения при попытке изменить значения Y")
    void testSetYThrowsException() {
        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableArrayFunction.setY(0, 100.0));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableLinkedListFunction.setY(2, 200.0));
    }

    @Test
    @DisplayName("Тест поиска индекса по значению X для обёрнутых функций")
    void testIndexOfX() {
        assertEquals(0, unmodifiableArrayFunction.indexOfX(1.0));
        assertEquals(2, unmodifiableArrayFunction.indexOfX(3.0));
        assertEquals(-1, unmodifiableArrayFunction.indexOfX(6.0));

        assertEquals(0, unmodifiableLinkedListFunction.indexOfX(1.0));
        assertEquals(2, unmodifiableLinkedListFunction.indexOfX(3.0));
        assertEquals(-1, unmodifiableLinkedListFunction.indexOfX(6.0));
    }

    @Test
    @DisplayName("Тест поиска индекса по значению Y для обёрнутых функций")
    void testIndexOfY() {
        assertEquals(0, unmodifiableArrayFunction.indexOfY(10.0));
        assertEquals(2, unmodifiableArrayFunction.indexOfY(30.0));
        assertEquals(-1, unmodifiableArrayFunction.indexOfY(100.0));

        assertEquals(0, unmodifiableLinkedListFunction.indexOfY(10.0));
        assertEquals(2, unmodifiableLinkedListFunction.indexOfY(30.0));
        assertEquals(-1, unmodifiableLinkedListFunction.indexOfY(100.0));
    }

    @Test
    @DisplayName("Тест получения левой границы для обёрнутых функций")
    void testLeftBound() {
        assertEquals(0.0, unmodifiableArrayFunction.leftBound(), 1e-10);
        assertEquals(0.0, unmodifiableLinkedListFunction.leftBound(), 1e-10);
    }

    @Test
    @DisplayName("Тест получения правой границы для обёрнутых функций")
    void testRightBound() {
        assertEquals(0.0, unmodifiableArrayFunction.rightBound(), 1e-10);
        assertEquals(0.0, unmodifiableLinkedListFunction.rightBound(), 1e-10);
    }

    @Test
    @DisplayName("Тест применения функции (интерполяции) для обёрнутых функций")
    void testApply() {
        assertEquals(10.0, unmodifiableArrayFunction.apply(1.0), 1e-10);
        assertEquals(30.0, unmodifiableArrayFunction.apply(3.0), 1e-10);
        assertEquals(25.0, unmodifiableArrayFunction.apply(2.5), 1e-10); // интерполяция

        assertEquals(10.0, unmodifiableLinkedListFunction.apply(1.0), 1e-10);
        assertEquals(30.0, unmodifiableLinkedListFunction.apply(3.0), 1e-10);
        assertEquals(25.0, unmodifiableLinkedListFunction.apply(2.5), 1e-10); // интерполяция
    }

    @Test
    @DisplayName("Тест итератора для обёрнутых функций")
    void testIterator() {
        int count = 0;
        for (Point point : unmodifiableArrayFunction) {
            assertEquals(arrayFunction.getX(count), point.x, 1e-10);
            assertEquals(arrayFunction.getY(count), point.y, 1e-10);
            count++;
        }
        assertEquals(5, count);

        count = 0;
        for (Point point : unmodifiableLinkedListFunction) {
            assertEquals(linkedListFunction.getX(count), point.x, 1e-10);
            assertEquals(linkedListFunction.getY(count), point.y, 1e-10);
            count++;
        }
        assertEquals(5, count);
    }

    @Test
    @DisplayName("Тест неизменности оригинальной функции после оборачивания")
    void testOriginalFunctionNotModified() {
        double originalY = arrayFunction.getY(0);
        double originalY2 = linkedListFunction.getY(0);

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableArrayFunction.setY(0, 999.0));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableLinkedListFunction.setY(0, 999.0));

        assertEquals(originalY, arrayFunction.getY(0), 1e-10);
        assertEquals(originalY2, linkedListFunction.getY(0), 1e-10);
    }

    @Test
    @DisplayName("Тест множественного оборачивания функции")
    void testMultipleWrapping() {
        TabulatedFunction doubleWrapped = new UnmodifiableTabulatedFunction(
                new UnmodifiableTabulatedFunction(arrayFunction));

        assertEquals(5, doubleWrapped.getCount());
        assertEquals(10.0, doubleWrapped.getY(0), 1e-10);
        assertThrows(UnsupportedOperationException.class,
                () -> doubleWrapped.setY(0, 100.0));
    }

    @Test
    @DisplayName("Тест конструктора с null аргументом")
    void testConstructorWithNullArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new UnmodifiableTabulatedFunction(null),
                "Должно выбрасываться IllegalArgumentException при передаче null");
    }
    @Test
    @DisplayName("Тест метода remove() итератора без вызова next()")
    void testIteratorRemoveWithoutNext() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction unmodifiable = new UnmodifiableTabulatedFunction(
                new ArrayTabulatedFunction(xValues, yValues));

        Iterator<Point> iterator = unmodifiable.iterator();

        assertThrows(UnsupportedOperationException.class,
                () -> iterator.remove());
    }

}