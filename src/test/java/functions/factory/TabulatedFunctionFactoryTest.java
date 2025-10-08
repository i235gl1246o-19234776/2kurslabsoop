package functions.factory;

import functions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedFunctionFactoryTest {

    @Test
    @DisplayName("ArrayTabulatedFunctionFactory должен создавать объекты ArrayTabulatedFunction")
    void testArrayTabulatedFunctionFactory() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof ArrayTabulatedFunction);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
    }

    @Test
    @DisplayName("LinkedListTabulatedFunctionFactory должен создавать объекты LinkedListTabulatedFunction")
    void testLinkedListTabulatedFunctionFactory() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof LinkedListTabulatedFunction);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
    }

    @Test
    @DisplayName("ArrayTabulatedFunctionFactory должен создавать функцию с большим количеством элементов")
    void testArrayFactoryWithManyElements() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0, 100.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof ArrayTabulatedFunction);
        assertEquals(10, function.getCount());
        assertEquals(10.0, function.getX(9), 0.0001);
        assertEquals(100.0, function.getY(9), 0.0001);
    }

    @Test
    @DisplayName("LinkedListTabulatedFunctionFactory должен создавать функцию с большим количеством элементов")
    void testLinkedListFactoryWithManyElements() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0, 100.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof LinkedListTabulatedFunction);
        assertEquals(10, function.getCount());
        assertEquals(10.0, function.getX(9), 0.0001);
        assertEquals(100.0, function.getY(9), 0.0001);
    }

    @Test
    @DisplayName("Созданные функции должны быть независимыми от исходных массивов")
    void testFunctionIndependenceFromArrays() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        xValues[0] = 100.0;
        yValues[0] = 200.0;

        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
    }


    @Test
    @DisplayName("ArrayTabulatedFunctionFactory.createStrict должен создавать StrictTabulatedFunction")
    void testArrayFactoryCreateStrict() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createStrict(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof StrictTabulatedFunction);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
    }

    @Test
    @DisplayName("LinkedListTabulatedFunctionFactory.createStrict должен создавать StrictTabulatedFunction")
    void testLinkedListFactoryCreateStrict() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createStrict(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof StrictTabulatedFunction);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
    }

    @Test
    @DisplayName("createStrict должен запрещать интерполяцию для ArrayFactory")
    void testArrayFactoryCreateStrictNoInterpolation() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createStrict(xValues, yValues);

        assertEquals(10.0, function.apply(1.0), 0.0001);
        assertEquals(20.0, function.apply(2.0), 0.0001);

        assertThrows(UnsupportedOperationException.class, () -> function.apply(1.5));
        assertThrows(UnsupportedOperationException.class, () -> function.apply(0.5));
        assertThrows(UnsupportedOperationException.class, () -> function.apply(3.5));
    }

    @Test
    @DisplayName("createStrict должен запрещать интерполяцию для LinkedListFactory")
    void testLinkedListFactoryCreateStrictNoInterpolation() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createStrict(xValues, yValues);

        assertEquals(10.0, function.apply(1.0), 0.0001);
        assertEquals(20.0, function.apply(2.0), 0.0001);

        assertThrows(UnsupportedOperationException.class, () -> function.apply(1.5));
        assertThrows(UnsupportedOperationException.class, () -> function.apply(0.5));
        assertThrows(UnsupportedOperationException.class, () -> function.apply(3.5));
    }

    @Test
    @DisplayName("createStrict должен сохранять независимость от исходных массивов")
    void testCreateStrictIndependenceFromArrays() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createStrict(xValues, yValues);

        xValues[0] = 100.0;
        yValues[0] = 200.0;

        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
    }

    @Test
    @DisplayName("createStrict с большими массивами для ArrayFactory")
    void testArrayFactoryCreateStrictWithManyElements() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0, 100.0};

        TabulatedFunction function = factory.createStrict(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof StrictTabulatedFunction);
        assertEquals(10, function.getCount());
        assertEquals(10.0, function.getX(9), 0.0001);
        assertEquals(100.0, function.getY(9), 0.0001);
    }

    @Test
    @DisplayName("createStrict с большими массивами для LinkedListFactory")
    void testLinkedListFactoryCreateStrictWithManyElements() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0, 100.0};

        TabulatedFunction function = factory.createStrict(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof StrictTabulatedFunction);
        assertEquals(10, function.getCount());
        assertEquals(10.0, function.getX(9), 0.0001);
        assertEquals(100.0, function.getY(9), 0.0001);
    }

    @Test
    @DisplayName("createStrict должен разрешать модификацию Y значений")
    void testCreateStrictAllowsSetY() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createStrict(xValues, yValues);

        function.setY(1, 25.0);
        assertEquals(25.0, function.getY(1), 0.0001);
        assertEquals(25.0, function.apply(2.0), 0.0001);
    }

    @Test
    @DisplayName("ArrayTabulatedFunctionFactory.createStrictUnmodifiable создает неизменяемую строгую функцию")
    void testArrayFactoryCreateStrictUnmodifiable() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        TabulatedFunction function = factory.createStrictUnmodifiable(xValues, yValues);

        assertNotNull(function);
        assertEquals(4, function.getCount());

        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(4.0, function.getX(3), 1e-10);
        assertEquals(40.0, function.getY(3), 1e-10);
    }

    @Test
    @DisplayName("LinkedListTabulatedFunctionFactory.createStrictUnmodifiable создает неизменяемую строгую функцию")
    void testLinkedListFactoryCreateStrictUnmodifiable() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();

        TabulatedFunction function = factory.createStrictUnmodifiable(xValues, yValues);

        assertNotNull(function);
        assertEquals(3, function.getCount());

        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
    }

    @Test
    @DisplayName("createStrictUnmodifiable должен запрещать интерполяцию")
    void testCreateStrictUnmodifiableNoInterpolation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction function = factory.createStrictUnmodifiable(xValues, yValues);

        assertEquals(10.0, function.apply(1.0), 1e-10);
        assertEquals(20.0, function.apply(2.0), 1e-10);

        assertThrows(UnsupportedOperationException.class, () -> function.apply(1.5));
        assertThrows(UnsupportedOperationException.class, () -> function.apply(0.5));
        assertThrows(UnsupportedOperationException.class, () -> function.apply(4.5));
    }

    @Test
    @DisplayName("createStrictUnmodifiable должен запрещать модификацию Y значений")
    void testCreateStrictUnmodifiableNoModification() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction function = factory.createStrictUnmodifiable(xValues, yValues);

        assertThrows(UnsupportedOperationException.class, () -> {
            function.setY(1, 25.0);
        });

        assertEquals(20.0, function.getY(1), 1e-10);
    }

    @Test
    @DisplayName("createStrictUnmodifiable должен быть полностью неизменяемым")
    void testCreateStrictUnmodifiableCompleteImmutability() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction function = factory.createStrictUnmodifiable(xValues, yValues);

        assertThrows(UnsupportedOperationException.class, () -> function.setY(0, 100.0));

        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(20.0, function.getY(1), 1e-10);
    }

    @Test
    @DisplayName("createStrictUnmodifiable должен сохранять независимость от исходных массивов")
    void testCreateStrictUnmodifiableIndependence() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createStrictUnmodifiable(xValues, yValues);

        xValues[0] = 100.0;
        yValues[0] = 200.0;

        // Функция должна остаться неизменной
        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
    }

    @Test
    @DisplayName("createStrictUnmodifiable итератор должен быть неизменяемым")
    void testCreateStrictUnmodifiableIterator() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction function = factory.createStrictUnmodifiable(xValues, yValues);

        int count = 0;
        for (var point : function) {
            assertNotNull(point);
            count++;
        }
        assertEquals(3, count);

        var iterator = function.iterator();
        assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
    }

    @Test
    @DisplayName("createStrictUnmodifiable должен обладать свойствами обеих оберток")
    void testCreateStrictUnmodifiableCombinedProperties() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction function = factory.createStrictUnmodifiable(xValues, yValues);

        assertThrows(UnsupportedOperationException.class, () -> function.apply(1.5));

        assertThrows(UnsupportedOperationException.class, () -> function.setY(0, 100.0));

        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(10.0, function.getY(0), 1e-10);
        assertEquals(10.0, function.apply(1.0), 1e-10);
    }

    @Test
    @DisplayName("Сравнение create, createStrict и createStrictUnmodifiable")
    void testCompareAllCreateMethods() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        TabulatedFunction normal = factory.create(xValues, yValues);
        TabulatedFunction strict = factory.createStrict(xValues, yValues);
        TabulatedFunction strictUnmodifiable = factory.createStrictUnmodifiable(xValues, yValues);

        for (int i = 0; i < xValues.length; i++) {
            assertEquals(normal.getX(i), strict.getX(i), 1e-10);
            assertEquals(normal.getX(i), strictUnmodifiable.getX(i), 1e-10);
            assertEquals(normal.getY(i), strict.getY(i), 1e-10);
            assertEquals(normal.getY(i), strictUnmodifiable.getY(i), 1e-10);
        }

        assertEquals(15.0, normal.apply(1.5), 1e-10); // Интерполяция работает
        assertThrows(UnsupportedOperationException.class, () -> strict.apply(1.5)); // Только strict
        assertThrows(UnsupportedOperationException.class, () -> strictUnmodifiable.apply(1.5)); // Strict + Unmodifiable

        normal.setY(0, 100.0);
        strict.setY(0, 100.0);
        assertThrows(UnsupportedOperationException.class, () -> strictUnmodifiable.setY(0, 100.0)); // Запрещено
    }

    @Test
    @DisplayName("ArrayTabulatedFunctionFactory.createUnmodifiable должен создавать UnmodifiableTabulatedFunction")
    void testArrayFactoryCreateUnmodifiable() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createUnmodifiable(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof UnmodifiableTabulatedFunction);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
    }

    @Test
    @DisplayName("LinkedListTabulatedFunctionFactory.createUnmodifiable должен создавать UnmodifiableTabulatedFunction")
    void testLinkedListFactoryCreateUnmodifiable() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createUnmodifiable(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof UnmodifiableTabulatedFunction);
        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
    }

    @Test
    @DisplayName("createUnmodifiable должен разрешать интерполяцию")
    void testCreateUnmodifiableAllowsInterpolation() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createUnmodifiable(xValues, yValues);

        assertEquals(15.0, function.apply(1.5), 0.0001);
        assertEquals(5.0, function.apply(0.5), 0.0001); // экстраполяция слева
        assertEquals(35.0, function.apply(3.5), 0.0001); // экстраполяция справа
    }

    @Test
    @DisplayName("createUnmodifiable должен запрещать модификацию Y значений")
    void testCreateUnmodifiableNoModification() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createUnmodifiable(xValues, yValues);

        assertThrows(UnsupportedOperationException.class, () -> {
            function.setY(1, 25.0);
        });

        assertEquals(20.0, function.getY(1), 0.0001);
    }

    @Test
    @DisplayName("createUnmodifiable должен быть полностью неизменяемым")
    void testCreateUnmodifiableCompleteImmutability() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createUnmodifiable(xValues, yValues);

        assertThrows(UnsupportedOperationException.class, () -> function.setY(0, 100.0));

        assertEquals(10.0, function.getY(0), 0.0001);
        assertEquals(20.0, function.getY(1), 0.0001);
        assertEquals(30.0, function.getY(2), 0.0001);
    }

    @Test
    @DisplayName("createUnmodifiable должен сохранять независимость от исходных массивов")
    void testCreateUnmodifiableIndependence() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createUnmodifiable(xValues, yValues);

        xValues[0] = 100.0;
        yValues[0] = 200.0;

        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
    }

    @Test
    @DisplayName("createUnmodifiable итератор должен быть неизменяемым")
    void testCreateUnmodifiableIterator() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createUnmodifiable(xValues, yValues);

        int count = 0;
        for (var point : function) {
            assertNotNull(point);
            count++;
        }
        assertEquals(3, count);

        var iterator = function.iterator();
        assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
    }

    @Test
    @DisplayName("createUnmodifiable с пустыми массивами должен бросать исключение")
    void testCreateUnmodifiableEmptyArrays() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] emptyX = {};
        double[] emptyY = {};

        assertThrows(IllegalArgumentException.class, () -> {
            factory.createUnmodifiable(emptyX, emptyY);
        });
    }

    @Test
    @DisplayName("createUnmodifiable с null массивами должен бросать исключение")
    void testCreateUnmodifiableNullArrays() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        assertThrows(IllegalArgumentException.class, () -> {
            factory.createUnmodifiable(null, new double[]{1.0});
        });

        assertThrows(IllegalArgumentException.class, () -> {
            factory.createUnmodifiable(new double[]{1.0}, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            factory.createUnmodifiable(null, null);
        });
    }

    @Test
    @DisplayName("createUnmodifiable с разными длинами массивов должен бросать исключение")
    void testCreateUnmodifiableDifferentLengthArrays() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0, 30.0}; // Разная длина

        assertThrows(IllegalArgumentException.class, () -> {
            factory.createUnmodifiable(xValues, yValues);
        });
    }

    @Test
    @DisplayName("createUnmodifiable с большими массивами")
    void testCreateUnmodifiableWithLargeArrays() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        int size = 1000;
        double[] xValues = new double[size];
        double[] yValues = new double[size];

        for (int i = 0; i < size; i++) {
            xValues[i] = i;
            yValues[i] = i * i;
        }

        TabulatedFunction function = factory.createUnmodifiable(xValues, yValues);

        assertNotNull(function);
        assertEquals(size, function.getCount());
        assertEquals(0.0, function.getX(0), 0.0001);
        assertEquals(0.0, function.getY(0), 0.0001);
        assertEquals(999.0, function.getX(999), 0.0001);
        assertEquals(998001.0, function.getY(999), 0.0001); // 999^2
    }

    @Test
    @DisplayName("Сравнение create и createUnmodifiable")
    void testCompareCreateAndCreateUnmodifiable() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction normal = factory.create(xValues, yValues);
        TabulatedFunction unmodifiable = factory.createUnmodifiable(xValues, yValues);

        for (int i = 0; i < xValues.length; i++) {
            assertEquals(normal.getX(i), unmodifiable.getX(i), 0.0001);
            assertEquals(normal.getY(i), unmodifiable.getY(i), 0.0001);
        }

        assertEquals(15.0, normal.apply(1.5), 0.0001);
        assertEquals(15.0, unmodifiable.apply(1.5), 0.0001);

        normal.setY(0, 100.0); // Модификация работает
        assertThrows(UnsupportedOperationException.class, () -> {
            unmodifiable.setY(0, 100.0); // Модификация запрещена
        });

        assertEquals(10.0, unmodifiable.getY(0), 0.0001);
    }

    @Test
    @DisplayName("createUnmodifiable должен оборачивать StrictTabulatedFunction корректно")
    void testCreateUnmodifiableWrapsStrictCorrectly() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction strictFunction = factory.createStrict(xValues, yValues);
        TabulatedFunction unmodifiableFunction = factory.createUnmodifiable(xValues, yValues);

        assertEquals(15.0, unmodifiableFunction.apply(1.5), 0.0001); // Интерполяция работает

        assertThrows(UnsupportedOperationException.class, () -> {
            strictFunction.apply(1.5);
        });
    }

    @Test
    @DisplayName("createUnmodifiable должен корректно работать с LinkedListFactory")
    void testLinkedListCreateUnmodifiableProperties() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.createUnmodifiable(xValues, yValues);

        assertNotNull(function);
        assertTrue(function instanceof UnmodifiableTabulatedFunction);
        assertEquals(3, function.getCount());

        assertEquals(15.0, function.apply(1.5), 0.0001);

        assertThrows(UnsupportedOperationException.class, () -> {
            function.setY(1, 25.0);
        });

        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
    }

    private final TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

    @Test
    @DisplayName("createStrict: корректные массивы → создаётся StrictTabulatedFunction")
    public void testCreateStrictWithValidArrays() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {0.0, 1.0, 4.0};

        TabulatedFunction result = factory.createStrict(x, y);

        // Проверяем, что результат — StrictTabulatedFunction
        assertNotNull(result);
        assertTrue(result instanceof StrictTabulatedFunction);

        // Проверяем содержимое (через делегирование)
        assertEquals(3, result.getCount());
        assertEquals(0.0, result.getX(0), 1e-10);
        assertEquals(1.0, result.getY(1), 1e-10);
        assertEquals(2.0, result.getX(2), 1e-10);
    }

    @Test
    @DisplayName("createStrict: null в xValues → IllegalArgumentException")
    public void testCreateStrictWithNullX() {
        double[] y = {1.0, 2.0};
        assertThrows(IllegalArgumentException.class, () -> {
            factory.createStrict(null, y);
        });
    }

    @Test
    @DisplayName("createStrict: null в yValues → IllegalArgumentException")
    public void testCreateStrictWithNullY() {
        double[] x = {1.0, 2.0};
        assertThrows(IllegalArgumentException.class, () -> {
            factory.createStrict(x, null);
        });
    }

    @Test
    @DisplayName("createStrict: разная длина массивов → IllegalArgumentException")
    public void testCreateStrictWithDifferentLengths() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 2.0}; // короче

        assertThrows(IllegalArgumentException.class, () -> {
            factory.createStrict(x, y);
        });
    }


    @Test
    @DisplayName("createStrict: возвращает именно StrictTabulatedFunction, а не базовую реализацию")
    public void testCreateStrictReturnsStrictWrapper() {
        double[] x = {0.0, 1.0};
        double[] y = {0.0, 1.0};

        TabulatedFunction strictFunc = factory.createStrict(x, y);
        TabulatedFunction baseFunc = factory.create(x, y);

        // Убеждаемся, что createStrict ≠ create
        assertNotSame(baseFunc, strictFunc);
        assertNotEquals(baseFunc.getClass(), strictFunc.getClass());
        assertTrue(strictFunc instanceof StrictTabulatedFunction);
        assertFalse(baseFunc instanceof StrictTabulatedFunction);
    }
}