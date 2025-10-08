package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import exception.ArrayIsNotSortedException;
import exception.DifferentLengthOfArraysException;


import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для ArrayTabulatedFunction")
public class ArrayTabulatedFunctionTest {

    private ArrayTabulatedFunction createTestFunction() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {1.0, 4.0, 6.0, 8.0, 10.0};
        return new ArrayTabulatedFunction(x, y);
    }

    @Test
    @DisplayName("Конструктор с разными длинами массивов x и y должен выбрасывать IllegalArgumentException, так как количество точек не совпадает")
    void constructorDifferentLengthsThrows() {
        assertThrows(DifferentLengthOfArraysException.class, () ->
                new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{3}), "Длины массивов x и y должны быть равны GOOD");
    }
    @Test
    @DisplayName("Конструктор xFrom > xTo")
    void constructorXFromMorexTO() {
        assertThrows(DifferentLengthOfArraysException.class, () ->
                new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{3}), "Длины массивов x и y должны быть равны GOOD");
    }

    @Test
    @DisplayName("Конструктор с неупорядоченными значениями x должен выбрасывать IllegalArgumentException, так как точки должны быть строго возрастающими GOOD")
    void constructorUnsortedXThrows() {
        assertThrows(ArrayIsNotSortedException.class, () ->
                new ArrayTabulatedFunction(new double[]{2, 1}, new double[]{4, 5}), "Массив x должен быть строго возрастающим GOOD");
    }

    @Test
    @DisplayName("Конструктор с дублирующимися значениями x должен выбрасывать IllegalArgumentException, так как x-координаты должны быть уникальными GOOD")
    void constructorDuplicateXThrows() {
        assertThrows(ArrayIsNotSortedException.class, () ->
                new ArrayTabulatedFunction(new double[]{1, 1}, new double[]{2, 3}), "Значения x не могут повторяться GOOD");
    }

    @Test
    @DisplayName("Конструктор с корректными массивами x и y должен создавать объект с правильным количеством точек и значениями GOOD")
    void constructorValidArraysCreatesObject() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        assertEquals(3, f.getCount(), "Количество точек должно быть 3 GOOD");
        assertEquals(1.0, f.getX(0), 1e-10, "Первая x-координата должна быть 1.0 GOOD");
        assertEquals(4.0, f.getY(1), 1e-10, "Вторая y-координата должна быть 4.0 GOOD");
    }

    @Test
    @DisplayName("Конструктор не должен модифицировать входные массивы — оригиналы должны остаться неизменными GOOD")
    void constructorDoesNotModifyInputArrays() {
        double[] originalX = {1, 2, 3};
        double[] originalY = {1, 4, 9};
        double[] copyX = Arrays.copyOf(originalX, originalX.length);
        double[] copyY = Arrays.copyOf(originalY, originalY.length);

        new ArrayTabulatedFunction(originalX, originalY);

        assertArrayEquals(copyX, originalX, 1e-10, "Массив x не должен быть изменён GOOD");
        assertArrayEquals(copyY, originalY, 1e-10, "Массив y не должен быть изменён GOOD");
    }


    @Test
    @DisplayName("Конструктор с count < 2 должен выбрасывать IllegalArgumentException, так как требуется минимум 2 точки для интерполяции GOOD")
    void constructorWithMathFunctionCountTooSmallThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(x -> x, 0, 1, 1), "count должен быть >= 2 для корректной дискретизации GOOD");
    }

    @Test
    @DisplayName("Конструктор с xFrom == xTo должен создавать uniform-массив одинаковых значений, так как все точки лежат в одной координате GOOD")
    void constructorEqualBoundsCreatesUniformValues() {
        MathFunction f = x -> x * x;
        ArrayTabulatedFunction tab = new ArrayTabulatedFunction(f, 2.0, 2.0, 5);

        assertEquals(5, tab.getCount(), "Должно быть 5 точек GOOD");
        for (int i = 0; i < 5; i++) {
            assertEquals(2.0, tab.getX(i), 1e-10, "Все x должны быть 2.0 GOOD");
            assertEquals(4.0, tab.getY(i), 1e-10, "Все y должны быть 4.0 (2^2) GOOD");
        }
    }

    @Test
    @DisplayName("Конструктор с xFrom == xTo и count=2 должен создавать две одинаковые точки с одинаковыми y GOOD")
    void constructorEqualBoundsCountTwo() {
        MathFunction f = x -> x * x;
        ArrayTabulatedFunction tab = new ArrayTabulatedFunction(f, 2.0, 2.0, 2);

        assertEquals(2, tab.getCount(), "Должно быть 2 точки GOOD");
        assertEquals(2.0, tab.getX(0), 1e-10, "x[0] должен быть 2.0 GOOD");
        assertEquals(2.0, tab.getX(1), 1e-10, "x[1] должен быть 2.0 GOOD");
        assertEquals(4.0, tab.getY(0), 1e-10, "y[0] должен быть 4.0 GOOD");
        assertEquals(4.0, tab.getY(1), 1e-10, "y[1] должен быть 4.0 GOOD");
    }

    @Test
    @DisplayName("Конструктор с xFrom > xTo должен автоматически поменять границы местами, чтобы обеспечить возрастающий порядок GOOD")
    void constructorReversesBoundsIfNecessary() {
        MathFunction f = x -> x;
        ArrayTabulatedFunction tab = new ArrayTabulatedFunction(f, 5.0, 1.0, 5);

        assertEquals(5, tab.getCount(), "Должно быть 5 точек GOOD");
        assertEquals(1.0, tab.getX(0), 1e-10, "Первая x-точка должна быть 1.0 GOOD");
        assertEquals(5.0, tab.getX(4), 1e-10, "Последняя x-точка должна быть 5.0 GOOD");
        assertEquals(1.0, tab.getY(0), 1e-10, "Первая y-точка должна быть 1.0 GOOD");
        assertEquals(5.0, tab.getY(4), 1e-10, "Последняя y-точка должна быть 5.0 GOOD");
    }

    @Test
    @DisplayName("Конструктор с MathFunction должен корректно дискретизировать функцию на равномерной сетке от xFrom до xTo GOOD")
    void constructorWithMathFunctionCorrectDiscretization() {
        MathFunction f = x -> 2 * x + 1;
        ArrayTabulatedFunction tab = new ArrayTabulatedFunction(f, 0, 4, 5);

        // x: [0, 1, 2, 3, 4]
        // y: [1, 3, 5, 7, 9]
        for (int i = 0; i < 5; i++) {
            assertEquals(i, tab.getX(i), 1e-10, "x[" + i + "] должен быть " + i + " GOOD");
            assertEquals(2 * i + 1, tab.getY(i), 1e-10, "y[" + i + "] должен быть " + (2 * i + 1) + " GOOD");
        }
    }

    @Test
    @DisplayName("getCount должен возвращать количество точек в таблице GOOD")
    void getCountReturnsCorrectSize() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{1, 4, 9}
        );
        assertEquals(3, f.getCount(), "Количество точек должно быть 3 GOOD");
    }

    @Test
    @DisplayName("getX должен возвращать значение x по индексу GOOD")
    void getXReturnsCorrectValue() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{10, 20, 30},
                new double[]{1, 2, 3}
        );
        assertEquals(20, f.getX(1), 1e-10, "x[1] должен быть 20 GOOD");
    }

    @Test
    @DisplayName("getX с отрицательным или выходящим за пределы индексом должен выбрасывать IndexOutOfBoundsException GOOD")
    void getXOutOfBoundsThrows() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2},
                new double[]{1, 4}
        );
        assertThrows(IllegalArgumentException.class, () -> f.getX(-1), "Индекс -1 недопустим GOOD");
        assertThrows(IllegalArgumentException.class, () -> f.getX(2), "Индекс 2 вне диапазона [0,1] GOOD");
    }

    @Test
    @DisplayName("getY должен возвращать значение y по индексу GOOD")
    void getYReturnsCorrectValue() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{1, 4, 9}
        );
        assertEquals(4, f.getY(1), 1e-10, "y[1] должен быть 4 GOOD");
    }

    @Test
    @DisplayName("getY с некорректным индексом должен выбрасывать IndexOutOfBoundsException GOOD")
    void getYOutOfBoundsThrows() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2},
                new double[]{1, 4}
        );
        assertThrows(IndexOutOfBoundsException.class, () -> f.getY(-1), "Индекс -1 недопустим GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.getY(2), "Индекс 2 вне диапазона GOOD");
    }

    @Test
    @DisplayName("setY должен устанавливать новое значение y по индексу GOOD")
    void setYSetsCorrectValue() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{1, 4, 9}
        );
        f.setY(1, 100);
        assertEquals(100, f.getY(1), 1e-10, "y[1] должен быть обновлён до 100 GOOD");
    }

    @Test
    @DisplayName("setY с некорректным индексом должен выбрасывать IndexOutOfBoundsException GOOD")
    void setYOutOfBoundsThrows() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2},
                new double[]{1, 4}
        );
        assertThrows(IllegalArgumentException.class, () -> f.setY(-1, 5), "Индекс -1 недопустим GOOD");
        assertThrows(IllegalArgumentException.class, () -> f.setY(2, 5), "Индекс 2 вне диапазона GOOD");
    }

    @Test
    @DisplayName("indexOfX должен находить индекс точного совпадения x-значения GOOD")
    void indexOfXFindsExistingX() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1.0, 2.5, 4.0},
                new double[]{1, 6.25, 16}
        );
        assertEquals(1, f.indexOfX(2.5), "x=2.5 находится на индексе 1 GOOD");
        assertEquals(0, f.indexOfX(1.0), "x=1.0 находится на индексе 0 GOOD");
    }

    @Test
    @DisplayName("indexOfX должен возвращать -1, если x не найдено в таблице GOOD")
    void indexOfXReturnsMinusOneForNotFound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1, 4, 9}
        );
        assertEquals(-1, f.indexOfX(1.5), "x=1.5 отсутствует, должен вернуть -1 GOOD");
    }

    @Test
    @DisplayName("indexOfX должен работать с погрешностью 1e-10 — значения внутри допуска считаются равными GOOD")
    void indexOfXWorksWithTolerance() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1, 4, 9}
        );
        assertEquals(1, f.indexOfX(2.0 + 1e-11), "2.0 + 1e-11 попадает в допуск GOOD");
        assertEquals(-1, f.indexOfX(2.0 + 1e-9), "2.0 + 1e-9 выходит за допуск GOOD");
    }

    @Test
    @DisplayName("indexOfY должен находить индекс точного совпадения y-значения GOOD")
    void indexOfYFindsExistingY() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{1, 4, 9}
        );
        assertEquals(1, f.indexOfY(4), "y=4 находится на индексе 1 GOOD");
        assertEquals(2, f.indexOfY(9), "y=9 находится на индексе 2 GOOD");
    }

    @Test
    @DisplayName("indexOfY должен возвращать -1, если y не найдено в таблице GOOD")
    void indexOfYReturnsMinusOneForNotFound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{1, 4, 9}
        );
        assertEquals(-1, f.indexOfY(5), "y=5 отсутствует, должен вернуть -1 GOOD");
    }

    @Test
    @DisplayName("indexOfY должен работать с погрешностью 1e-10 — значения внутри допуска считаются равными GOOD")
    void indexOfYWorksWithTolerance() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{1, 4, 9}
        );
        assertEquals(1, f.indexOfY(4.0 + 1e-11), "4.0 + 1e-11 попадает в допуск GOOD");
        assertEquals(-1, f.indexOfY(4.0 + 1e-9), "4.0 + 1e-9 выходит за допуск GOOD");
    }


    @Test
    @DisplayName("leftBound должен возвращать минимальное значение x из таблицы GOOD")
    void leftBoundReturnsFirstX() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{10, 20, 30},
                new double[]{1, 2, 3}
        );
        assertEquals(10, f.leftBound(), 1e-10, "Минимальный x должен быть 10 GOOD");
    }

    @Test
    @DisplayName("rightBound должен возвращать максимальное значение x из таблицы GOOD")
    void rightBoundReturnsLastX() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{10, 20, 30},
                new double[]{1, 2, 3}
        );
        assertEquals(30, f.rightBound(), 1e-10, "Максимальный x должен быть 30 GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX при x <= минимальному значению должен возвращать 0 — ближайшая слева точка GOOD")
    void floorIndexOfXUnderLeftBound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{1, 4, 9}
        );
        assertThrows(IllegalArgumentException.class,()-> f.floorIndexOfX(0.5), "x=0.5 < 1 → floorIndex = 0 GOOD");
        assertEquals(0, f.floorIndexOfX(1.0), "x=1.0 — граница → floorIndex = 0 GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX при x >= максимальному значению должен возвращать count-1 — последний индекс GOOD")
    void floorIndexOfXOverRightBound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{1, 4, 9}
        );
        assertEquals(2, f.floorIndexOfX(3.0), "x=3.0 — граница → floorIndex = 2 GOOD");
        assertThrows(IllegalArgumentException.class,()-> f.floorIndexOfX(4.0), "x=4.0 > 3 → floorIndex = 2 (последний) GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX должен возвращать индекс самой правой точки, меньшей или равной x, внутри диапазона GOOD")
    void floorIndexOfXInMiddle() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3, 4},
                new double[]{1, 4, 9, 16}
        );
        assertEquals(0, f.floorIndexOfX(1.5), "x=1.5 между 1 и 2 → floorIndex = 0 GOOD");
        assertEquals(1, f.floorIndexOfX(2.3), "x=2.3 между 2 и 3 → floorIndex = 1 GOOD");
        assertEquals(2, f.floorIndexOfX(3.9), "x=3.9 между 3 и 4 → floorIndex = 2 GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX с двумя точками должен корректно определять положение x относительно отрезка GOOD")
    void floorIndexOfXWithTwoPoints() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{0, 1},
                new double[]{0, 1}
        );
        //assertThrows(InterpolationException.class,()-> f.floorIndexOfX(0.5), "x=0.5 между 0 и 1 → floorIndex = 0 GOOD");
        assertEquals(0, f.floorIndexOfX(0.0), "x=0.0 — начало → floorIndex = 0 GOOD");
        assertEquals(1, f.floorIndexOfX(1.0), "x=1.0 — конец → floorIndex = 1 GOOD");
        assertThrows(IllegalArgumentException.class,()-> f.floorIndexOfX(2.0), "x=2.0 > 1 → floorIndex = 1 (последний) GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX никогда не должен возвращать -42 — это защита от ошибок реализации GOOD")
    void floorIndexOfXNeverReturnsMinus42() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{0, 1, 2},
                new double[]{0, 1, 4}
        );
        double x = -1;
        assertThrows(IllegalArgumentException.class, ()->f.floorIndexOfX(x), "Метод floorIndexOfX не должен возвращать -42 GOOD");

    }

    @Test
    @DisplayName("floorIndexOfX на пустой функции (count=0) должен выбрасывать IndexOutOfBoundsException")
    void floorIndexOfXOnEmptyFunctionThrows() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1,2}, new double[]{1,2});
        f.remove(0); //count = 0
        f.remove(0); //count = 0
        assertThrows(IndexOutOfBoundsException.class, () -> f.floorIndexOfX(5.0), "Нельзя вызвать floorIndexOfX на пустой функции GOOD");
    }

    @Test
    @DisplayName("interpolate должен выполнять линейную интерполяцию между двумя точками по заданному floorIndex")
    void interpolateCorrectly() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{0, 2},
                new double[]{0, 4}
        );
        //y = 2x
        assertEquals(2, f.interpolate(1, 0), 1e-10, "Интерполяция между (0,0) и (2,4) в x=1 даёт y=2 GOOD");
    }

    @Test
    @DisplayName("interpolate с floorIndex=0 и двумя точками должен корректно интерполировать по первому отрезку")
    void interpolateWithFloorZero() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 3},
                new double[]{1, 5}
        );
        assertEquals(3, f.interpolate(2, 0), 1e-10, "Интерполяция между (1,1) и (3,5) в x=2 даёт y=3 GOOD");
    }

    @Test
    @DisplayName("interpolate с некорректным floorIndex должен выбрасывать IndexOutOfBoundsException")
    void interpolateInvalidFloorIndexThrows() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{1, 4, 9}
        );
        assertThrows(IndexOutOfBoundsException.class, () -> f.interpolate(1.5, -1), "floorIndex=-1 недопустим GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.interpolate(1.5, 2), "floorIndex=2 выходит за пределы [0,1] GOOD");
    }

    @Test
    @DisplayName("interpolate с одной точкой должен возвращать её y, даже если floorIndex не соответствует диапазону")
    void interpolateSinglePoint() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1,2},
                new double[]{5,6}
        );
        f.remove(1);
        assertEquals(5, f.interpolate(10, 0), 1e-10, "Одна точка: всегда возвращаем её y=5 GOOD");
    }

    @Test
    @DisplayName("interpolate на пустой функции должен выбрасывать IndexOutOfBoundsException")
    void interpolateOnEmptyFunctionThrows() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1,2}, new double[]{1,2});
        f.remove(0); // count = 0
        f.remove(0); // count = 0
        assertThrows(IndexOutOfBoundsException.class, () -> f.interpolate(1.5, 0), "Нельзя интерполировать на пустой функции GOOD");
    }

/*
    @Test
    @DisplayName("extrapolateLeft с одной точкой должен возвращать её y, независимо от x")
    void extrapolateLeftSinglePoint() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1,2},
                new double[]{5,6}
        );
        //f.remove(1);
        assertEquals(5, f.extrapolateLeft(0), 1e-10, "Экстраполяция влево от одной точки даёт y=5 GOOD");
        assertEquals(5, f.extrapolateLeft(10), 1e-10, "Экстраполяция влево от одной точки даёт y=5 GOOD");
    }*/

    @Test
    @DisplayName("extrapolateLeft с несколькими точками должен использовать первый отрезок для экстраполяции влево")
    void extrapolateLeftUsesFirstSegment() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2},
                new double[]{1, 3}
        );
        // Линия: y = 2x - 1
        assertEquals(-1, f.extrapolateLeft(0), 1e-10, "Экстраполяция влево от x=0: 2*0 - 1 = -1 GOOD");
    }

    @Test
    @DisplayName("extrapolateRight с одной точкой должен возвращать её y, независимо от x")
    void extrapolateRightSinglePoint() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1,2},
                new double[]{5,6}
        );
        assertEquals(4, f.extrapolateRight(0), 1e-10, "Экстраполяция вправо от одной точки даёт y=5 GOOD");
        assertEquals(14, f.extrapolateRight(10), 1e-10, "Экстраполяция вправо от одной точки даёт y=5 GOOD");
    }

    @Test
    @DisplayName("extrapolateRight с несколькими точками должен использовать последний отрезок для экстраполяции вправо")
    void extrapolateRightUsesLastSegment() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2},
                new double[]{1, 3}
        );
        //y = 2x - 1
        assertEquals(5, f.extrapolateRight(3), 1e-10, "Экстраполяция вправо от x=3: 2*3 - 1 = 5 GOOD");
    }


    @Test
    @DisplayName("apply на пустой функции должен выбрасывать IndexOutOfBoundsException")
    void applyOnEmptyFunctionThrows() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1,2}, new double[]{1,2});
        f.remove(0); // count = 0
        f.remove(0); // count = 0
        assertThrows(IndexOutOfBoundsException.class, () -> f.apply(5.0), "Нельзя применить функцию к пустой таблице GOOD");
    }


    @Test
    @DisplayName("andThen должен создавать композицию f.andThen(g), где g применяется после f")
    void andThenAppliesAfterFunction() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{0, 1, 2},
                new double[]{0, 1, 2}
        );

        MathFunction after = x -> x * 2;
        MathFunction composed = f.andThen(after);

        assertEquals(0, composed.apply(0), 1e-10, "f(0)=0, then g(0)=0 GOOD");
        assertEquals(2, composed.apply(1), 1e-10, "f(1)=1, then g(1)=2 GOOD");
    }

    @Test
    @DisplayName("andThen не должен изменять оригинал — исходная таблица должна остаться нетронутой")
    void andThenDoesNotModifyOriginal() {
        ArrayTabulatedFunction f = createTestFunction();
        MathFunction after = x -> x * 2;
        MathFunction composed = f.andThen(after);

        //Проверяем, что f не изменился
        assertEquals(5, f.getCount(), "Оригинал должен сохранить 5 точек GOOD");
        assertEquals(1.0, f.getX(0), 1e-10, "f.x[0] должен остаться 1.0 GOOD");
        assertEquals(10.0, f.getY(4), 1e-10, "f.y[4] должен остаться 10.0 GOOD");
    }

    @Test
    @DisplayName("getxVal должен возвращать копию массива x с правильной длиной — изменения копии не влияют на оригинал")
    void getxValReturnsCopyAndCorrectLength() {
        ArrayTabulatedFunction f = createTestFunction();
        double[] xCopy = f.getxVal();

        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0, 5.0}, xCopy, 1e-10, "Копия x должна содержать [1,2,3,4,5]");
        assertEquals(5, xCopy.length, "Длина копии x должна быть 5 GOOD");

        //Изменяем копию — оригинал не должен меняться
        xCopy[0] = 999;
        assertEquals(1.0, f.getX(0), 1e-10, "Оригинал x[0] не должен измениться GOOD");
    }

    @Test
    @DisplayName("getyVal должен возвращать копию массива y с правильной длиной — изменения копии не влияют на оригинал ")
    void getyValReturnsCopyAndCorrectLength() {
        ArrayTabulatedFunction f = createTestFunction();
        double[] yCopy = f.getyVal();

        assertArrayEquals(new double[]{1.0, 4.0, 6.0, 8.0, 10.0}, yCopy, 1e-10, "Копия y должна содержать [1,4,6,8,10] GOOD");
        assertEquals(5, yCopy.length, "Длина копии y должна быть 5 GOOD");

        // Изменяем копию — оригинал не должен меняться
        yCopy[0] = 999;
        assertEquals(1.0, f.getY(0), 1e-10, "Оригинал y[0] не должен измениться GOOD");
    }

    @Test
    @DisplayName("getxVal и getyVal после удаления элементов должны возвращать только первые count элементов")
    void getxValGetyValAfterRemoval() {
        ArrayTabulatedFunction f = createTestFunction();
        f.remove(2); //удалили x=3.0, y=6.0 → теперь count=4
        f.remove(3); //удалили x=4.0, y=8.0 → теперь count=3

        double[] xCopy = f.getxVal();
        double[] yCopy = f.getyVal();

        assertEquals(3, xCopy.length, "Длина x после двух удалений должна быть 3 GOOD");
        assertEquals(3, yCopy.length, "Длина y после двух удалений должна быть 3 GOOD");

        assertArrayEquals(new double[]{1.0, 2.0, 4.0}, xCopy, 1e-10, "Оставшиеся x: [1,2,4] GOOD");
        assertArrayEquals(new double[]{1.0, 4.0, 8.0}, yCopy, 1e-10, "Оставшиеся y: [1,4,8] GOOD");
    }

    @Test
    @DisplayName("remove среднего элемента должен корректно удалить точку и сместить остальные")
    void testRemoveMiddleElement() {
        ArrayTabulatedFunction f = createTestFunction();
        assertEquals(5, f.getCount(), "Изначально 5 точек GOOD");

        f.remove(2); //удаляем x=3.0, y=6.0

        assertEquals(4, f.getCount(), "После удаления осталось 4 точки GOOD");

        assertEquals(1.0, f.getX(0), "x[0] = 1.0 GOOD");
        assertEquals(2.0, f.getX(1), "x[1] = 2.0 GOOD");
        assertEquals(4.0, f.getX(2), "x[2] = 4.0 GOOD");
        assertEquals(5.0, f.getX(3), "x[3] = 5.0 GOOD");

        assertEquals(1.0, f.getY(0), "y[0] = 1.0 GOOD");
        assertEquals(4.0, f.getY(1), "y[1] = 4.0 GOOD");
        assertEquals(8.0, f.getY(2), "y[2] = 8.0 GOOD");
        assertEquals(10.0, f.getY(3), "y[3] = 10.0 GOOD");

        assertEquals(6.0, f.interpolate(3.0, 1), 1e-10, "Интерполяция между x=2 и x=4: (4+8)/2 = 6 GOOD");
        assertEquals(6.0, f.apply(3.0), 1e-10, "apply(3.0) через интерполяцию даёт 6.0 GOOD");
    }

    @Test
    @DisplayName("remove первого элемента должен корректно удалить первую точку и сдвинуть массив")
    void testRemoveFirstElement() {
        ArrayTabulatedFunction f = createTestFunction();
        f.remove(0); //удаляем x=1.0, y=1.0

        assertEquals(4, f.getCount(), "Осталось 4 точки GOOD");

        assertEquals(2.0, f.getX(0), "x[0] = 2.0 GOOD");
        assertEquals(3.0, f.getX(1), "x[1] = 3.0 GOOD");
        assertEquals(4.0, f.getX(2), "x[2] = 4.0 GOOD");
        assertEquals(5.0, f.getX(3), "x[3] = 5.0 GOOD");

        assertEquals(4.0, f.getY(0), "y[0] = 4.0 GOOD");
        assertEquals(6.0, f.getY(1), "y[1] = 6.0 GOOD");
        assertEquals(8.0, f.getY(2), "y[2] = 8.0 GOOD");
        assertEquals(10.0, f.getY(3), "y[3] = 10.0 GOOD");

        assertEquals(3.0, f.extrapolateLeft(1.5), 1e-10, "Экстраполяция влево от x=1.5: между (2,4) и (3,6) → 3.0 GOOD");
        assertEquals(3.0, f.apply(1.5), 1e-10, "apply(1.5) через экстраполяцию даёт 3.0 GOOD");
    }

    @Test
    @DisplayName("remove последнего элемента должен корректно удалить последнюю точку")
    void testRemoveLastElement() {
        ArrayTabulatedFunction f = createTestFunction();
        f.remove(4); //удаляем x=5.0, y=10.0

        assertEquals(4, f.getCount(), "Осталось 4 точки GOOD");

        assertEquals(1.0, f.getX(0), "x[0] = 1.0 GOOD");
        assertEquals(2.0, f.getX(1), "x[1] = 2.0 GOOD");
        assertEquals(3.0, f.getX(2), "x[2] = 3.0 GOOD");
        assertEquals(4.0, f.getX(3), "x[3] = 4.0 GOOD");

        assertEquals(1.0, f.getY(0), "y[0] = 1.0 GOOD");
        assertEquals(4.0, f.getY(1), "y[1] = 4.0 GOOD");
        assertEquals(6.0, f.getY(2), "y[2] = 6.0 GOOD");
        assertEquals(8.0, f.getY(3), "y[3] = 8.0 GOOD");

        assertEquals(12.0, f.extrapolateRight(6.0), 1e-10, "Экстраполяция вправо: между (4,8) и (5,10) → 8 + 2*(6-4) = 12 GOOD");
        assertEquals(12.0, f.apply(6.0), 1e-10, "apply(6.0) через экстраполяцию даёт 12.0 GOOD");
    }

    @Test
    @DisplayName("remove нескольких элементов должен корректно обновлять таблицу и сохранять порядок")
    void testRemoveMultipleElements() {
        ArrayTabulatedFunction f = createTestFunction();

        f.remove(0); //удаляем (1,1)
        f.remove(1); //удаляем (3,6) — теперь на позиции 1 был (4,8)
        f.remove(1); //удаляем (4,8) — теперь осталось (2,4) и (5,10)

        assertEquals(2, f.getCount(), "Осталось 2 точки GOOD");

        assertEquals(2.0, f.getX(0), "x[0] = 2.0 GOOD");
        assertEquals(5.0, f.getX(1), "x[1] = 5.0 GOOD");

        assertEquals(4.0, f.getY(0), "y[0] = 4.0 GOOD");
        assertEquals(10.0, f.getY(1), "y[1] = 10.0 GOOD");

        assertEquals(7.0, f.apply(3.5), 1e-10, "Интерполяция между (2,4) и (5,10): 4 + (10-4)/(5-2)*(3.5-2) = 4 + 2*1.5 = 7 GOOD");
        assertEquals(0, f.floorIndexOfX(3.5), "floorIndexOfX(3.5) = 0 — ближайшая слева точка — x=2 GOOD");
        assertEquals(2.0, f.extrapolateLeft(1.0), 1e-10, "Экстраполяция влево: 4 - (10-4)/(5-2)*(2-1) = 4 - 2 = 2 GOOD");
        assertEquals(12.0, f.extrapolateRight(6.0), 1e-10, "Экстраполяция вправо: 10 + 2*(6-5) = 12 GOOD");
    }

    @Test
    @DisplayName("remove единственной точки должен очистить таблицу — все методы должны бросать исключения")
    void testRemoveFromSizeOne() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{10.0, 11}, new double[]{100.0, 110});
        f.remove(1);
        assertEquals(1, f.getCount(), "Изначально одна точка GOOD");

        f.remove(0);

        assertEquals(0, f.getCount(), "После удаления — 0 точек GOOD");

        assertThrows(IllegalArgumentException.class, () -> f.getX(0), "getX(0) на пустой таблице — исключение GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.getY(0), "getY(0) на пустой таблице — исключение GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.interpolate(15.0, 0), "interpolate на пустой таблице — исключение GOOD");
        assertThrows(IllegalArgumentException.class, ()-> f.extrapolateLeft(15.0), "extrapolateLeft на пустой таблице — исключение GOOD");
        assertThrows(IllegalArgumentException.class, () -> f.extrapolateRight(15.0), "extrapolateRight на пустой таблице — исключение GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.apply(15.0), "apply на пустой таблице — исключение GOOD");
    }

    @Test
    @DisplayName("remove с отрицательным индексом должен выбрасывать IndexOutOfBoundsException")
    void testRemoveNegativeIndex() {
        ArrayTabulatedFunction f = createTestFunction();
        assertThrows(IndexOutOfBoundsException.class, () -> f.remove(-1), "Индекс -1 недопустим GOOD");
    }

    @Test
    @DisplayName("remove с индексом больше размера должен выбрасывать IndexOutOfBoundsException")
    void testRemoveIndexTooLarge() {
        ArrayTabulatedFunction f = createTestFunction();
        assertThrows(IndexOutOfBoundsException.class, () -> f.remove(5), "Индекс 5 вне диапазона [0,4] GOOD");
    }

    @Test
    @DisplayName("remove после полного удаления всех точек должен запрещать дальнейшие удаления")
    void testRemoveOnEmptyAfterAllRemoved() {
        ArrayTabulatedFunction f = createTestFunction();
        f.remove(0);
        f.remove(0);
        f.remove(0);
        f.remove(0);
        assertEquals(1, f.getCount(), "Осталась одна точка GOOD");
        f.remove(0);
        assertEquals(0, f.getCount(), "Все точки удалены GOOD");

        assertThrows(IndexOutOfBoundsException.class, () -> f.remove(0), "Удаление из пустой таблицы — исключение GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX должен корректно работать после удаления элемента — индексы пересчитаны")
    void testFloorIndexOfXAfterRemoval() {
        ArrayTabulatedFunction f = createTestFunction();
        f.remove(2); // удалили x=3.0

        assertEquals(0, f.floorIndexOfX(1.0), "x=1.0 — точка 0 GOOD");
        assertEquals(0, f.floorIndexOfX(1.5), "x=1.5 — между 1 и 2 → floorIndex=0 GOOD");
        assertEquals(1, f.floorIndexOfX(2.0), "x=2.0 — точка 1 GOOD");
        assertEquals(1, f.floorIndexOfX(2.5), "x=2.5 — между 2 и 4 → floorIndex=1 GOOD");
        assertEquals(1, f.floorIndexOfX(3.0), "x=3.0 — между 2 и 4 → floorIndex=1 GOOD");
        assertEquals(1, f.floorIndexOfX(3.9), "x=3.9 — между 2 и 4 → floorIndex=1 GOOD");
        assertEquals(2, f.floorIndexOfX(4.0), "x=4.0 — точка 2 GOOD");
        assertEquals(2, f.floorIndexOfX(4.5), "x=4.5 — между 4 и 5 → floorIndex=2 GOOD");
        assertEquals(3, f.floorIndexOfX(5.0), "x=5.0 — точка 3 GOOD");
        assertThrows(IllegalArgumentException.class,()-> f.floorIndexOfX(10.0), "x=10.0 > max → Исключение GOOD");
        assertThrows(IllegalArgumentException.class,()-> f.floorIndexOfX(0.5), "x=0.5 < min → floorIndex=0 GOOD");
    }

    @Test
    @DisplayName("apply должен корректно работать после удаления — интерполяция и экстраполяция должны сохраняться")
    void testApplyAfterRemoval() {
        ArrayTabulatedFunction f = createTestFunction();
        f.remove(1); // удаляем x=2.0, y=4.0 → теперь точки: [1, 3, 4, 5] с y=[1,6,8,10]

        assertEquals(3.5, f.apply(2.0), 1e-10, "apply(2.0): между (1,1) и (3,6) → 1 + (6-1)/(3-1)*(2-1) = 1 + 2.5 = 3.5 GOOD");
        assertEquals(7.0, f.apply(3.5), 1e-10, "apply(3.5): между (3,6) и (4,8) → 6 + (8-6)/(4-3)*(3.5-3) = 6 + 1 = 7 GOOD");
        assertEquals(12.0, f.apply(6.0), 1e-10, "apply(6.0): экстраполяция справа: 10 + (10-8)/(5-4)*(6-5) = 10 + 2 = 12 GOOD");
    }

    @Test
    @DisplayName("indexOfX и indexOfY должны корректно возвращать -1 для удалённых значений")
    void testIndexOfXAndYAfterRemoval() {
        ArrayTabulatedFunction f = createTestFunction();
        f.remove(2); // удалили x=3.0, y=6.0

        assertEquals(0, f.indexOfX(1.0), "x=1.0 есть → индекс 0 GOOD");
        assertEquals(1, f.indexOfX(2.0), "x=2.0 есть → индекс 1 GOOD");
        assertEquals(-1, f.indexOfX(3.0), "x=3.0 удалён → -1 GOOD");
        assertEquals(2, f.indexOfX(4.0), "x=4.0 есть → индекс 2 GOOD");
        assertEquals(3, f.indexOfX(5.0), "x=5.0 есть → индекс 3 GOOD");

        assertEquals(0, f.indexOfY(1.0), "y=1.0 есть → индекс 0 GOOD");
        assertEquals(1, f.indexOfY(4.0), "y=4.0 есть → индекс 1 GOOD");
        assertEquals(-1, f.indexOfY(6.0), "y=6.0 удалён → -1 GOOD");
        assertEquals(2, f.indexOfY(8.0), "y=8.0 есть → индекс 2 GOOD");
        assertEquals(3, f.indexOfY(10.0), "y=10.0 есть → индекс 3 GOOD");
    }

    @Test
    @DisplayName("После удаления всех точек все методы проверка")
    void testAllMethodsThrowAfterLastPointRemoved() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1,2}, new double[]{1,2});
        f.remove(0);
        f.remove(0);

        assertThrows(IllegalArgumentException.class, () -> f.getX(0), "getX — исключение GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.getY(0), "getY — исключение GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.leftBound(), "leftBound — исключение GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.rightBound(), "rightBound — исключение GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.floorIndexOfX(0.5), "floorIndexOfX — исключение GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.interpolate(0.5, 0), "interpolate — исключение GOOD");
        assertThrows(IllegalArgumentException.class,()-> f.extrapolateLeft(0.5), "extrapolateLeft — исключение GOOD");
        assertThrows(IllegalArgumentException.class, () -> f.extrapolateRight(0.5), "extrapolateRight — исключение GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> f.apply(0.5), "apply — исключение GOOD");
        assertEquals(-1, f.indexOfX(0.5), "indexOfX — возвращает -1, даже при count=0 GOOD");
        assertEquals(-1, f.indexOfY(0.5), "indexOfY — возвращает -1, даже при count=0 GOOD");
    }

    @Test
    @DisplayName("Композиция ArrayTabulatedFunction с ArrayTabulatedFunction: f(g(x)) = (2x+3)+1 = 2x+4 — точечные значения")
    void testArrayWithArray() {
        //f(x) = x + 1
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = 2x + 3
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {3.0, 5.0, 7.0, 9.0, 11.0};
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(x2, y2);

        CompositeFunction composition = new CompositeFunction(f, g);
        //f(g(x)) = g(x) + 1 = (2x + 3) + 1 = 2x + 4

        assertEquals(4.0, composition.apply(0.0), 1e-8, "f(g(0)) = f(3) = 4 GOOD");
        assertEquals(6.0, composition.apply(1.0), 1e-8, "f(g(1)) = f(5) = 6 GOOD");
        assertEquals(8.0, composition.apply(2.0), 1e-8, "f(g(2)) = f(7) = 8 GOOD");
        assertEquals(10.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(9) = 10 GOOD");

        assertEquals(4.5, composition.apply(0.25), 1e-8, "f(g(0.25)) = f(3.5) = 4.5 (интерполяция в f) GOOD");
        assertEquals(5.0, composition.apply(0.5), 1e-8, "f(g(0.5)) = f(4.0) = 5.0 (интерполяция в f) GOOD");
        assertEquals(5.5, composition.apply(0.75), 1e-8, "f(g(0.75)) = f(4.5) = 5.5 (интерполяция в f) GOOD");
        assertEquals(6.5, composition.apply(1.25), 1e-8, "f(g(1.25)) = f(5.5) = 6.5 (интерполяция в f) GOOD");
        assertEquals(7.0, composition.apply(1.5), 1e-8, "f(g(1.5)) = f(6.0) = 7.0 (интерполяция в f) GOOD");
    }

    @Test
    @DisplayName("Композиция ArrayTabulatedFunction с andThen(g): f.andThen(g) = g(f(x)) = 2*(x+1)+3 = 2x+5 — проверка значений")
    void testArrayWithArrayAndThen() {
        //f(x) = x + 1
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = 2x + 3
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {3.0, 5.0, 7.0, 9.0, 11.0};
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(x2, y2);

        MathFunction composition = f.andThen(g);
        //f.andThen(g) = f(g(x)) = 2*(x+1)+3 = 2x + 5

        assertEquals(4.0, composition.apply(0.0), 1e-8, "f(g(0)) = f(1) = 5 GOOD");
        assertEquals(6.0, composition.apply(1.0), 1e-8, "f(g(1)) = f(2) = 7 GOOD");
        assertEquals(8.0, composition.apply(2.0), 1e-8, "f(g(2)) = f(3) = 9 GOOD");
        assertEquals(10.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(4) = 11 GOOD");

        assertEquals(4.5, composition.apply(0.25), 1e-8, "f(g(0.25)) = f(1.25) ≈ 5.5 (интерполяция в g) GOOD");
        assertEquals(5.0, composition.apply(0.5), 1e-8, "f(g(0.5)) = f(1.5) = 6.0 (интерполяция в g) GOOD");
        assertEquals(5.5, composition.apply(0.75), 1e-8, "f(g(0.75)) = f(1.75) ≈ 6.5 (интерполяция в g) GOOD");
        assertEquals(6.5, composition.apply(1.25), 1e-8, "f(g(1.25)) = f(2.25) ≈ 7.5 (интерполяция в g) GOOD");
        assertEquals(7.0, composition.apply(1.5), 1e-8, "f(g(1.5)) = f(2.5) = 8.0 (интерполяция в g) GOOD");
    }

    @Test
    @DisplayName("Композиция f(g(x)) = ln(x+1) + 1 — проверка логарифмической композиции с интерполяцией")
    void testArrayWithArrayExponentialWithLogarithmicComposition() {
        //f(x) = x + 1
        double[] x1 = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] y1 = {1.0, 1.5, 2.0, 2.5, 3.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = ln(x + 1)
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {0.0, 0.6931, 1.0986, 1.3863, 1.6094};
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = ln(x + 1) + 1
        CompositeFunction composition = new CompositeFunction(f, g);

        assertEquals(1.0, composition.apply(0.0), 0.1, "f(g(0)) = f(0) = 1 GOOD");
        assertEquals(1.6931, composition.apply(1.0), 0.1, "f(g(1)) = f(ln2) ≈ f(0.6931) ≈ 1.6931 GOOD");
        assertEquals(2.0986, composition.apply(2.0), 0.1, "f(g(2)) = f(ln3) ≈ f(1.0986) ≈ 2.0986 GOOD");
        assertEquals(2.3863, composition.apply(3.0), 0.1, "f(g(3)) = f(ln4) ≈ f(1.3863) ≈ 2.3863 GOOD");
        assertEquals(2.6094, composition.apply(4.0), 0.1, "f(g(4)) = f(ln5) ≈ f(1.6094) ≈ 2.6094 GOOD");
    }

    @Test
    @DisplayName("Композиция sin(cos(x)) — тригонометрическая композиция с интерполяцией")
    void testArrayWithArrayTrigonometricComposition() {
        //f(x) = sin(x)
        double[] x1 = {0.0, Math.PI/6, Math.PI/4, Math.PI/3, Math.PI/2};
        double[] y1 = {0.0, 0.5, 0.7071, 0.8660, 1.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = cos(x)
        double[] x2 = {0.0, Math.PI/6, Math.PI/4, Math.PI/3, Math.PI/2};
        double[] y2 = {1.0, 0.8660, 0.7071, 0.5, 0.0};
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = sin(cos(x))
        CompositeFunction composition = new CompositeFunction(f, g);

        assertEquals(Math.sin(1.0), composition.apply(0.0), 0.1, "sin(cos(0)) = sin(1) GOOD");
        assertEquals(Math.sin(0.8660), composition.apply(Math.PI/6), 0.1, "sin(cos(π/6)) = sin(√3/2) ≈ sin(0.8660) GOOD");
        assertEquals(Math.sin(0.7071), composition.apply(Math.PI/4), 0.1, "sin(cos(π/4)) = sin(√2/2) ≈ sin(0.7071) GOOD");
        assertEquals(Math.sin(0.5), composition.apply(Math.PI/3), 0.1, "sin(cos(π/3)) = sin(0.5) GOOD");
        assertEquals(Math.sin(0.0), composition.apply(Math.PI/2), 0.1, "sin(cos(π/2)) = sin(0) = 0 GOOD");
    }

    @Test
    @DisplayName("Композиция √(x-3) — экстраполяция влево и вправо для составной функции")
    void testArrayWithArrayCompositeWithExtrapolation() {
        //f(x) = √x
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {0.0, 1.0, 1.4142, 1.7321, 2.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = x - 3
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        double[] y2 = {-3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0};
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = √(x - 3)
        CompositeFunction composition = new CompositeFunction(f, g);

        assertTrue(composition.apply(0.0) < 0, "f(g(0)) = f(-3) — экстраполяция влево → результат < 0 GOOD");
        assertTrue(composition.apply(1.0) < 0, "f(g(1)) = f(-2) — экстраполяция влево → результат < 0 GOOD");
        assertTrue(composition.apply(2.0) < 0, "f(g(2)) = f(-1) — экстраполяция влево → результат < 0 GOOD");

        assertEquals(0.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(0) = 0 GOOD");
        assertEquals(1.0, composition.apply(4.0), 1e-8, "f(g(4)) = f(1) = 1 GOOD");
        assertEquals(1.4142, composition.apply(5.0), 0.1, "f(g(5)) = f(2) = √2 ≈ 1.4142 GOOD");
        assertEquals(1.7321, composition.apply(6.0), 0.1, "f(g(6)) = f(3) = √3 ≈ 1.7321 GOOD");

        assertEquals(2.236, composition.apply(8.0), 0.1, "f(g(8)) = f(5) — экстраполяция вправо → √5 ≈ 2.236 GOOD");
    }

    @Test
    @DisplayName("Композиция LinkedListTabulatedFunction с ArrayTabulatedFunction: f(g(x)) = (2x+3)+1 = 2x+4 — проверка типов")
    void testArrayWithLinkedList() {
        //f(x) = x + 1
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = 2x + 3
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {3.0, 5.0, 7.0, 9.0, 11.0};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        CompositeFunction composition = new CompositeFunction(f, g);
        //f(g(x)) = (2x + 3) + 1 = 2x + 4

        assertEquals(4.0, composition.apply(0.0), 1e-8, "f(g(0)) = f(3) = 4 GOOD");
        assertEquals(6.0, composition.apply(1.0), 1e-8, "f(g(1)) = f(5) = 6 GOOD");
        assertEquals(8.0, composition.apply(2.0), 1e-8, "f(g(2)) = f(7) = 8 GOOD");
        assertEquals(10.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(9) = 10 GOOD");

        assertEquals(4.5, composition.apply(0.25), 1e-8, "f(g(0.25)) = f(3.5) = 4.5 (интерполяция в f) GOOD");
        assertEquals(5.0, composition.apply(0.5), 1e-8, "f(g(0.5)) = f(4.0) = 5.0 (интерполяция в f) GOOD");
        assertEquals(5.5, composition.apply(0.75), 1e-8, "f(g(0.75)) = f(4.5) = 5.5 (интерполяция в f) GOOD");
        assertEquals(6.5, composition.apply(1.25), 1e-8, "f(g(1.25)) = f(5.5) = 6.5 (интерполяция в f) GOOD");
        assertEquals(7.0, composition.apply(1.5), 1e-8, "f(g(1.5)) = f(6.0) = 7.0 (интерполяция в f) GOOD");
    }

    @Test
    @DisplayName("Композиция LinkedListTabulatedFunction с ArrayTabulatedFunction: ln(x+1)+1 — логарифмическая композиция")
    void testArrayWithLinkedListExponentialWithLogarithmicComposition() {
        //f(x) = x + 1
        double[] x1 = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] y1 = {1.0, 1.5, 2.0, 2.5, 3.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = ln(x + 1)
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {0.0, 0.6931, 1.0986, 1.3863, 1.6094};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = ln(x + 1) + 1
        CompositeFunction composition = new CompositeFunction(f, g);

        assertEquals(1.0, composition.apply(0.0), 0.1, "f(g(0)) = f(0) = 1 GOOD");
        assertEquals(1.6931, composition.apply(1.0), 0.1, "f(g(1)) = f(ln2) ≈ 1.6931 GOOD");
        assertEquals(2.0986, composition.apply(2.0), 0.1, "f(g(2)) = f(ln3) ≈ 2.0986 GOOD");
        assertEquals(2.3863, composition.apply(3.0), 0.1, "f(g(3)) = f(ln4) ≈ 2.3863 GOOD");
        assertEquals(2.6094, composition.apply(4.0), 0.1, "f(g(4)) = f(ln5) ≈ 2.6094 GOOD");
    }

    @Test
    @DisplayName("Композиция LinkedListTabulatedFunction с ArrayTabulatedFunction: sin(cos(x)) — тригонометрическая композиция")
    void testArrayWithLinkedListTrigonometricComposition() {
        //f(x) = sin(x)
        double[] x1 = {0.0, Math.PI/6, Math.PI/4, Math.PI/3, Math.PI/2};
        double[] y1 = {0.0, 0.5, 0.7071, 0.8660, 1.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = cos(x)
        double[] x2 = {0.0, Math.PI/6, Math.PI/4, Math.PI/3, Math.PI/2};
        double[] y2 = {1.0, 0.8660, 0.7071, 0.5, 0.0};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = sin(cos(x))
        CompositeFunction composition = new CompositeFunction(f, g);

        assertEquals(Math.sin(1.0), composition.apply(0.0), 0.1, "sin(cos(0)) = sin(1) GOOD");
        assertEquals(Math.sin(0.8660), composition.apply(Math.PI/6), 0.1, "sin(cos(π/6)) = sin(√3/2) ≈ sin(0.8660) GOOD");
        assertEquals(Math.sin(0.7071), composition.apply(Math.PI/4), 0.1, "sin(cos(π/4)) = sin(√2/2) ≈ sin(0.7071) GOOD");
        assertEquals(Math.sin(0.5), composition.apply(Math.PI/3), 0.1, "sin(cos(π/3)) = sin(0.5) GOOD");
        assertEquals(Math.sin(0.0), composition.apply(Math.PI/2), 0.1, "sin(cos(π/2)) = sin(0) = 0 GOOD");
    }

    @Test
    @DisplayName("Композиция LinkedListTabulatedFunction с ArrayTabulatedFunction: √(x-3) — экстраполяция влево/вправо")
    void testArrayWithLinkedListCompositeWithExtrapolation() {
        //f(x) = √x
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {0.0, 1.0, 1.4142, 1.7321, 2.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = x - 3
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        double[] y2 = {-3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = √(x - 3)
        CompositeFunction composition = new CompositeFunction(f, g);

        assertTrue(composition.apply(0.0) < 0, "f(g(0)) = f(-3) — экстраполяция влево → результат < 0 GOOD");
        assertTrue(composition.apply(1.0) < 0, "f(g(1)) = f(-2) — экстраполяция влево → результат < 0 GOOD");
        assertTrue(composition.apply(2.0) < 0, "f(g(2)) = f(-1) — экстраполяция влево → результат < 0 GOOD");

        assertEquals(0.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(0) = 0 GOOD");
        assertEquals(1.0, composition.apply(4.0), 1e-8, "f(g(4)) = f(1) = 1 GOOD");
        assertEquals(1.4142, composition.apply(5.0), 0.1, "f(g(5)) = f(2) = √2 ≈ 1.4142 GOOD");
        assertEquals(1.7321, composition.apply(6.0), 0.1, "f(g(6)) = f(3) = √3 ≈ 1.7321 GOOD");

        assertEquals(2.236, composition.apply(8.0), 0.1, "f(g(8)) = f(5) — экстраполяция вправо → √5 ≈ 2.236 GOOD");
    }

    @Test
    @DisplayName("floorIndexOfX покрывает все ВОЗМОЖНЫЕ ветки условия в цикле")
    void floorIndexOfXCoversAllPossibleBranches() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 4.0, 9.0, 16.0}
        );

        //true && true - x между элементами
        assertEquals(0, f.floorIndexOfX(1.5), "x между 1.0 и 2.0 → true&&true GOOD");
        assertEquals(1, f.floorIndexOfX(2.5), "x между 2.0 и 3.0 → true&&true GOOD");
        assertEquals(2, f.floorIndexOfX(3.5), "x между 3.0 и 4.0 → true&&true GOOD");

        //true && false - x равен следующему элементу
        //x=2.0: i=0: 1.0<=2.0(true) && 2.0<2.0(false) → false, i=1: 2.0<=2.0(true) && 2.0<3.0(true) → true
        assertEquals(1, f.floorIndexOfX(2.0), "x=2.0: проходит две итерации GOOD");
        assertEquals(2, f.floorIndexOfX(3.0), "x=3.0: проходит итерации GOOD");

        //x равен последнему элементу - все итерации false, возврат count-1
        assertEquals(3, f.floorIndexOfX(4.0), "x=4.0: все итерации false → count-1 GOOD");
    }

    @Test
    void testIteratorWithWhileLoop() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        int index = 0;

        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(xValues[index], point.x, 1e-10);
            assertEquals(yValues[index], point.y, 1e-10);
            index++;
        }

        assertEquals(xValues.length, index);
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testIteratorWithForEachLoop() {
        double[] xValues = {10.0, 20.0, 30.0};
        double[] yValues = {100.0, 200.0, 300.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        int index = 0;
        for (Point point : function) {
            assertEquals(xValues[index], point.x, 1e-10);
            assertEquals(yValues[index], point.y, 1e-10);
            index++;
        }

        assertEquals(xValues.length, index);
    }

    @Test
    void testIteratorEmptyFunction() {
        double[] xValues = {1,2};
        double[] yValues = {1,2};

        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        function.remove(0);
        function.remove(0);

        // Должен сразу вернуть false
        assertFalse(iterator.hasNext());

        // Должен сразу бросить исключение
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testIteratorSingleElement() {
        double[] xValues = {5.5, 6.6};
        double[] yValues = {7.7, 8.8};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        function.remove(1);
        Iterator<Point> iterator = function.iterator();

        assertTrue(iterator.hasNext());
        Point point = iterator.next();
        assertEquals(5.5, point.x, 1e-10);
        assertEquals(7.7, point.y, 1e-10);

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }
    @Test
    void testConstructor_XArrayTooShort_ThrowsException() {

        double[] xValues = {1.0};
        double[] yValues = {10.0};

        assertThrows(IllegalArgumentException.class, ()->new ArrayTabulatedFunction(xValues, yValues));
    }

    @Test
    @DisplayName("Вставка точки в начало таблицы")
    public void testInsertAtBeginning() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(0.5, 5.0);

        assertEquals(4, function.getCount(), "Количество точек должно увеличиться на 1");
        assertEquals(0.5, function.getX(0), 1e-10, "Новая точка должна быть на позиции 0");
        assertEquals(5.0, function.getY(0), 1e-10, "Y значение должно соответствовать вставленному");
        assertEquals(1.0, function.getX(1), 1e-10, "Первая исходная точка должна сместиться на позицию 1");
        assertEquals(10.0, function.getY(1), 1e-10, "Y значение первой точки должно сохраниться");
    }

    @Test
    @DisplayName("Вставка точки в середину таблицы")
    public void testInsertInMiddle() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {10.0, 30.0, 50.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 20.0);

        assertEquals(4, function.getCount(), "Количество точек должно увеличиться на 1");
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 5.0},
                function.getxVal(), 1e-10, "X значения должны быть упорядочены");
        assertEquals(20.0, function.getY(1), 1e-10, "Y значение вставленной точки должно быть корректным");
    }

    @Test
    @DisplayName("Вставка точки в конец таблицы")
    public void testInsertAtEnd() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(4.0, 40.0);

        assertEquals(4, function.getCount(), "Количество точек должно увеличиться на 1");
        assertEquals(4.0, function.getX(3), 1e-10, "Новая точка должна быть на последней позиции");
        assertEquals(40.0, function.getY(3), 1e-10, "Y значение последней точки должно быть корректным");
        assertEquals(3.0, function.getX(2), 1e-10, "Предпоследняя точка должна сохранить свое положение");
    }

    @Test
    @DisplayName("Вставка точки с существующим X (замена Y)")
    public void testInsertExistingX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 25.0);

        assertEquals(3, function.getCount(), "Количество точек не должно измениться при замене");
        assertEquals(2.0, function.getX(1), 1e-10, "X значение должно остаться прежним");
        assertEquals(25.0, function.getY(1), 1e-10, "Y значение должно быть заменено");
        assertNotEquals(20.0, function.getY(1), 1e-10, "Старое Y значение должно быть заменено");
    }

    @Test
    @DisplayName("Вставка точки в пустую таблицу")
    public void testInsertIntoEmptyFunction() {
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(
                x -> x * x, 1.0, 3.0, 3
        );

        while (function.getCount() > 0) {
            function.remove(0);
        }

        function.insert(0.5, 0.25);

        assertEquals(1, function.getCount(), "Таблица должна содержать 1 точку");
        assertEquals(0.5, function.getX(0), 1e-10, "X значение должно быть корректным");
        assertEquals(0.25, function.getY(0), 1e-10, "Y значение должно быть корректным");
    }

    @Test
    @DisplayName("Вставка нескольких точек с сохранением порядка")
    public void testInsertMaintainsOrder() {
        double[] xValues = {1.0, 4.0};
        double[] yValues = {1.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(2.0, 2.0);
        function.insert(3.0, 3.0);
        function.insert(0.5, 0.5);

        assertEquals(5, function.getCount(), "Должно быть 5 точек после трех вставок");

        for (int i = 0; i < function.getCount() - 1; i++) {
            assertTrue(function.getX(i) < function.getX(i + 1),
                    "Порядок X значений должен сохраняться: " + function.getX(i) + " < " + function.getX(i + 1));
        }
    }

    @Test
    @DisplayName("Вставка точки с отрицательным X")
    public void testInsertNegativeX() {
        double[] xValues = {-2.0, 0.0, 2.0};
        double[] yValues = {4.0, 0.0, 4.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(-1.0, 1.0);
        function.insert(-3.0, 9.0);

        assertEquals(5, function.getCount(), "Должно быть 5 точек");
        assertArrayEquals(new double[]{-3.0, -2.0, -1.0, 0.0, 2.0},
                function.getxVal(), 1e-10, "Отрицательные X должны корректно сортироваться");
    }

    @Test
    @DisplayName("Вставка точки с нулевым X")
    public void testInsertZeroX() {
        double[] xValues = {-1.0, 1.0};
        double[] yValues = {1.0, 1.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(0.0, 0.0);

        assertEquals(3, function.getCount(), "Должно быть 3 точки");
        assertEquals(0.0, function.getX(1), 1e-10, "Ноль должен быть в середине");
        assertEquals(0.0, function.getY(1), 1e-10, "Y значение нулевой точки должно быть корректным");
    }

    @Test
    @DisplayName("Вставка точки с одинаковым Y но разным X")
    public void testInsertSameYDifferentX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {5.0, 5.0, 5.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.insert(1.5, 5.0);
        function.insert(2.5, 5.0);

        assertEquals(5, function.getCount(), "Должно быть 5 точек");
        for (int i = 0; i < function.getCount(); i++) {
            assertEquals(5.0, function.getY(i), 1e-10,
                    "Все Y значения должны остаться равными 5.0");
        }
    }

}
