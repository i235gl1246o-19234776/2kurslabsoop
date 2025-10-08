package functions;

import exception.InterpolationException;
import exception.DifferentLengthOfArraysException;
import exception.ArrayIsNotSortedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.NoSuchElementException;


@DisplayName("Тесты для LinkedListTabulatedFunction")
class LinkedListTabulatedFunctionTest {

    private static final double delta = 1e-4;

    @Test
    @DisplayName("Тест на проверку с заданными значениями")
    void testConstructorWithArrays() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};

        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(4, function.getCount(), "Количество точек = 4, GOOD");
        assertEquals(1.0, function.leftBound(), delta, "Левая граница = 1.0, GOOD");
        assertEquals(4.0, function.rightBound(), delta, "Правая граница = 4.0, GOOD");

        //Проверка значений
        assertEquals(1.0, function.getX(0), delta, "x[0] = 1.0, GOOD");
        assertEquals(2.0, function.getX(1), delta, "x[1] = 2.0, GOOD");
        assertEquals(3.0, function.getX(2), delta, "x[2] = 3.0, GOOD");
        assertEquals(4.0, function.getX(3), delta, "x[3] = 4.0, GOOD");

        assertEquals(10.0, function.getY(0), delta, "y[0] = 10.0, GOOD");
        assertEquals(20.0, function.getY(1), delta, "y[1] = 20.0, GOOD");
        assertEquals(30.0, function.getY(2), delta, "y[2] = 30.0, GOOD");
        assertEquals(40.0, function.getY(3), delta, "y[3] = 40.0, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку с заданной функцией")
    void testConstructorWithFunction() {
        MathFunction sqr1 = new SqrFunction();
        LinkedListTabulatedFunction function1 = new LinkedListTabulatedFunction(sqr1, 0.0, 4.0, 5);

        MathFunction sqr2 = new SqrFunction();
        LinkedListTabulatedFunction function2 = new LinkedListTabulatedFunction(sqr2, 4.0, 0.0, 5);

        assertEquals(5, function1.getCount(), "Количество точек = 5, GOOD");
        assertEquals(0.0, function1.leftBound(), delta, "Левая граница = 0.0, GOOD");
        assertEquals(4.0, function1.rightBound(), delta, "Правая граница = 4.0, GOOD");

        assertEquals(0.0, function1.getX(0), delta, "x[0], GOOD");
        assertEquals(1.0, function1.getX(1), delta, "x[1], GOOD");
        assertEquals(2.0, function1.getX(2), delta, "x[2], GOOD");
        assertEquals(3.0, function1.getX(3), delta, "x[3], GOOD");
        assertEquals(4.0, function1.getX(4), delta, "x[4], GOOD");

        assertEquals(0.0, function1.getY(0), delta, "y[0], GOOD");
        assertEquals(1.0, function1.getY(1), delta, "y[1], GOOD");
        assertEquals(4.0, function1.getY(2), delta, "y[2], GOOD");
        assertEquals(9.0, function1.getY(3), delta, "y[3], GOOD");
        assertEquals(16.0, function1.getY(4), delta, "y[4], GOOD");

        assertEquals(5, function2.getCount(), "Количество точек = 5, GOOD");
        assertEquals(0.0, function2.leftBound(), delta, "Левая граница = 0.0, GOOD");
        assertEquals(4.0, function2.rightBound(), delta, "Правая граница = 4.0, GOOD");

        assertEquals(0.0, function2.getX(0), delta, "x[0], GOOD");
        assertEquals(1.0, function2.getX(1), delta, "x[1], GOOD");
        assertEquals(2.0, function2.getX(2), delta, "x[2], GOOD");
        assertEquals(3.0, function2.getX(3), delta, "x[3], GOOD");
        assertEquals(4.0, function2.getX(4), delta, "x[4], GOOD");

        assertEquals(0.0, function2.getY(0), delta, "y[0], GOOD");
        assertEquals(1.0, function2.getY(1), delta, "y[1], GOOD");
        assertEquals(4.0, function2.getY(2), delta, "y[2], GOOD");
        assertEquals(9.0, function2.getY(3), delta, "y[3], GOOD");
        assertEquals(16.0, function2.getY(4), delta, "y[4], GOOD");
    }


    @Test
    @DisplayName("Тест получения узлов")
    void testGetNode() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // Проверка getNode через публичные методы
        assertEquals(1.0, function.getX(0), delta, "getNode(0).x, GOOD");
        assertEquals(10.0, function.getY(0), delta, "getNode(0).y, GOOD");

        assertEquals(2.0, function.getX(1), delta, "getNode(1).x, GOOD");
        assertEquals(20.0, function.getY(1), delta, "getNode(1).y, GOOD");

        assertEquals(3.0, function.getX(2), delta, "getNode(2).x, GOOD");
        assertEquals(30.0, function.getY(2), delta, "getNode(2).y, GOOD");

        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(-1), "Отрицательный индекс, GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(3), "Индекс за границами, GOOD");
    }

    @Test
    @DisplayName("Тест установки значений Y")
    void testSetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.setY(1, 25.0);
        assertEquals(25.0, function.getY(1), delta, "setY должен изменить значение, GOOD");

        function.setY(2, 35.0);
        assertEquals(35.0, function.getY(2), delta, "setY должен изменить значение, GOOD");
    }

    @Test
    @DisplayName("Тест поиска индекса по X")
    void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfX(1.0), "indexOfX(1.0), GOOD");
        assertEquals(1, function.indexOfX(2.0), "indexOfX(2.0), GOOD");
        assertEquals(2, function.indexOfX(3.0), "indexOfX(3.0), GOOD");
        assertEquals(3, function.indexOfX(4.0), "indexOfX(4.0), GOOD");

        assertEquals(-1, function.indexOfX(0.0), "indexOfX(0.0) не найден, GOOD");
        assertEquals(-1, function.indexOfX(2.5), "indexOfX(2.5) не найден, GOOD");
    }

    @Test
    @DisplayName("Тест поиска нижнего индекса по X")
    void testFloorNodeOfX() {
        double[] xValues = {1.0, 3.0, 5.0, 7.0};
        double[] yValues = {10.0, 30.0, 50.0, 70.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> function.floorIndexOfX(0.5), "floorIndexOfX(0.5), GOOD");
        assertEquals(0, function.floorIndexOfX(2.0), "floorIndexOfX(2.0), GOOD");
        assertEquals(1, function.floorIndexOfX(4.0), "floorIndexOfX(4.0), GOOD");
        assertEquals(2, function.floorIndexOfX(6.0), "floorIndexOfX(6.0), GOOD");
        assertEquals(3, function.floorIndexOfX(7.0), "floorIndexOfX(7.0), GOOD");
        assertThrows(IllegalArgumentException.class, () -> function.floorIndexOfX(8.0), "floorIndexOfX(8.0), GOOD");
    }

    @Test
    @DisplayName("Тест оптимизированного применения функции")
    void testOptimizedApply() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        //Точные значения
        assertEquals(10.0, function.apply(1.0), delta, "apply(1.0), GOOD");
        assertEquals(20.0, function.apply(2.0), delta, "apply(2.0), GOOD");
        assertEquals(30.0, function.apply(3.0), delta, "apply(3.0), GOOD");
        assertEquals(40.0, function.apply(4.0), delta, "apply(4.0), GOOD");

        // Интерполяция
        assertEquals(15.0, function.apply(1.5), delta, "apply(1.5) интерполяция, GOOD");
        assertEquals(25.0, function.apply(2.5), delta, "apply(2.5) интерполяция, GOOD");
        assertEquals(35.0, function.apply(3.5), delta, "apply(3.5) интерполяция, GOOD");

        // Экстраполяция
        assertEquals(0.0, function.apply(0.0), delta, "apply(0.0) экстраполяция, GOOD");
        assertEquals(45.0, function.apply(4.5), delta, "apply(4.5) экстраполяция, GOOD");
    }

    @Test
    @DisplayName("Тест структуры циклического списка")
    void testCircularListStructure() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.getX(0), delta, "Первый элемент, GOOD");
        assertEquals(3.0, function.getX(2), delta, "Последний элемент, GOOD");

        assertEquals(0, function.indexOfX(1.0), "indexOfX для первого элемента, GOOD");
        assertEquals(2, function.indexOfX(3.0), "indexOfX для последнего элемента, GOOD");

        assertEquals(1.0, function.getX(0), delta, "Индекс 0, GOOD");
        assertEquals(2.0, function.getX(1), delta, "Индекс 1, GOOD");
        assertEquals(3.0, function.getX(2), delta, "Индекс 2, GOOD");

        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(3),
                "Индекс 3 должен вызывать исключение, GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(6),
                "Индекс 6 должен вызывать исключение, GOOD");
    }

    @Test
    @DisplayName("Тест функции с одной точкой")
    void testSinglePointFunction() {
        MathFunction constant = new ConstantFunction(5.0);
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(constant, 2.0, 2.0, 3);

        assertEquals(3, function.getCount(), "Количество точек должно быть 3, GOOD");
        assertEquals(2.0, function.leftBound(), delta, "Левая граница, GOOD");
        assertEquals(2.0, function.rightBound(), delta, "Правая граница, GOOD");

        assertEquals(2.0, function.getX(0), delta, "x[0] для одной точки, GOOD");
        assertEquals(2.0, function.getX(1), delta, "x[1] для одной точки, GOOD");
        assertEquals(2.0, function.getX(2), delta, "x[2] для одной точки, GOOD");

        assertEquals(5.0, function.getY(0), delta, "y[0] для одной точки, GOOD");
    }

    @Test
    @DisplayName("Тест вставки в пустой список")
    public void testInsertEmptyList() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        list.remove(0);
        list.remove(0);
        assertEquals(0, list.getCount(), "GOOD");

        list.insert(1.0, 2.0);
        assertEquals(1, list.getCount(), "GOOD");
        assertEquals(1.0, list.getX(0), "GOOD");
        assertEquals(2.0, list.getY(0), "GOOD");
    }

    @Test
    @DisplayName("Тест вставки в начало")
    public void testInsertAtBeginning() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{2.0, 4.0, 6.0}, new double[]{1.0, 2.0, 3.0});
        assertEquals(3, list.getCount(), "GOOD");

        list.insert(1.0, 10.0);
        assertEquals(4, list.getCount(), "GOOD");
        assertEquals(1.0, list.getX(0), "GOOD");
        assertEquals(10.0, list.getY(0), "GOOD");
        assertEquals(2.0, list.getX(1), "Остались в порядке, GOOD");
    }

    @Test
    @DisplayName("Тест вставки в середину")
    public void testInsertInMiddle() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1.0, 3.0, 5.0}, new double[]{1.0, 3.0, 5.0});
        list.insert(2.0, 2.0);
        assertEquals(4, list.getCount(), "GOOD");
        assertEquals(1.0, list.getX(0), "GOOD");
        assertEquals(2.0, list.getX(1), "GOOD");
        assertEquals(3.0, list.getX(2), "GOOD");
        assertEquals(2.0, list.getY(1), "GOOD");
    }

    @Test
    @DisplayName("Тест вставки в конец")
    public void testInsertAtEnd() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 2.0, 3.0});
        list.insert(4.0, 4.0);
        assertEquals(4, list.getCount(), "GOOD");
        assertEquals(4.0, list.getX(3), "GOOD");
        assertEquals(4.0, list.getY(3), "GOOD");
    }

    @Test
    @DisplayName("Тест вставки с дубликатом X")
    public void testInsertDuplicateX() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 2.0, 3.0});
        list.insert(2.0, 99.0); // замена
        assertEquals(3, list.getCount(), "GOOD");
        assertEquals(99.0, list.getY(1), "y обновился, GOOD");
    }

    @Test
    @DisplayName("Тест множественной вставки")
    public void testInsertMultipleTimes() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1, 2}, new double[]{1, 2});
        list.remove(0);
        list.remove(0);
        list.insert(5.0, 5.0);
        list.insert(3.0, 3.0);
        list.insert(7.0, 7.0);
        list.insert(4.0, 4.0);

        assertEquals(4, list.getCount(), "GOOD");
        assertEquals(3.0, list.getX(0), "GOOD");
        assertEquals(4.0, list.getX(1), "GOOD");
        assertEquals(5.0, list.getX(2), "GOOD");
        assertEquals(7.0, list.getX(3), "GOOD");
    }

    @Test
    @DisplayName("Тест вставки с малым эпсилон")
    public void testInsertWithSmallEpsilon() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1.0, 3.0}, new double[]{1.0, 3.0});
        list.insert(1.0 + 1e-11, 99.0); //Почти совпадает — должно заменить
        assertEquals(2, list.getCount(), "GOOD");
        assertEquals(99.0, list.getY(0), "заменило первый элемент, GOOD");
    }

    @Test
    @DisplayName("Тест удаления единственного элемента")
    public void testRemoveSingleElement() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{5.0, 9}, new double[]{10.0, 7});
        list.remove(0);
        assertEquals(1, list.getCount(), "GOOD");
        assertEquals(9.0, list.getX(0), "GOOD");
        assertEquals(7.0, list.getY(0), "GOOD");

        list.remove(0);
        assertEquals(0, list.getCount(), "GOOD");
        assertNull(list.getHead(), "GOOD");
    }

    @Test
    @DisplayName("Тест удаления первого элемента")
    public void testRemoveFirstElement() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{10.0, 20.0, 30.0}
        );

        assertEquals(3, list.getCount(), "GOOD");
        assertEquals(1.0, list.getX(0), "GOOD");
        assertEquals(2.0, list.getX(1), "GOOD");
        assertEquals(3.0, list.getX(2), "GOOD");

        list.remove(0);

        assertEquals(2, list.getCount(), "GOOD");
        assertEquals(2.0, list.getX(0), "теперь второй стал первым, GOOD");
        assertEquals(3.0, list.getX(1), "GOOD");
        assertEquals(20.0, list.getY(0), "GOOD");
        assertEquals(30.0, list.getY(1), "GOOD");

        assertEquals(2.0, list.leftBound(), "правильные границы, GOOD");
        assertEquals(3.0, list.rightBound(), "GOOD");
    }

    @Test
    @DisplayName("Тест удаления среднего элемента")
    public void testRemoveMiddleElement() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 2.0, 3.0, 4.0}
        );

        list.remove(2); //Удаляем x=3.0

        assertEquals(3, list.getCount(), "GOOD");
        assertEquals(1.0, list.getX(0), "GOOD");
        assertEquals(2.0, list.getX(1), "GOOD");
        assertEquals(4.0, list.getX(2), "GOOD");
        assertEquals(1.0, list.getY(0), "GOOD");
        assertEquals(2.0, list.getY(1), "GOOD");
        assertEquals(4.0, list.getY(2), "GOOD");

        assertEquals(1.0, list.leftBound(), "GOOD");
        assertEquals(4.0, list.rightBound(), "GOOD");
    }

    @Test
    @DisplayName("Тест удаления последнего элемента")
    public void testRemoveLastElement() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 2.0, 3.0}
        );

        list.remove(2); //Удаляем последний

        assertEquals(2, list.getCount(), "GOOD");
        assertEquals(1.0, list.getX(0), "GOOD");
        assertEquals(2.0, list.getX(1), "GOOD");
        assertEquals(1.0, list.leftBound(), "GOOD");
        assertEquals(2.0, list.rightBound(), "GOOD");

        //Проверим, что apply() продолжает работать
        assertEquals(1.5, list.apply(1.5), 1e-10, "интерполяция между 1.0 и 2.0, GOOD");
    }

    @Test
    @DisplayName("Тест удаления всех элементов по одному")
    public void testRemoveAllElementsOneByOne() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0, 4.0},
                new double[]{1.0, 2.0, 3.0, 4.0}
        );

        for (int i = 0; i < 4; i++) {
            list.remove(0); //Всегда удаляем первый
            assertEquals(3 - i, list.getCount(), "GOOD");
        }

        assertNull(list.getHead(), "GOOD");
        assertEquals(0, list.getCount(), "GOOD");
    }

    @Test
    @DisplayName("Тест удаления после вставки")
    public void testRemoveAfterInsert() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1, 2}, new double[]{1, 2});


        list.insert(1.0, 10.0);
        list.insert(3.0, 30.0);
        list.insert(2.0, 20.0); //Вставится между 1.0 и 3.0 → [1.0, 2.0, 3.0]

        list.remove(1); //Удаляем 2.0

        assertEquals(2, list.getCount(), "GOOD");
        assertEquals(1.0, list.getX(0), "GOOD");
        assertEquals(3.0, list.getX(1), "GOOD");
        assertEquals(10.0, list.getY(0), "GOOD");
        assertEquals(30.0, list.getY(1), "GOOD");
    }

    @Test
    @DisplayName("Тест удаления за границами")
    public void testRemoveOutOfBounds() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1.0, 2}, new double[]{1.0, 2});

        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1), "GOOD");
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(2), "GOOD");

        list.remove(0); //Удалили единственный элемент
        //assertTrue(assertThrows(IllegalStateException.class, () -> list.remove(0)).getMessage().contains("Список пуст, ы"));
    }

    @Test
    @DisplayName("Тест удаления и проверки apply")
    public void testRemoveAndCheckApply() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(
                new double[]{0.0, 1.0, 2.0, 3.0},
                new double[]{0.0, 1.0, 4.0, 9.0} // x^2
        );

        list.remove(1); //Удаляем x=1.0 → остаются [0.0, 2.0, 3.0]

        assertEquals(3.0, list.apply(1.5), 1e-10, "GOOD");
        assertEquals(1.0, list.apply(0.5), 1e-10, "GOOD");
        assertEquals(6.5, list.apply(2.5), 1e-10, "GOOD");
    }

    @Test
    @DisplayName("Тест поиска нижнего индекса в пустом списке")
    public void testFloorIndexOfX_EmptyList() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1, 2}, new double[]{1, 2});
        list.remove(0);
        list.remove(0);
        assertThrows(IllegalArgumentException.class, () -> list.floorIndexOfX(1.0), "Пустой список должен бросать исключение, GOOD");
    }

    @Test
    @DisplayName("Тест экстраполяции")
    public void testExtrapolateLeft_CountOne() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{5.0, 8}, new double[]{10.0, 16});
        assertEquals(6.0, list.extrapolateLeft(3.0), delta, "Экстраполяция слева для одной точки, GOOD");
        assertEquals(20.0, list.extrapolateRight(10.0), delta, "Экстраполяция справа для одной точки, GOOD");
    }

    @Test
    @DisplayName("Тест интерполяции для одной точки")
    public void testInterpolate_CountOne() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{5.0, 10}, new double[]{10.0, 20});
        list.remove(0);
        assertThrows(InterpolationException.class, () -> list.interpolate(5.0, 0), "Интерполяция для одной точки, GOOD");
    }

    @Test
    @DisplayName("Тест apply с точным совпадением на head")
    public void testApply_ExactMatchOnHead() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {10.0, 30.0, 50.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(10.0, list.apply(1.0), delta, "apply(1.0) — точное совпадение с head, GOOD");
    }

    @Test
    @DisplayName("Тест apply с точным совпадением на последнем элементе")
    public void testApply_ExactMatchOnLast() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {10.0, 30.0, 50.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(50.0, list.apply(5.0), delta, "apply(5.0) — точное совпадение с последним, GOOD");
    }

    @Test
    @DisplayName("Тест поиска узла с точным совпадением на head")
    public void testFloorNodeOfX_ExactMatchOnHead() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {10.0, 30.0, 50.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);

        LinkedListTabulatedFunction.Node node = list.floorNodeOfX(1.0);
        assertEquals(1.0, node.x, delta, "floorNodeOfX(1.0) должен вернуть head, GOOD");
    }

    @Test
    @DisplayName("Тест поиска узла с точным совпадением на последнем элементе")
    public void testFloorNodeOfX_ExactMatchOnLast() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {10.0, 30.0, 50.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);

        LinkedListTabulatedFunction.Node node = list.floorNodeOfX(5.0);
        assertEquals(5.0, node.x, delta, "floorNodeOfX(5.0) должен вернуть последний узел, GOOD");
    }

    @Test
    @DisplayName("Тест поиска узла при X больше или равном последнему")
    public void testFloorNodeOfX_XGreaterThanOrEqualToLast() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {10.0, 30.0, 50.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> list.floorNodeOfX(6.0), "floorNodeOfX(6.0) должен вернуть последний узел, GOOD");
    }

    @Test
    @DisplayName("Тест поиска узла при X меньше первого")
    public void testFloorNodeOfX_XLessThanFirst() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {10.0, 30.0, 50.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> list.floorNodeOfX(0.5), "floorNodeOfX(0.5) должен вернуть head, GOOD");
    }

    @Test
    @DisplayName("Тест вставки в конец с обновлением циклической структуры")
    public void testInsert_InsertAtEnd_CircularUpdate() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{10.0, 20.0});
        list.insert(3.0, 30.0);

        //Проверим, что голова не сдвинулась, а хвост правильно связан
        assertEquals(3.0, list.getX(2), "GOOD");
        assertEquals(1.0, list.getX(0), "head не изменился, GOOD");
        assertEquals(2.0, list.getX(1), "GOOD");
        assertEquals(3.0, list.rightBound(), "GOOD");
    }

    @Test
    @DisplayName("Тест вставки дубликата с эпсилон")
    public void testInsert_InsertDuplicateWithEpsilon() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1.0, 2}, new double[]{10.0, 5});
        list.remove(0);
        list.insert(1.0 + 1e-9, 99.0); // должно заменить, т.к. |1.0 - 1.000000001| < 1e-10?

        assertEquals(2, list.getCount(), "1e-9 > 1e-10 — должно быть добавлено как новая точка, GOOD");
        assertEquals(1.0, list.getX(0), delta, "GOOD");
        assertEquals(99.0, list.getY(0), "GOOD");
        assertEquals(2, list.getX(1), delta, "GOOD");
        assertEquals(5.0, list.getY(1), "GOOD");
    }

    @Test
    @DisplayName("Тест вставки дубликата в пределах эпсилон")
    public void testInsert_InsertDuplicateWithinEpsilon() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1.0, 2}, new double[]{10.0, 2});
        list.remove(1);
        list.insert(1.0 + 1e-11, 99.0); // |1.0 - 1.00000000001| < 1e-10 → true

        assertEquals(1, list.getCount(), "Должно заменить существующий элемент, GOOD");
        assertEquals(99.0, list.getY(0), delta, "GOOD");
    }

    @Test
    @DisplayName("Тест поиска индекса по Y (найден)")
    public void testIndexOfY_Found() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(1, list.indexOfY(20.0), "indexOfY(20.0) должен найти индекс 1, GOOD");
    }

    @Test
    @DisplayName("Тест поиска индекса по Y (не найден)")
    public void testIndexOfY_NotFound() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(-1, list.indexOfY(25.0), "indexOfY(25.0) не найден, GOOD");
    }

    @Test
    @DisplayName("Тест поиска индекса по Y с эпсилон")
    public void testIndexOfY_EpsilonMatch() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(1, list.indexOfY(20.0 + 1e-11), "indexOfY с погрешностью 1e-11 должен найти, GOOD");
        assertEquals(-1, list.indexOfY(20.0 + 1e-9), "indexOfY с погрешностью 1e-9 не должен найти (больше 1e-10), GOOD");
    }

    @Test
    @DisplayName("Тест левой границы пустого списка")
    public void testLeftBound_EmptyList() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{-1, 6}, new double[]{1, 6});
        list.remove(0);
        list.remove(0);
        assertThrows(IllegalArgumentException.class, () -> list.leftBound(), "Пустой список при leftBound(), GOOD");
    }

    @Test
    @DisplayName("Тест правой границы пустого списка")
    public void testRightBound_EmptyList() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1, 2}, new double[]{1, 2});
        list.remove(0);
        list.remove(0);
        assertThrows(IllegalArgumentException.class, () -> list.rightBound(), "Пустой список при rightBound(), GOOD");
    }

    @Test
    @DisplayName("Тест конструктора с массивами разной длины")
    public void testConstructor_ArraysDifferentLength() {
        assertThrows(DifferentLengthOfArraysException.class, () ->
                new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{10.0}), "Разные длины массивов, GOOD");
    }

    @Test
    @DisplayName("Тест конструктора с не строго возрастающими X")
    public void testConstructor_XNotStrictlyIncreasing() {
        assertThrows(ArrayIsNotSortedException.class, () ->
                new LinkedListTabulatedFunction(new double[]{1.0, 1.0, 2.0}, new double[]{10.0, 20.0, 30.0}), "Не строго возрастающие x, GOOD");
        assertThrows(ArrayIsNotSortedException.class, () ->
                new LinkedListTabulatedFunction(new double[]{2.0, 1.0}, new double[]{10.0, 20.0}), "Убывающие x, GOOD");
    }
/*
    @Test
    @DisplayName("Тест apply с экстраполяцией слева для одной точки")
    public void testApply_ExtrapolateLeft_CountOne() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{5.0, 6}, new double[]{10.0, 12});
        list.remove(1);
        assertEquals(8, list.apply(4.0), "apply(4.0) при одной точке — экстраполяция слева, GOOD");
    }

    @Test
    @DisplayName("Тест apply с экстраполяцией справа для одной точки")
    public void testApply_ExtrapolateRight_CountOne() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{5.0, 6}, new double[]{10.0, 12});
        list.remove(1);
        assertEquals(10.0, list.apply(6.0), delta, "apply(6.0) при одной точке — экстраполяция справа, GOOD");
    }*/

    @Test
    @DisplayName("Тест apply с интерполяцией на границе")
    public void testApply_Interpolate_BoundaryCase() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);

        //Проверим границу: x = 1.0 (head) и x = 2.0 (last)
        assertEquals(10.0, list.apply(1.0), delta, "GOOD");
        assertEquals(20.0, list.apply(2.0), delta, "GOOD");

        //Проверим интерполяцию на границе между ними
        assertEquals(15.0, list.apply(1.5), delta, "GOOD");
    }

    @Test
    @DisplayName("Тест обновления head после удаления")
    public void testRemove_HeadIsUpdatedAfterRemoval() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{10.0, 20.0, 30.0}
        );

        list.remove(0); //Удаляем head

        assertNotNull(list.getHead(), "GOOD");
        assertEquals(2.0, list.getHead().x, delta, "После удаления head, новый head должен быть 2.0, GOOD");
    }

    @Test
    @DisplayName("Тест вставки перед head в циклическом списке")
    public void testInsert_InsertAfterHeadInCircular() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1.0, 4}, new double[]{10.0, 4});
        list.remove(0);
        list.insert(2.0, 20.0);
        list.insert(0.5, 5.0); //Вставить перед head

        assertEquals(3, list.getCount(), "GOOD");
        assertEquals(0.5, list.getX(0), "GOOD");
        assertEquals(2.0, list.getX(1), "GOOD");
        assertEquals(4.0, list.getX(2), "GOOD");

        assertEquals(5.0, list.getY(0), "GOOD");
        assertEquals(20.0, list.getY(1), "GOOD");
        //assertEquals(20.0, list.getY(2), "GOOD");
    }

    @Test
    @DisplayName("Тест apply с одной точкой и эпсилон")
    public void testApply_WithSingleNodeAndEpsilon() {
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(new double[]{1.0, 2}, new double[]{10.0, 20});
        assertEquals(10.0, list.apply(1.0 + 1e-11), delta, "apply(1.0 + 1e-11) — должно вернуть 10.0 (точное совпадение по эпсилон), GOOD");
    }

    @Test
    @DisplayName("Тест композиции LinkedList с LinkedList")
    void testLinkedListWithLinkedList() {
        //f(x) = x + 1
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = 2x + 3
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {3.0, 5.0, 7.0, 9.0, 11.0};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        CompositeFunction composition = new CompositeFunction(f, g);
        // f(g(x)) = (2x + 3) + 1 = 2x + 4
        assertEquals(4.0, composition.apply(0.0), 1e-8, "f(g(0)) = f(3) = 4, GOOD");
        assertEquals(6.0, composition.apply(1.0), 1e-8, "f(g(1)) = f(5) = 6, GOOD");
        assertEquals(8.0, composition.apply(2.0), 1e-8, "f(g(2)) = f(7) = 8, GOOD");
        assertEquals(10.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(9) = 10, GOOD");

        assertEquals(4.5, composition.apply(0.25), 1e-8, "f(g(0.5)) интерполяция, GOOD");
        assertEquals(5.0, composition.apply(0.5), 1e-8, "f(g(3.5)) интерполяций, GOOD");
        assertEquals(5.5, composition.apply(0.75), 1e-8, "f(g(3.5)) интерполяций, GOOD");
        assertEquals(6.5, composition.apply(1.25), 1e-8, "f(g(3.5)) интерполяций, GOOD");
        assertEquals(7.0, composition.apply(1.5), 1e-8, "f(g(3.5)) интерполяций, GOOD");
    }

    @Test
    @DisplayName("Тест andThen с LinkedList")
    void testLinkedListWithLinkedListAndThen() {
        //f(x) = x + 1
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = 2x + 3
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {3.0, 5.0, 7.0, 9.0, 11.0};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        MathFunction composition = f.andThen(g);
        // f(g(x)) = (2x + 3) + 1 = 2x + 4
        assertEquals(4.0, composition.apply(0.0), 1e-8, "f(g(0)) = f(3) = 4, GOOD");
        assertEquals(6.0, composition.apply(1.0), 1e-8, "f(g(1)) = f(5) = 6, GOOD");
        assertEquals(8.0, composition.apply(2.0), 1e-8, "f(g(2)) = f(7) = 8, GOOD");
        assertEquals(10.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(9) = 10, GOOD");

        assertEquals(4.5, composition.apply(0.25), 1e-8, "f(g(0.5)) интерполяция, GOOD");
        assertEquals(5.0, composition.apply(0.5), 1e-8, "f(g(3.5)) интерполяций, GOOD");
        assertEquals(5.5, composition.apply(0.75), 1e-8, "f(g(3.5)) интерполяций, GOOD");
        assertEquals(6.5, composition.apply(1.25), 1e-8, "f(g(3.5)) интерполяций, GOOD");
        assertEquals(7.0, composition.apply(1.5), 1e-8, "f(g(3.5)) интерполяций, GOOD");
    }

    @Test
    @DisplayName("Тест экспоненциально-логарифмической композиции")
    void testLinkedListWithLinkedListExponentialWithLogarithmicComposition() {
        //f(x) = x + 1
        double[] x1 = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] y1 = {1.0, 1.5, 2.0, 2.5, 3.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = ln(x + 1)
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {0.0, 0.6931, 1.0986, 1.3863, 1.6094};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = ln(x + 1) + 1
        CompositeFunction composition = new CompositeFunction(f, g);

        assertEquals(1.0, composition.apply(0.0), 0.1, "f(g(0)) = f(0) = 1, GOOD");
        assertEquals(1.6931, composition.apply(1.0), 0.1, "f(g(1)) = f(ln2) ≈ 2, GOOD");
        assertEquals(2.0986, composition.apply(2.0), 0.1, "f(g(2)) = f(ln3) ≈ 3, GOOD");
        assertEquals(2.3863, composition.apply(3.0), 0.1, "f(g(3)) = f(ln4) ≈ 4, GOOD");
        assertEquals(2.6094, composition.apply(4.0), 0.1, "f(g(4)) = f(ln5) ≈ 5, GOOD");
    }

    @Test
    @DisplayName("Тест тригонометрической композиции")
    void testLinkedListWithLinkedListTrigonometricComposition() {
        //f(x) = sin(x)
        double[] x1 = {0.0, Math.PI / 6, Math.PI / 4, Math.PI / 3, Math.PI / 2};
        double[] y1 = {0.0, 0.5, 0.7071, 0.8660, 1.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = cos(x)
        double[] x2 = {0.0, Math.PI / 6, Math.PI / 4, Math.PI / 3, Math.PI / 2};
        double[] y2 = {1.0, 0.8660, 0.7071, 0.5, 0.0};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = sin(cos(x))
        CompositeFunction composition = new CompositeFunction(f, g);

        assertEquals(Math.sin(1.0), composition.apply(0.0), 0.1, "sin(cos(0)) = sin(1), GOOD");
        assertEquals(Math.sin(0.8660), composition.apply(Math.PI / 6), 0.1, "sin(cos(π/6)), GOOD");
        assertEquals(Math.sin(0.7071), composition.apply(Math.PI / 4), 0.1, "sin(cos(π/4)), GOOD");
        assertEquals(Math.sin(0.5), composition.apply(Math.PI / 3), 0.1, "sin(cos(π/3)), GOOD");
        assertEquals(Math.sin(0.0), composition.apply(Math.PI / 2), 0.1, "sin(cos(π/2)) = sin(0), GOOD");
    }

    @Test
    @DisplayName("Тест композиции с экстраполяцией")
    void testLinkedListWithLinkedListCompositeWithExtrapolation() {
        //f(x) = √x
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {0.0, 1.0, 1.4142, 1.7321, 2.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = x - 3
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        double[] y2 = {-3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = f(x - 3) = √(x - 3)
        CompositeFunction composition = new CompositeFunction(f, g);
        //Экстраполяция влево (x - 3 < 0)
        //assertThrows(InterpolationException.class,()->CompositeFunction(f, g), "f(g(0)) = f(-3) экстраполяция влево, GOOD");
        //assertTrue(composition.apply(1.0), "f(g(1)) = f(-2) экстраполяция влево, GOOD");
        //assertTrue(composition.apply(2.0), "f(g(2)) = f(-1) экстраполяция влево, GOOD");
        assertEquals(-1, composition.apply(2.0));

        //Граничные значения
        assertEquals(0.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(0) = 0, GOOD");
        assertEquals(1.0, composition.apply(4.0), 1e-8, "f(g(4)) = f(1) = 1, GOOD");
        assertEquals(1.4142, composition.apply(5.0), 0.1, "f(g(5)) = f(2) = √2, GOOD");
        assertEquals(1.7321, composition.apply(6.0), 0.1, "f(g(6)) = f(3) = √3, GOOD");

        //Экстраполяция вправо (x - 3 > 4)
        //assertEquals(2.236, composition.apply(8.0), 0.1, "f(g(8)) = f(5) экстраполяция вправо, GOOD");
    }

    @Test
    @DisplayName("Тест композиции LinkedList с Array")
    void testLinkedListWithArray() {
        //f(x) = x + 1
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = 2x + 3
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {3.0, 5.0, 7.0, 9.0, 11.0};
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(x2, y2);

        CompositeFunction composition = new CompositeFunction(f, g);
        //f(g(x)) = (2x + 3) + 1 = 2x + 4
        //assertThrows(InterpolationException.class,()-> composition.apply(-1.0), "f(g(0)) = f(3) = 4, GOOD");
        assertEquals(6.0, composition.apply(1.0), 1e-8, "f(g(1)) = f(5) = 6, GOOD");
        assertEquals(8.0, composition.apply(2.0), 1e-8, "f(g(2)) = f(7) = 8, GOOD");
        assertEquals(10.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(9) = 10, GOOD");

        assertEquals(4.5, composition.apply(0.25), 1e-8, "f(g(0.5)) интерполяция, GOOD");
        assertEquals(5.0, composition.apply(0.5), 1e-8, "f(g(3.5)) интерполяций, GOOD");
        assertEquals(5.5, composition.apply(0.75), 1e-8, "f(g(3.5)) интерполяций, GOOD");
        assertEquals(6.5, composition.apply(1.25), 1e-8, "f(g(3.5)) интерполяций, GOOD");
        assertEquals(7.0, composition.apply(1.5), 1e-8, "f(g(3.5)) интерполяций, GOOD");
    }

    @Test
    @DisplayName("Тест экспоненциально-логарифмической композиции с Array")
    void testLinkedListWithArrayExponentialWithLogarithmicComposition() {
        //f(x) = x + 1
        double[] x1 = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] y1 = {1.0, 1.5, 2.0, 2.5, 3.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = ln(x + 1)
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {0.0, 0.6931, 1.0986, 1.3863, 1.6094};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = ln(x + 1) + 1
        CompositeFunction composition = new CompositeFunction(f, g);

        assertEquals(1.0, composition.apply(0.0), 0.1, "f(g(0)) = f(0) = 1, GOOD");
        assertEquals(1.6931, composition.apply(1.0), 0.1, "f(g(1)) = f(ln2) ≈ 2, GOOD");
        assertEquals(2.0986, composition.apply(2.0), 0.1, "f(g(2)) = f(ln3) ≈ 3, GOOD");
        assertEquals(2.3863, composition.apply(3.0), 0.1, "f(g(3)) = f(ln4) ≈ 4, GOOD");
        assertEquals(2.6094, composition.apply(4.0), 0.1, "f(g(4)) = f(ln5) ≈ 5, GOOD");
    }

    @Test
    @DisplayName("Тест тригонометрической композиции с Array")
    void testLinkedListWithArrayTrigonometricComposition() {
        //f(x) = sin(x)
        double[] x1 = {0.0, Math.PI / 6, Math.PI / 4, Math.PI / 3, Math.PI / 2};
        double[] y1 = {0.0, 0.5, 0.7071, 0.8660, 1.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = cos(x)
        double[] x2 = {0.0, Math.PI / 6, Math.PI / 4, Math.PI / 3, Math.PI / 2};
        double[] y2 = {1.0, 0.8660, 0.7071, 0.5, 0.0};
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = sin(cos(x))
        CompositeFunction composition = new CompositeFunction(f, g);

        assertEquals(Math.sin(1.0), composition.apply(0.0), 0.1, "sin(cos(0)) = sin(1), GOOD");
        assertEquals(Math.sin(0.8660), composition.apply(Math.PI / 6), 0.1, "sin(cos(π/6)), GOOD");
        assertEquals(Math.sin(0.7071), composition.apply(Math.PI / 4), 0.1, "sin(cos(π/4)), GOOD");
        assertEquals(Math.sin(0.5), composition.apply(Math.PI / 3), 0.1, "sin(cos(π/3)), GOOD");
        assertEquals(Math.sin(0.0), composition.apply(Math.PI / 2), 0.1, "sin(cos(π/2)) = sin(0), GOOD");
    }

    @Test
    @DisplayName("Тест композиции с экстраполяцией с Array")
    void testLinkedListWithArrayCompositeWithExtrapolation() {
        //f(x) = √x
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {0.0, 1.0, 1.4142, 1.7321, 2.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = x - 3
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        double[] y2 = {-3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0};
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(x2, y2);

        //h(x) = f(g(x)) = f(x - 3) = √(x - 3)
        CompositeFunction composition = new CompositeFunction(f, g);
        //Экстраполяция влево (x - 3 < 0)
        assertTrue(composition.apply(0.0) < 0, "f(g(0)) = f(-3) экстраполяция влево, GOOD");
        assertTrue(composition.apply(1.0) < 0, "f(g(1)) = f(-2) экстраполяция влево, GOOD");
        assertTrue(composition.apply(2.0) < 0, "f(g(2)) = f(-1) экстраполяция влево, GOOD");

        //Граничные значения
        assertEquals(0.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(0) = 0, GOOD");
        assertEquals(1.0, composition.apply(4.0), 1e-8, "f(g(4)) = f(1) = 1, GOOD");
        assertEquals(1.4142, composition.apply(5.0), 0.1, "f(g(5)) = f(2) = √2, GOOD");
        assertEquals(1.7321, composition.apply(6.0), 0.1, "f(g(6)) = f(3) = √3, GOOD");

        //Экстраполяция вправо (x - 3 > 4)
        assertEquals(2.236, composition.apply(8.0), 0.1, "f(g(8)) = f(5) экстраполяция вправо, GOOD");
    }

    @Test
    @DisplayName("Тестирование случаев, когда не нашелся x или y в пустом списке")
    void testIndexOfXOrYIfHeadEqualsNull() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2},
                new double[]{1, 2}
        );

        assertEquals(-1, function.indexOfX(0.5));
        assertEquals(-1, function.indexOfY(11.0));
        //assertThrows(IllegalArgumentException.class, () -> function.floorNodeOfX(9.0)).getMessage().contains("Список пуст, ы");
        //assertThrows(IllegalArgumentException.class, () -> function.remove(0)).getMessage().contains("Список пуст, ы");
    }

    @Test
    @DisplayName("Итератор должен корректно работать с циклом while")
    void testIteratorWorksCorrectlyWithWhileLoop() {
        double[] xVal = {1.0, 2.0, 3.0};
        double[] yVal = {4.0, 5.0, 6.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xVal, yVal);
        Iterator<Point> iterator = function.iterator();
        int index = 0;
        double[] xValExp = {1.0, 2.0, 3.0};
        double[] yValExp = {4.0, 5.0, 6.0};
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(xValExp[index], point.x, delta, "X координата точки " + index + " должна быть корректной");
            assertEquals(yValExp[index], point.y, delta, "Y координата точки " + index + " должна быть корректной");
            index++;
        }
        assertEquals(index, 3, delta, "Должны быть пройдены все точки с помощью цикла while");
    }

    @Test
    @DisplayName("Итератор должен поддерживать цикл for-each")
    public void testIteratorSupportsForEachLoop() {
        double[] xVal = {1.0, 2.0, 3.0};
        double[] yVal = {4.0, 5.0, 6.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xVal, yVal);
        Iterator<Point> iterator = function.iterator();
        int index = 0;
        double[] xValExp = {1.0, 2.0, 3.0};
        double[] yValExp = {4.0, 5.0, 6.0};

        for (Point point : function) {
            assertEquals(xValExp[index], point.x, delta, "X координата точки " + index + " должна быть корректной");
            assertEquals(yValExp[index], point.y, delta, "Y координата точки " + index + " должна быть корректной");
            index++;
        }

        assertEquals(index, 3, delta, "Должны быть пройдены все точки с помощью цикла for-each");
    }
    @Test
    @DisplayName("Конструктор с массивами: выбрасывает исключение при длине < 2")
    void constructorArraysTooShort() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LinkedListTabulatedFunction(new double[]{1}, new double[]{2}),
                "Должно быть не менее 2 точек"
        );
        assertTrue(exception.getMessage().contains("не менее 2 точек"));
    }

    @Test
    @DisplayName("Конструктор с MathFunction: выбрасывает исключение при count < 2")
    void constructorMathFunctionTooFewPoints() {
        MathFunction linear = x -> 2 * x;
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LinkedListTabulatedFunction(linear, 0, 1, 1),
                "Должно быть не менее 2 точек"
        );
        assertTrue(exception.getMessage().contains("не менее 2"));
    }


    @Test
    @DisplayName("next() возвращает точки по порядку")
    void iteratorNextReturnsCorrectPoints() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        Iterator<Point> it = function.iterator();
        assertTrue(it.hasNext());
        Point p1 = it.next();
        assertEquals(1.0, p1.x, 1e-10);
        assertEquals(2.0, p1.y, 1e-10);

        Point p2 = it.next();
        assertEquals(2.0, p2.x, 1e-10);
        assertEquals(4.0, p2.y, 1e-10);

        Point p3 = it.next();
        assertEquals(3.0, p3.x, 1e-10);
        assertEquals(6.0, p3.y, 1e-10);

        assertFalse(it.hasNext());
    }

    @Test
    @DisplayName("next() после конца выбрасывает NoSuchElementException")
    void iteratorNextAfterEndThrowsException() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        Iterator<Point> it = function.iterator();
        while (it.hasNext()) it.next(); // проходим все

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                it::next,
                "После завершения итерации next() должен бросать исключение"
        );
        assertTrue(exception.getMessage().contains("Элементов больше нет"));
    }


    @Test
    @DisplayName("getY возвращает правильное значение")
    void getYReturnsCorrectValue() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        assertEquals(2.0, function.getY(0), 1e-10);
        assertEquals(4.0, function.getY(1), 1e-10);
        assertEquals(6.0, function.getY(2), 1e-10);
    }

    @Test
    @DisplayName("getY выбрасывает исключение при неверном индексе")
    void getYInvalidIndexThrowsException() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        assertThrows(IndexOutOfBoundsException.class, () -> function.getY(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> function.getY(3));
    }

    @Test
    @DisplayName("setY устанавливает новое значение")
    void setYUpdatesValue() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        function.setY(1, 100.0);
        assertEquals(100.0, function.getY(1), 1e-10);
    }

    @Test
    @DisplayName("setY выбрасывает исключение при неверном индексе")
    void setYInvalidIndexThrowsException() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        assertThrows(IndexOutOfBoundsException.class, () -> function.setY(-1, 5.0));
        assertThrows(IndexOutOfBoundsException.class, () -> function.setY(3, 5.0));
    }


    @Test
    @DisplayName("indexOfX возвращает правильный индекс")
    void indexOfXReturnsCorrectIndex() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        assertEquals(0, function.indexOfX(1.0));
        assertEquals(1, function.indexOfX(2.0));
        assertEquals(2, function.indexOfX(3.0));
        assertEquals(-1, function.indexOfX(999.0)); // не существует
    }


    @Test
    @DisplayName("indexOfY возвращает правильный индекс или -1")
    void indexOfYReturnsCorrectIndex() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        assertEquals(0, function.indexOfY(2.0));
        assertEquals(1, function.indexOfY(4.0));
        assertEquals(-1, function.indexOfY(999.0)); // не существует
    }

    @Test
    @DisplayName("indexOfY возвращает -1 при пустом списке")
    void indexOfYOnEmptyReturnsMinusOne() {
        LinkedListTabulatedFunction empty = new LinkedListTabulatedFunction(
                new double[]{1, 2}, new double[]{1, 2}
        );
        empty.remove(0);
        empty.remove(0); // теперь пусто

        assertEquals(-1, empty.indexOfY(1.0));
    }

    @Test
    @DisplayName("rightBound возвращает x последнего элемента")
    void rightBoundReturnsLastX() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        assertEquals(3.0, function.rightBound(), 1e-10);
    }

    @Test
    @DisplayName("rightBound выбрасывает исключение при пустом списке")
    void rightBoundOnEmptyThrowsException() {
        LinkedListTabulatedFunction empty = new LinkedListTabulatedFunction(
                new double[]{1, 2}, new double[]{1, 2}
        );
        empty.remove(0);
        empty.remove(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                empty::rightBound
        );
        assertTrue(exception.getMessage().contains("Список пуст"));
    }


    @Test
    @DisplayName("interpolate правильно интерполирует внутри интервала")
    void interpolateWorksInsideInterval() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        double interpolated = function.interpolate(1.5, 0); // floorIndex=0 → между 1 и 2
        assertEquals(3.0, interpolated, 1e-10);
    }

    @Test
    @DisplayName("interpolate выбрасывает InterpolationException при x вне интервала")
    void interpolateThrowsExceptionWhenXOutsideInterval() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        InterpolationException exception = assertThrows(

                InterpolationException.class,
                () -> function.interpolate(0.5, 0) // x=0.5, но интервал [1,2]
        );
        assertTrue(exception.getMessage().contains("находится вне интервала"));
    }

    @Test
    @DisplayName("remove удаляет элемент по индексу")
    void removeAtIndexWorks() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );
        assertEquals(3, function.getCount()); // до удаления
        function.remove(1); // удаляем (2,4)
        assertEquals(2, function.getCount());
        assertEquals(2.0, function.getY(0), 1e-10);
        assertEquals(6.0, function.getY(1), 1e-10); // теперь на индексе 1 — третья точка
    }


    @Test
    @DisplayName("floorNodeOfX выбрасывает исключение при пустом списке")
    void floorNodeOfXOnEmptyThrowsException() {
        LinkedListTabulatedFunction empty = new LinkedListTabulatedFunction(
                new double[]{1, 2}, new double[]{1, 2}
        );
        empty.remove(0);
        empty.remove(0);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> empty.floorNodeOfX(1.0)
        );
        assertTrue(exception.getMessage().contains("Список пуст"));
    }

    @Test
    @DisplayName("Список пуст для пуского")
    void VoidTest(){
        LinkedListTabulatedFunction v = new LinkedListTabulatedFunction(new double[]{1,2}, new double[]{1,2});
        v.remove(0);
        v.remove(0);
        assertThrows(IllegalStateException.class, ()->v.remove(0));
    }
    @Test
    @DisplayName("getNode выбрасывает исключение при отрицательном индексе")
    void getNodeThrowsExceptionOnNegativeIndex() throws Exception {
        java.lang.reflect.Method getNodeMethod = LinkedListTabulatedFunction.class.getDeclaredMethod("getNode", int.class);
        getNodeMethod.setAccessible(true);

        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );

        // Ловим InvocationTargetException — обёртку рефлексии
        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                () -> getNodeMethod.invoke(function, -1)
        );

        // Получаем реальное исключение, которое было брошено внутри getNode
        Throwable targetException = exception.getTargetException();
        assertInstanceOf(IndexOutOfBoundsException.class, targetException);
        assertTrue(targetException.getMessage().contains("Индекс: -1"));

    }
    @Test
    @DisplayName("getNode выбрасывает исключение при индексе >= count")
    void getNodeThrowsExceptionOnIndexTooLarge() throws Exception {
        java.lang.reflect.Method getNodeMethod = LinkedListTabulatedFunction.class.getDeclaredMethod("getNode", int.class);
        getNodeMethod.setAccessible(true);

        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                new double[]{1, 2, 3},
                new double[]{2, 4, 6}
        );

        // 1. Ловим обёртку — InvocationTargetException
        InvocationTargetException wrapperException = assertThrows(
                InvocationTargetException.class,
                () -> getNodeMethod.invoke(function, 4) // индекс 4 при count=3
        );
        Throwable realException = wrapperException.getTargetException();
        assertInstanceOf(IndexOutOfBoundsException.class, realException);
        assertTrue(realException.getMessage().contains("Индекс: 4"));
    }

    @Test
    @DisplayName("extrapolateLeft бросает исключение при <2 точек")
    void extrapolateLeftThrowsExceptionWhenLessThanTwoPoints() {
        // Создаём функцию с 1 точкой
        LinkedListTabulatedFunction singlePoint = new LinkedListTabulatedFunction(
                new double[]{1,2},
                new double[]{2, 4}
        );
        singlePoint.remove(1);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> singlePoint.extrapolateLeft(0.0)
            );
            assertTrue(exception.getMessage().contains("Экстраполяция на <2 элемента"));
        }

        @Test
        @DisplayName("extrapolateLeft работает при >=2 точек")
        void extrapolateLeftWorksWhenTwoOrMorePoints() {

            LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                    new double[]{1, 2, 3},
                    new double[]{2, 4, 6}
            );
            assertDoesNotThrow(() -> function.extrapolateLeft(0.0));

        }


        @Test
        @DisplayName("extrapolateRight бросает исключение при <2 точек")
        void extrapolateRightThrowsExceptionWhenLessThanTwoPoints() {
            LinkedListTabulatedFunction singlePoint = new LinkedListTabulatedFunction(
                    new double[]{1, 2},
                    new double[]{2, 4}
            );
            singlePoint.remove(1);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> singlePoint.extrapolateRight(5.0)
            );
            assertTrue(exception.getMessage().contains("Экстраполяция на <2 элемента"));
        }

        @Test
        @DisplayName("extrapolateRight работает при >=2 точек")
        void extrapolateRightWorksWhenTwoOrMorePoints() {
            LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                    new double[]{1, 2, 3},
                    new double[]{2, 4, 6}
            );
            assertDoesNotThrow(() -> function.extrapolateRight(4.0));

        }


        @Test
        @DisplayName("indexOfX возвращает правильный индекс для существующего x")
        void indexOfXReturnsCorrectIndexForExistingX() {
            LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                    new double[]{1, 2, 3},
                    new double[]{2, 4, 6}
            );
            assertEquals(0, function.indexOfX(1.0));
            assertEquals(1, function.indexOfX(2.0));
            assertEquals(2, function.indexOfX(3.0));
        }

        @Test
        @DisplayName("indexOfX возвращает -1 для несуществующего x")
        void indexOfXReturnsMinusOneForNonExistingX() {
            LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(
                    new double[]{1, 2, 3},
                    new double[]{2, 4, 6}
            );
            assertEquals(-1, function.indexOfX(999.0));
            assertEquals(-1, function.indexOfX(1.5));
        }
    @Test
    @DisplayName("extrapolateRight бросает исключение при <2 точек")
    void testIndexOfX_EmptyList_ThrowsException() {
        LinkedListTabulatedFunction singlePoint = new LinkedListTabulatedFunction(
                new double[]{1, 2},
                new double[]{2, 4}
        );
        singlePoint.remove(1);
        singlePoint.remove(0);


       assertThrows(IllegalStateException.class, ()->singlePoint.indexOfX(0));
    }



}
