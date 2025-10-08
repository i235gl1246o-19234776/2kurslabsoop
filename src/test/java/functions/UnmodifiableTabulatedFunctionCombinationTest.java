package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class UnmodifiableTabulatedFunctionCombinationTest {

    @Test
    @DisplayName("Тест оборачивания Strict в Unmodifiable: запрет модификации и интерполяции")
    void testStrictWrappedInUnmodifiable() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction strictFunction = new StrictTabulatedFunction(
                new ArrayTabulatedFunction(xValues, yValues));

        TabulatedFunction strictUnmodifiable = new UnmodifiableTabulatedFunction(strictFunction);

        assertEquals(3, strictUnmodifiable.getCount());
        assertEquals(1.0, strictUnmodifiable.getX(0), 1e-10);
        assertEquals(10.0, strictUnmodifiable.getY(0), 1e-10);

        assertThrows(UnsupportedOperationException.class,
                () -> strictUnmodifiable.setY(0, 100.0));

        assertThrows(UnsupportedOperationException.class,
                () -> strictUnmodifiable.apply(1.5));

        assertEquals(10.0, strictUnmodifiable.apply(1.0), 1e-10);
        assertEquals(20.0, strictUnmodifiable.apply(2.0), 1e-10);
        assertEquals(30.0, strictUnmodifiable.apply(3.0), 1e-10);
    }

    @Test
    @DisplayName("Тест оборачивания Unmodifiable в Strict: запрет модификации и интерполяции")
    void testUnmodifiableWrappedInStrict() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction unmodifiableFunction = new UnmodifiableTabulatedFunction(
                new ArrayTabulatedFunction(xValues, yValues));

        TabulatedFunction unmodifiableStrict = new StrictTabulatedFunction(unmodifiableFunction);

        assertEquals(3, unmodifiableStrict.getCount());
        assertEquals(1.0, unmodifiableStrict.getX(0), 1e-10);
        assertEquals(10.0, unmodifiableStrict.getY(0), 1e-10);

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableStrict.setY(0, 100.0));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableStrict.apply(1.5));

        assertEquals(10.0, unmodifiableStrict.apply(1.0), 1e-10);
        assertEquals(20.0, unmodifiableStrict.apply(2.0), 1e-10);
        assertEquals(30.0, unmodifiableStrict.apply(3.0), 1e-10);
    }

    @Test
    @DisplayName("Тест двойного оборачивания: Strict -> Unmodifiable -> Strict")
    void testStrictUnmodifiableStrictCombination() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction tripleWrapped = new StrictTabulatedFunction(
                new UnmodifiableTabulatedFunction(
                        new StrictTabulatedFunction(
                                new ArrayTabulatedFunction(xValues, yValues))));

        assertEquals(3, tripleWrapped.getCount());
        assertEquals(1.0, tripleWrapped.getX(0), 1e-10);
        assertEquals(10.0, tripleWrapped.getY(0), 1e-10);

        assertThrows(UnsupportedOperationException.class,
                () -> tripleWrapped.setY(0, 100.0));

        assertThrows(UnsupportedOperationException.class,
                () -> tripleWrapped.apply(1.5));

        assertEquals(10.0, tripleWrapped.apply(1.0), 1e-10);
        assertEquals(20.0, tripleWrapped.apply(2.0), 1e-10);
        assertEquals(30.0, tripleWrapped.apply(3.0), 1e-10);
    }

    @Test
    @DisplayName("Тест двойного оборачивания: Unmodifiable -> Strict -> Unmodifiable")
    void testUnmodifiableStrictUnmodifiableCombination() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction tripleWrapped = new UnmodifiableTabulatedFunction(
                new StrictTabulatedFunction(
                        new UnmodifiableTabulatedFunction(
                                new ArrayTabulatedFunction(xValues, yValues))));

        assertEquals(3, tripleWrapped.getCount());
        assertEquals(1.0, tripleWrapped.getX(0), 1e-10);
        assertEquals(10.0, tripleWrapped.getY(0), 1e-10);


        assertThrows(UnsupportedOperationException.class,
                () -> tripleWrapped.setY(0, 100.0));

        assertThrows(UnsupportedOperationException.class,
                () -> tripleWrapped.apply(1.5));

        assertEquals(10.0, tripleWrapped.apply(1.0), 1e-10);
        assertEquals(20.0, tripleWrapped.apply(2.0), 1e-10);
        assertEquals(30.0, tripleWrapped.apply(3.0), 1e-10);
    }

    @Test
    @DisplayName("Тест оборачивания Strict+Unmodifiable для LinkedListTabulatedFunction")
    void testStrictUnmodifiableWithLinkedList() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction strictUnmodifiable = new StrictTabulatedFunction(
                new UnmodifiableTabulatedFunction(
                        new LinkedListTabulatedFunction(xValues, yValues)));

        assertEquals(3, strictUnmodifiable.getCount());
        assertEquals(1.0, strictUnmodifiable.getX(0), 1e-10);
        assertEquals(10.0, strictUnmodifiable.getY(0), 1e-10);

        assertThrows(UnsupportedOperationException.class,
                () -> strictUnmodifiable.setY(0, 100.0));

        assertThrows(UnsupportedOperationException.class,
                () -> strictUnmodifiable.apply(1.5));

        assertEquals(10.0, strictUnmodifiable.apply(1.0), 1e-10);
        assertEquals(20.0, strictUnmodifiable.apply(2.0), 1e-10);
        assertEquals(30.0, strictUnmodifiable.apply(3.0), 1e-10);
    }

    @Test
    @DisplayName("Тест оборачивания Unmodifiable+Strict для LinkedListTabulatedFunction")
    void testUnmodifiableStrictWithLinkedList() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction unmodifiableStrict = new UnmodifiableTabulatedFunction(
                new StrictTabulatedFunction(
                        new LinkedListTabulatedFunction(xValues, yValues)));

        assertEquals(3, unmodifiableStrict.getCount());
        assertEquals(1.0, unmodifiableStrict.getX(0), 1e-10);
        assertEquals(10.0, unmodifiableStrict.getY(0), 1e-10);

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableStrict.setY(0, 100.0));

        assertThrows(UnsupportedOperationException.class,
                () -> unmodifiableStrict.apply(1.5));

        assertEquals(10.0, unmodifiableStrict.apply(1.0), 1e-10);
        assertEquals(20.0, unmodifiableStrict.apply(2.0), 1e-10);
        assertEquals(30.0, unmodifiableStrict.apply(3.0), 1e-10);
    }

    @Test
    @DisplayName("Тест итератора для комбинированных обёрток")
    void testIteratorWithCombinedWrappers() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction combined = new StrictTabulatedFunction(
                new UnmodifiableTabulatedFunction(
                        new ArrayTabulatedFunction(xValues, yValues)));

        int count = 0;
        for (Point point : combined) {
            assertEquals(xValues[count], point.x, 1e-10);
            assertEquals(yValues[count], point.y, 1e-10);
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Тест граничных значений для комбинированных обёрток")
    void testBoundariesWithCombinedWrappers() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction combined = new UnmodifiableTabulatedFunction(
                new StrictTabulatedFunction(
                        new ArrayTabulatedFunction(xValues, yValues)));

        assertEquals(0.0, combined.leftBound(), 1e-10);
        assertEquals(0.0, combined.rightBound(), 1e-10);

        assertEquals(0, combined.indexOfX(1.0));
        assertEquals(2, combined.indexOfX(3.0));
        assertEquals(0, combined.indexOfY(10.0));
        assertEquals(2, combined.indexOfY(30.0));
    }

    @Test
    @DisplayName("Тест неизменности оригинальной функции при комбинированном оборачивании")
    void testOriginalFunctionPreservationWithCombinedWrappers() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction original = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction combined = new StrictTabulatedFunction(
                new UnmodifiableTabulatedFunction(original));

        double originalY = original.getY(0);

        assertThrows(UnsupportedOperationException.class,
                () -> combined.setY(0, 999.0));

        assertEquals(originalY, original.getY(0), 1e-10);
    }
}