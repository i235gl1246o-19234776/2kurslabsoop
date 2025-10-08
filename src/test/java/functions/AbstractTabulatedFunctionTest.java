package functions;

import exception.ArrayIsNotSortedException;
import exception.DifferentLengthOfArraysException;
import exception.InterpolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Iterator;

public class AbstractTabulatedFunctionTest {

    private static final double DELTA = 1e-10;


    @Test
    @DisplayName("floorIndexOfX при x < leftBound должен возвращать 0 — ближайшая слева точка")
    public void testFloorIndexOfX_BelowLeftBound() {
        MockTabulatedFunction f = new MockTabulatedFunction(0.0, 2.0, 1.0, 3.0);
        assertEquals(0, f.floorIndexOfX(-1.0), "x = -1.0 < x0=0.0 → floorIndex = 0 GOOD");
        assertEquals(0, f.floorIndexOfX(0.0 - DELTA * 2), "x чуть ниже x0 → floorIndex = 0 GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX при x == leftBound должен возвращать 0 — первая точка точно совпадает ")
    public void testFloorIndexOfX_AtLeftBound() {
        MockTabulatedFunction f = new MockTabulatedFunction(0.0, 2.0, 1.0, 3.0);
        assertEquals(0, f.floorIndexOfX(0.0), "x = x0 = 0.0 → floorIndex = 0 GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX при x между x0 и x1 должен возвращать 0 — интервал [x0, x1)")
    public void testFloorIndexOfX_BetweenBounds() {
        MockTabulatedFunction f = new MockTabulatedFunction(0.0, 2.0, 1.0, 3.0);
        assertEquals(0, f.floorIndexOfX(0.5), "x = 0.5 ∈ [x0, x1) → floorIndex = 0 GOOD");
        assertEquals(0, f.floorIndexOfX(1.999), "x = 1.999 < x1 → floorIndex = 0 GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX при x == rightBound должен возвращать count-1 = 1 — последняя точка")
    public void testFloorIndexOfX_AtRightBound() {
        MockTabulatedFunction f = new MockTabulatedFunction(0.0, 2.0, 1.0, 3.0);
        assertEquals(1, f.floorIndexOfX(2.0), "x = x1 = 2.0 → floorIndex = 1 (count-1) GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX при x > rightBound должен возвращать count = 2 — за пределами диапазона")
    public void testFloorIndexOfX_AboveRightBound() {
        MockTabulatedFunction f = new MockTabulatedFunction(0.0, 2.0, 1.0, 3.0);
        assertEquals(2, f.floorIndexOfX(2.1), "x = 2.1 > x1 → floorIndex = count = 2 GOOD");
        assertEquals(2, f.floorIndexOfX(100.0), "x = 100.0 >> x1 → floorIndex = count = 2 GOOD");
    }

    @Test
    @DisplayName("interpolate должен корректно выполнять линейную интерполяцию в середине отрезка")
    public void testInterpolateWithExplicitBounds() {
        MockTabulatedFunction function = new MockTabulatedFunction(0.0, 2.0, 1.0, 3.0);

        // Интерполяция в середине: (x=1.0) → y = 1.0 + (3.0-1.0)/(2.0-0.0)*(1.0-0.0) = 2.0
        double result = function.interpolate(1.0, 0.0, 2.0, 1.0, 3.0);
        assertEquals(2.0, result, DELTA, "Интерполяция в середине (x=1.0): y=2.0 GOOD");

        // Ближе к левой границе: x=0.5 → y = 1.0 + 1.0*(0.5/2.0) = 1.5
        result = function.interpolate(0.5, 0.0, 2.0, 1.0, 3.0);
        assertEquals(1.5, result, DELTA, "Интерполяция ближе к левой границе (x=0.5): y=1.5 GOOD");

        // Ближе к правой границе: x=1.5 → y = 1.0 + 1.0*(1.5/2.0) = 2.5
        result = function.interpolate(1.5, 0.0, 2.0, 1.0, 3.0);
        assertEquals(2.5, result, DELTA, "Интерполяция ближе к правой границе (x=1.5): y=2.5 GOOD");

        // Тест с отрицательными значениями: x=0.0 между x0=-2.0 и x1=2.0, y0=-4.0, y1=4.0 → y=0.0
        MockTabulatedFunction negativeFunction = new MockTabulatedFunction(-2.0, 2.0, -4.0, 4.0);
        result = negativeFunction.interpolate(0.0, -2.0, 2.0, -4.0, 4.0);
        assertEquals(0.0, result, DELTA, "Интерполяция с отрицательными значениями: y=0.0 GOOD");
    }


    @Test
    @DisplayName("apply должен возвращать точное значение y при совпадении x с x0 или x1")
    public void testApplyWithExactMatch() {
        MockTabulatedFunction function = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(2.0, function.apply(1.0), DELTA, "apply(x0=1.0) → y0=2.0 GOOD");
        assertEquals(6.0, function.apply(3.0), DELTA, "apply(x1=3.0) → y1=6.0 GOOD");
    }

    @Test
    @DisplayName("apply должен выполнять линейную интерполяцию между x0 и x1 при x внутри диапазона")
    public void testApplyWithInterpolation() {
        MockTabulatedFunction function = new MockTabulatedFunction(0.0, 4.0, 0.0, 8.0);
        assertEquals(2.0, function.apply(1.0), DELTA, "apply(x=1.0): интерполяция → y=2.0 GOOD");
        assertEquals(4.0, function.apply(2.0), DELTA, "apply(x=2.0): интерполяция → y=4.0 GOOD");
        assertEquals(6.0, function.apply(3.0), DELTA, "apply(x=3.0): интерполяция → y=6.0 GOOD");
    }

    @Test
    @DisplayName("apply должен выполнять экстраполяцию влево по линии, проходящей через x0 и x1")
    public void testApplyWithExtrapolationLeft() {
        MockTabulatedFunction function = new MockTabulatedFunction(2.0, 4.0, 4.0, 8.0);
        // Уравнение: y = 2x (т.к. 4.0 = 2*2, 8.0 = 2*4)
        assertEquals(2.0, function.apply(1.0), DELTA, "extrapolateLeft(x=1.0): y=2*1=2.0 GOOD");
        assertEquals(0.0, function.apply(0.0), DELTA, "extrapolateLeft(x=0.0): y=2*0=0.0 GOOD");
        assertEquals(-4.0, function.apply(-2.0), DELTA, "extrapolateLeft(x=-2.0): y=2*(-2)=-4.0 GOOD");
    }

    @Test
    @DisplayName("apply должен выполнять экстраполяцию вправо по линии, проходящей через x0 и x1")
    public void testApplyWithExtrapolationRight() {
        MockTabulatedFunction function = new MockTabulatedFunction(2.0, 4.0, 4.0, 8.0);
        // Уравнение: y = 2x
        assertEquals(10.0, function.apply(5.0), DELTA, "extrapolateRight(x=5.0): y=2*5=10.0 GOOD");
        assertEquals(12.0, function.apply(6.0), DELTA, "extrapolateRight(x=6.0): y=2*6=12.0 GOOD");
        assertEquals(16.0, function.apply(8.0), DELTA, "extrapolateRight(x=8.0): y=2*8=16.0 GOOD");
    }

    @Test
    @DisplayName("apply должен корректно работать с константной функцией — возвращает y0 всегда")
    public void testApplyWithConstantFunction() {
        MockTabulatedFunction function = new MockTabulatedFunction(0.0, 10.0, 5.0, 5.0);
        assertEquals(5.0, function.apply(-5.0), DELTA, "const function: extrapolateLeft → y=5.0 GOOD");
        assertEquals(5.0, function.apply(2.5), DELTA, "const function: interpolation → y=5.0 GOOD");
        assertEquals(5.0, function.apply(15.0), DELTA, "const function: extrapolateRight → y=5.0 GOOD");
    }


    @Test
    @DisplayName("getCount должен возвращать 2 для двухточечной функции — всегда фиксировано")
    public void testGetCount() {
        MockTabulatedFunction f = new MockTabulatedFunction(0, 1, 0, 1);
        assertEquals(2, f.getCount(), "Две точки → getCount() = 2 GOOD");
    }


    @Test
    @DisplayName("getX(0) должен возвращать x0 — первую координату")
    public void testGetX_Index0() {
        MockTabulatedFunction f = new MockTabulatedFunction(10.0, 20.0, 100.0, 200.0);
        assertEquals(10.0, f.getX(0), DELTA, "getX(0) → x0 = 10.0 GOOD");
    }

    @Test
    @DisplayName("getX(1) должен возвращать x1 — вторую координату")
    public void testGetX_Index1() {
        MockTabulatedFunction f = new MockTabulatedFunction(10.0, 20.0, 100.0, 200.0);
        assertEquals(20.0, f.getX(1), DELTA, "getX(1) → x1 = 20.0 GOOD");
    }

    @Test
    @DisplayName("getX с индексом >= 2 должен выбрасывать IllegalArgumentException — индекс вне диапазона")
    public void testGetX_IndexOutOfBounds() {
        MockTabulatedFunction f = new MockTabulatedFunction(0, 1, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> f.getX(2));
    }

    @Test
    @DisplayName("getY(0) должен возвращать y0 — первую функциональную величину")
    public void testGetY_Index0() {
        MockTabulatedFunction f = new MockTabulatedFunction(10.0, 20.0, 100.0, 200.0);
        assertEquals(100.0, f.getY(0), DELTA, "getY(0) → y0 = 100.0 GOOD");
    }

    @Test
    @DisplayName("getY(1) должен возвращать y1 — вторую функциональную величину")
    public void testGetY_Index1() {
        MockTabulatedFunction f = new MockTabulatedFunction(10.0, 20.0, 100.0, 200.0);
        assertEquals(200.0, f.getY(1), DELTA, "getY(1) → y1 = 200.0 GOOD");
    }

    @Test
    @DisplayName("getY с индексом >= 2 должен выбрасывать IllegalArgumentException — индекс вне диапазона")
    public void testGetY_IndexOutOfBounds() {
        MockTabulatedFunction f = new MockTabulatedFunction(0, 1, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> f.getY(2));

    }


    @Test
    @DisplayName("setY должен выбрасывать UnsupportedOperationException — Mock-функция неизменяема")
    public void testSetY_ThrowsUnsupportedOperationException() {
        MockTabulatedFunction f = new MockTabulatedFunction(0, 1, 0, 1);
        assertThrows(UnsupportedOperationException.class, () -> f.setY(0, 5.0));

    }


    @Test
    @DisplayName("indexOfX должен находить индекс 0 при точном совпадении с x0")
    public void testIndexOfX_ExactMatch_x0() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(0, f.indexOfX(1.0), "indexOfX(x0=1.0) → index=0 GOOD");
    }

    @Test
    @DisplayName("indexOfX должен находить индекс 1 при точном совпадении с x1")
    public void testIndexOfX_ExactMatch_x1() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(1, f.indexOfX(3.0), "indexOfX(x1=3.0) → index=1 GOOD");
    }

    @Test
    @DisplayName("indexOfX должен находить x0 при значении в допуске 1e-10 от x0")
    public void testIndexOfX_CloseTo_x0() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(0, f.indexOfX(1.0 + DELTA * 0.9999), "x = x0 + 0.9999*δ → совпадение GOOD");
        assertEquals(0, f.indexOfX(1.0 - DELTA * 0.9999), "x = x0 - 0.9999*δ → совпадение GOOD");
    }

    @Test
    @DisplayName("indexOfX должен находить x1 при значении в допуске 1e-10 от x1")
    public void testIndexOfX_CloseTo_x1() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(1, f.indexOfX(3.0 + DELTA * 0.9999), "x = x1 + 0.9999*δ → совпадение GOOD");
        assertEquals(1, f.indexOfX(3.0 - DELTA * 0.9999), "x = x1 - 0.9999*δ → совпадение GOOD");
    }

    @Test
    @DisplayName("indexOfX должен возвращать -1, если x не совпадает ни с x0, ни с x1")
    public void testIndexOfX_NotFound() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(-1, f.indexOfX(2.0), "x=2.0 отсутствует → -1 GOOD");
        assertEquals(-1, f.indexOfX(0.0), "x=0.0 отсутствует → -1 GOOD");
        assertEquals(-1, f.indexOfX(4.0), "x=4.0 отсутствует → -1 GOOD");
    }


    @Test
    @DisplayName("indexOfY должен находить индекс 0 при точном совпадении с y0 ")
    public void testIndexOfY_ExactMatch_y0() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(0, f.indexOfY(2.0), "indexOfY(y0=2.0) → index=0 GOOD");
    }

    @Test
    @DisplayName("indexOfY должен находить индекс 1 при точном совпадении с y1")
    public void testIndexOfY_ExactMatch_y1() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(1, f.indexOfY(6.0), "indexOfY(y1=6.0) → index=1 GOOD");
    }

    @Test
    @DisplayName("indexOfY должен находить y0 при значении в допуске 1e-10 от y0")
    public void testIndexOfY_CloseTo_y0() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(0, f.indexOfY(2.0 + DELTA * 0.9999), "y = y0 + 0.9999*δ → совпадение GOOD");
        assertEquals(0, f.indexOfY(2.0 - DELTA * 0.9999), "y = y0 - 0.9999*δ → совпадение GOOD");
    }

    @Test
    @DisplayName("indexOfY должен находить y1 при значении в допуске 1e-10 от y1")
    public void testIndexOfY_CloseTo_y1() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(1, f.indexOfY(6.0 + DELTA * 0.9999), "y = y1 + 0.9999*δ → совпадение GOOD");
        assertEquals(1, f.indexOfY(6.0 - DELTA * 0.9999), "y = y1 - 0.9999*δ → совпадение GOOD");
    }

    @Test
    @DisplayName("indexOfY должен возвращать -1, если y не совпадает ни с y0, ни с y1")
    public void testIndexOfY_NotFound() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        assertEquals(-1, f.indexOfY(3.0), "y=3.0 отсутствует → -1 GOOD");
        assertEquals(-1, f.indexOfY(1.0), "y=1.0 отсутствует → -1 GOOD");
        assertEquals(-1, f.indexOfY(7.0), "y=7.0 отсутствует → -1 GOOD");
    }


    @Test
    @DisplayName("leftBound должен возвращать минимальное значение x (x0)")
    public void testLeftBound() {
        MockTabulatedFunction f = new MockTabulatedFunction(-5.0, 10.0, 0, 0);
        assertEquals(-5.0, f.leftBound(), DELTA, "leftBound = x0 = -5.0 GOOD");
    }

    @Test
    @DisplayName("rightBound должен возвращать максимальное значение x (x1)")
    public void testRightBound() {
        MockTabulatedFunction f = new MockTabulatedFunction(-5.0, 10.0, 0, 0);
        assertEquals(10.0, f.rightBound(), DELTA, "rightBound = x1 = 10.0 GOOD");
    }

    @Test
    @DisplayName("Конструктор должен выбрасывать IllegalArgumentException, если x0 > x1 — недопустимый диапазон")
    public void testConstructor_ThrowsOnInvalidRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new MockTabulatedFunction(2.0, 1.0, 0, 0));
    }


    @Test
    @DisplayName("checkLengthIsTheSame: массивы одинаковой длины — не должно выбрасывать исключение")
    void testCheckLengthIsTheSame_SameLength_Success() {
        // Должен пройти без ошибок
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 4.0, 9.0};
        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkLengthIsTheSame(x, y));
    }

    @Test
    @DisplayName("checkLengthIsTheSame: массивы разной длины — должно выбрасывать DifferentLengthOfArraysException")
    void testCheckLengthIsTheSame_DifferentLength_ThrowsException() {
        double[] x = {1.0, 2.0};
        double[] y = {1.0, 2.0, 3.0};

        DifferentLengthOfArraysException exception = assertThrows(
                DifferentLengthOfArraysException.class,
                () -> AbstractTabulatedFunction.checkLengthIsTheSame(x, y)
        );
    }


    @Test
    @DisplayName("checkSorted: строго возрастающий массив — не должно выбрасывать исключение")
    void testCheckSorted_StrictlyIncreasing_Success() {
        // Строго возрастающий массив — должен пройти
        double[] x = {1.0, 2.0, 3.0, 5.5, 10.0};
        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkSorted(x));
    }

    @Test
    @DisplayName("checkSorted: нарушение строгого возрастания (равные/убывающие элементы) — должно выбрасывать ArrayIsNotSortedException")
    void testCheckSorted_NotStrictlyIncreasing_ThrowsException() {
        //Нарушение строгого возрастания — должно бросить исключение

        //Элемент равен предыдущему
        double[] x1 = {1.0, 2.0, 2.0, 3.0};
        assertThrows(ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(x1));

        //Элемент меньше предыдущего
        double[] x2 = {1.0, 3.0, 2.0, 4.0};
        assertThrows(ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(x2));

        //Все элементы одинаковые
        double[] x3 = {5.0, 5.0, 5.0};
        assertThrows(ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(x3));
    }


    @Test
    @DisplayName("checkSorted: массив с отрицательными и положительными числами в порядке возрастания — не должно выбрасывать исключение")
    void testCheckSorted_NegativeAndPositive_Success() {
        double[] x = {-5.0, -2.0, 0.0, 1.5, 10.0};
        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkSorted(x));
    }


    @Test
    @DisplayName("interpolate (ArrayTabulatedFunction): точка вне интервала — должно выбрасывать InterpolationException")
    void testInterpolate_OutsideInterval_ArrayImpl() {
        double[] x = {1.0, 3.0, 5.0};
        double[] y = {1.0, 9.0, 25.0};
        ArrayTabulatedFunction af = new ArrayTabulatedFunction(x, y);

        assertThrows(InterpolationException.class,
                () -> af.interpolate(6.0, 0));

        assertThrows(InterpolationException.class,
                () -> af.interpolate(0.5, 0));

        assertThrows(InterpolationException.class,
                () -> af.interpolate(7.0, 1));
    }

    @Test
    @DisplayName("interpolate (LinkedListTabulatedFunction): точка вне интервала — должно выбрасывать InterpolationException")
    void testInterpolate_OutsideInterval_LinkedListImpl() {
        double[] x = {1.0, 3.0, 5.0};
        double[] y = {1.0, 9.0, 25.0};
        LinkedListTabulatedFunction llf = new LinkedListTabulatedFunction(x, y);

        assertThrows(InterpolationException.class,
                () -> llf.interpolate(6.0, 0));
        assertThrows(InterpolationException.class,
                () -> llf.interpolate(0.5, 0));
        assertThrows(InterpolationException.class,
                () -> llf.interpolate(7.0, 1));
    }


    @Test
    @DisplayName("interpolate: точка внутри интервала — корректный расчёт линейной интерполяции")
    void testInterpolate_InsideInterval_Success() {
        double[] x = {1.0, 3.0, 5.0};
        double[] y = {1.0, 9.0, 25.0};
        ArrayTabulatedFunction af = new ArrayTabulatedFunction(x, y);

        assertEquals(5.0, af.interpolate(2.0, 0), 1e-10); // (1->3): (2-1)/(3-1)=0.5 → 1 + 0.5*(9-1)=5.0

        assertEquals(17.0, af.interpolate(4.0, 1), 1e-10); // (3->5): (4-3)/2=0.5 → 9 + 0.5*16=17
    }
    @Test
    @DisplayName("Тест итератора, возвращающего null - проверка деталей исключения")
    void testIteratorReturnsNullDetails() {
        MockTabulatedFunction f = new MockTabulatedFunction(1.0, 3.0, 2.0, 6.0);
        TabulatedFunction unmodifiable = new UnmodifiableTabulatedFunction(f);

        // Более подходящее исключение для операций, которые не поддерживаются
        assertThrows(UnsupportedOperationException.class, () -> unmodifiable.iterator().remove());
    }

    @Test
    @DisplayName("toString для ArrayTabulatedFunction с несколькими точками")
    void testArrayTabulatedFunctionToString() {
        double[] xValues = {0.0, 0.5, 1.0};
        double[] yValues = {0.0, 0.25, 1.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "ArrayTabulatedFunction size = 3\n" +
                "[0.0; 0.0]\n" +
                "[0.5; 0.25]\n" +
                "[1.0; 1.0]\n";

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("toString для LinkedListTabulatedFunction с несколькими точками")
    void testLinkedListTabulatedFunctionToString() {
        double[] xValues = {0.0, 0.5, 1.0};
        double[] yValues = {0.0, 0.25, 1.0};

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "LinkedListTabulatedFunction size = 3\n" +
                "[0.0; 0.0]\n" +
                "[0.5; 0.25]\n" +
                "[1.0; 1.0]\n";

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("toString для функции с отрицательными значениями")
    void testToStringWithNegativeValues() {
        double[] xValues = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues = {4.0, 1.0, 0.0, 1.0, 4.0};

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "LinkedListTabulatedFunction size = 5\n" +
                "[-2.0; 4.0]\n" +
                "[-1.0; 1.0]\n" +
                "[0.0; 0.0]\n" +
                "[1.0; 1.0]\n" +
                "[2.0; 4.0]\n";

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("toString для функции с большими числами")
    void testToStringWithLargeNumbers() {
        double[] xValues = {1000.0, 2000.0, 3000.0};
        double[] yValues = {1000000.0, 4000000.0, 9000000.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "ArrayTabulatedFunction size = 3\n" +
                "[1000.0; 1000000.0]\n" +
                "[2000.0; 4000000.0]\n" +
                "[3000.0; 9000000.0]\n";

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("toString для функции с десятичными значениями")
    void testToStringWithDecimalValues() {
        double[] xValues = {0.1, 0.2, 0.3, 0.4, 0.5};
        double[] yValues = {0.01, 0.04, 0.09, 0.16, 0.25};

        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);
        String result = function.toString();

        String expected = "LinkedListTabulatedFunction size = 5\n" +
                "[0.1; 0.01]\n" +
                "[0.2; 0.04]\n" +
                "[0.3; 0.09]\n" +
                "[0.4; 0.16]\n" +
                "[0.5; 0.25]\n";

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("toString после модификации функции")
    void testToStringAfterModification() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Получаем строковое представление до модификации
        String before = function.toString();

        // Модифицируем функцию
        function.setY(1, 25.0);

        // Получаем строковое представление после модификации
        String after = function.toString();

        String expectedBefore = "ArrayTabulatedFunction size = 3\n" +
                "[1.0; 10.0]\n" +
                "[2.0; 20.0]\n" +
                "[3.0; 30.0]\n";

        String expectedAfter = "ArrayTabulatedFunction size = 3\n" +
                "[1.0; 10.0]\n" +
                "[2.0; 25.0]\n" +
                "[3.0; 30.0]\n";

        assertEquals(expectedBefore, before);
        assertEquals(expectedAfter, after);
    }
}