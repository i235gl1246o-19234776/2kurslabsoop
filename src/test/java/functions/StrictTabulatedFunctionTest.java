package functions;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class StrictTabulatedFunctionTest {

    private ArrayTabulatedFunction arrayFunction;
    private LinkedListTabulatedFunction linkedListFunction;
    private StrictTabulatedFunction strictArrayFunction;
    private StrictTabulatedFunction strictLinkedListFunction;

    @BeforeEach
    void setUp() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};

        arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);

        strictArrayFunction = new StrictTabulatedFunction(arrayFunction);
        strictLinkedListFunction = new StrictTabulatedFunction(linkedListFunction);
    }

    @Test
    @DisplayName("Конструктор должен корректно создавать обёртку для ArrayTabulatedFunction")
    void testConstructorWithArrayFunction() {
        assertNotNull(strictArrayFunction);
        assertEquals(4, strictArrayFunction.getCount());
    }

    @Test
    @DisplayName("Конструктор должен корректно создавать обёртку для LinkedListTabulatedFunction")
    void testConstructorWithLinkedListFunction() {
        assertNotNull(strictLinkedListFunction);
        assertEquals(4, strictLinkedListFunction.getCount());
    }

    @Test
    @DisplayName("Метод apply должен возвращать правильное значение для существующего X (Array)")
    void testApplyForExistingX_Array() {
        assertEquals(10.0, strictArrayFunction.apply(1.0), 0.0001);
        assertEquals(20.0, strictArrayFunction.apply(2.0), 0.0001);
        assertEquals(30.0, strictArrayFunction.apply(3.0), 0.0001);
        assertEquals(40.0, strictArrayFunction.apply(4.0), 0.0001);
    }

    @Test
    @DisplayName("Метод apply должен возвращать правильное значение для существующего X (LinkedList)")
    void testApplyForExistingX_LinkedList() {
        assertEquals(10.0, strictLinkedListFunction.apply(1.0), 0.0001);
        assertEquals(20.0, strictLinkedListFunction.apply(2.0), 0.0001);
        assertEquals(30.0, strictLinkedListFunction.apply(3.0), 0.0001);
        assertEquals(40.0, strictLinkedListFunction.apply(4.0), 0.0001);
    }

    @Test
    @DisplayName("Метод apply должен бросать исключение для несуществующего X (Array)")
    void testApplyThrowsExceptionForNonExistingX_Array() {
        assertThrows(UnsupportedOperationException.class, () -> strictArrayFunction.apply(0.5));
        assertThrows(UnsupportedOperationException.class, () -> strictArrayFunction.apply(2.5));
        assertThrows(UnsupportedOperationException.class, () -> strictArrayFunction.apply(4.5));
    }

    @Test
    @DisplayName("Метод apply должен бросать исключение для несуществующего X (LinkedList)")
    void testApplyThrowsExceptionForNonExistingX_LinkedList() {
        assertThrows(UnsupportedOperationException.class, () -> strictLinkedListFunction.apply(0.5));
        assertThrows(UnsupportedOperationException.class, () -> strictLinkedListFunction.apply(2.5));
        assertThrows(UnsupportedOperationException.class, () -> strictLinkedListFunction.apply(4.5));
    }

    @Test
    @DisplayName("Сообщение исключения должно содержать значение X и информацию о запрете интерполяции")
    void testExceptionMessageContainsXValue() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> strictArrayFunction.apply(2.7)
        );
    }

    @Test
    @DisplayName("Метод getCount должен делегировать вызов базовой функции (Array)")
    void testGetCount_Array() {
        assertEquals(4, strictArrayFunction.getCount());
    }

    @Test
    @DisplayName("Метод getCount должен делегировать вызов базовой функции (LinkedList)")
    void testGetCount_LinkedList() {
        assertEquals(4, strictLinkedListFunction.getCount());
    }

    @Test
    @DisplayName("Метод getX должен делегировать вызов базовой функции (Array)")
    void testGetX_Array() {
        assertEquals(1.0, strictArrayFunction.getX(0), 0.0001);
        assertEquals(2.0, strictArrayFunction.getX(1), 0.0001);
        assertEquals(3.0, strictArrayFunction.getX(2), 0.0001);
        assertEquals(4.0, strictArrayFunction.getX(3), 0.0001);
    }

    @Test
    @DisplayName("Метод getX должен делегировать вызов базовой функции (LinkedList)")
    void testGetX_LinkedList() {
        assertEquals(1.0, strictLinkedListFunction.getX(0), 0.0001);
        assertEquals(2.0, strictLinkedListFunction.getX(1), 0.0001);
        assertEquals(3.0, strictLinkedListFunction.getX(2), 0.0001);
        assertEquals(4.0, strictLinkedListFunction.getX(3), 0.0001);
    }

    @Test
    @DisplayName("Метод getY должен делегировать вызов базовой функции (Array)")
    void testGetY_Array() {
        assertEquals(10.0, strictArrayFunction.getY(0), 0.0001);
        assertEquals(20.0, strictArrayFunction.getY(1), 0.0001);
        assertEquals(30.0, strictArrayFunction.getY(2), 0.0001);
        assertEquals(40.0, strictArrayFunction.getY(3), 0.0001);
    }

    @Test
    @DisplayName("Метод getY должен делегировать вызов базовой функции (LinkedList)")
    void testGetY_LinkedList() {
        assertEquals(10.0, strictLinkedListFunction.getY(0), 0.0001);
        assertEquals(20.0, strictLinkedListFunction.getY(1), 0.0001);
        assertEquals(30.0, strictLinkedListFunction.getY(2), 0.0001);
        assertEquals(40.0, strictLinkedListFunction.getY(3), 0.0001);
    }

    @Test
    @DisplayName("Метод setY должен делегировать вызов и изменять значение в базовой функции (Array)")
    void testSetY_Array() {
        strictArrayFunction.setY(1, 25.0);
        assertEquals(25.0, strictArrayFunction.getY(1), 0.0001);
        assertEquals(25.0, arrayFunction.getY(1), 0.0001);
    }

    @Test
    @DisplayName("Метод setY должен делегировать вызов и изменять значение в базовой функции (LinkedList)")
    void testSetY_LinkedList() {
        strictLinkedListFunction.setY(1, 25.0);
        assertEquals(25.0, strictLinkedListFunction.getY(1), 0.0001);
        assertEquals(25.0, linkedListFunction.getY(1), 0.0001);
    }

    @Test
    @DisplayName("Метод indexOfX должен делегировать вызов базовой функции (Array)")
    void testIndexOfX_Array() {
        assertEquals(0, strictArrayFunction.indexOfX(1.0));
        assertEquals(1, strictArrayFunction.indexOfX(2.0));
        assertEquals(2, strictArrayFunction.indexOfX(3.0));
        assertEquals(3, strictArrayFunction.indexOfX(4.0));
        assertEquals(-1, strictArrayFunction.indexOfX(5.0));
    }

    @Test
    @DisplayName("Метод indexOfX должен делегировать вызов базовой функции (LinkedList)")
    void testIndexOfX_LinkedList() {
        assertEquals(0, strictLinkedListFunction.indexOfX(1.0));
        assertEquals(1, strictLinkedListFunction.indexOfX(2.0));
        assertEquals(2, strictLinkedListFunction.indexOfX(3.0));
        assertEquals(3, strictLinkedListFunction.indexOfX(4.0));
        assertEquals(-1, strictLinkedListFunction.indexOfX(5.0));
    }

    @Test
    @DisplayName("Метод indexOfY должен делегировать вызов базовой функции (Array)")
    void testIndexOfY_Array() {
        assertEquals(0, strictArrayFunction.indexOfY(10.0));
        assertEquals(1, strictArrayFunction.indexOfY(20.0));
        assertEquals(2, strictArrayFunction.indexOfY(30.0));
        assertEquals(3, strictArrayFunction.indexOfY(40.0));
        assertEquals(-1, strictArrayFunction.indexOfY(50.0));
    }

    @Test
    @DisplayName("Метод indexOfY должен делегировать вызов базовой функции (LinkedList)")
    void testIndexOfY_LinkedList() {
        assertEquals(0, strictLinkedListFunction.indexOfY(10.0));
        assertEquals(1, strictLinkedListFunction.indexOfY(20.0));
        assertEquals(2, strictLinkedListFunction.indexOfY(30.0));
        assertEquals(3, strictLinkedListFunction.indexOfY(40.0));
        assertEquals(-1, strictLinkedListFunction.indexOfY(50.0));
    }

    @Test
    @DisplayName("Метод leftBound должен делегировать вызов базовой функции (Array)")
    void testLeftBound_Array() {
        assertEquals(1.0, strictArrayFunction.leftBound(), 0.0001);
    }

    @Test
    @DisplayName("Метод leftBound должен делегировать вызов базовой функции (LinkedList)")
    void testLeftBound_LinkedList() {
        assertEquals(1.0, strictLinkedListFunction.leftBound(), 0.0001);
    }

    @Test
    @DisplayName("Метод rightBound должен делегировать вызов базовой функции (Array)")
    void testRightBound_Array() {
        assertEquals(4.0, strictArrayFunction.rightBound(), 0.0001);
    }

    @Test
    @DisplayName("Метод rightBound должен делегировать вызов базовой функции (LinkedList)")
    void testRightBound_LinkedList() {
        assertEquals(4.0, strictLinkedListFunction.rightBound(), 0.0001);
    }

    @Test
    @DisplayName("Итератор должен корректно работать с точками функции (Array)")
    void testIterator_Array() {
        int count = 0;
        for (Point point : strictArrayFunction) {
            assertNotNull(point);
            assertEquals(arrayFunction.getX(count), point.x, 0.0001);
            assertEquals(arrayFunction.getY(count), point.y, 0.0001);
            count++;
        }
        assertEquals(4, count);
    }

    @Test
    @DisplayName("Итератор должен корректно работать с точками функции (LinkedList)")
    void testIterator_LinkedList() {
        int count = 0;
        for (Point point : strictLinkedListFunction) {
            assertNotNull(point);
            assertEquals(linkedListFunction.getX(count), point.x, 0.0001);
            assertEquals(linkedListFunction.getY(count), point.y, 0.0001);
            count++;
        }
        assertEquals(4, count);
    }

    @Test
    @DisplayName("Проверка граничных значений для apply")
    void testBoundaryValuesForApply() {
        // Граничные значения должны работать
        assertEquals(10.0, strictArrayFunction.apply(1.0), 0.0001);
        assertEquals(40.0, strictArrayFunction.apply(4.0), 0.0001);

        // Значения за границами должны бросать исключение
        assertThrows(UnsupportedOperationException.class, () -> strictArrayFunction.apply(0.999));
        assertThrows(UnsupportedOperationException.class, () -> strictArrayFunction.apply(4.001));
    }

    @Test
    @DisplayName("Проверка неизменяемости базовой функции после оборачивания")
    void testBaseFunctionImmutability() {
        // Изменения через StrictTabulatedFunction должны отражаться в базовой функции
        strictArrayFunction.setY(0, 15.0);
        assertEquals(15.0, arrayFunction.getY(0), 0.0001);

        // Изменения через базовую функцию должны отражаться в StrictTabulatedFunction
        arrayFunction.setY(1, 25.0);
        assertEquals(25.0, strictArrayFunction.getY(1), 0.0001);
    }
}