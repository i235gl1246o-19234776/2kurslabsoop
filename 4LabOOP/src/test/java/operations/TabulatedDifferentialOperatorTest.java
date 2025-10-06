package operations;

import concurrent.SynchronizedTabulatedFunction;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.factory.*;
import functions.TabulatedFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TabulatedDifferentialOperatorTest {

    private static final double DELTA = 1e-10;

    private static Stream<TabulatedFunctionFactory> provideFactories() {
        return Stream.of(
                new ArrayTabulatedFunctionFactory(),
                new LinkedListTabulatedFunctionFactory()
        );
    }

    private static final double[] X_LINEAR = {0.0, 1.0, 2.0, 3.0, 4.0};
    private static final double[] Y_LINEAR = {1.0, 3.0, 5.0, 7.0, 9.0}; // f(x) = 2x + 1

    private static final double[] X_QUADRATIC = {0.0, 1.0, 2.0, 3.0, 4.0};
    private static final double[] Y_QUADRATIC = {0.0, 1.0, 4.0, 9.0, 16.0}; // f(x) = x^2

    @ParameterizedTest
    @MethodSource("provideFactories")
    @DisplayName("Производная линейной функции f(x) = 2x + 1 должна быть константой 2.0")
    void testLinearFunctionDerivative(TabulatedFunctionFactory factory) {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);
        TabulatedFunction linearFunction = factory.create(X_LINEAR, Y_LINEAR);
        TabulatedFunction derivative = operator.derive(linearFunction);

        assertEquals(X_LINEAR.length, derivative.getCount(), "Количество точек должно сохраняться");

        for (int i = 1; i < derivative.getCount() - 1; i++) {
            assertEquals(2.0, derivative.getY(i), DELTA,
                    "Производная в точке x=" + X_LINEAR[i] + " должна быть 2.0");
        }

        if (derivative.getCount() > 1) {
            assertEquals(2.0, derivative.getY(0), DELTA, "Производная в первой точке должна быть 2.0");
            assertEquals(2.0, derivative.getY(derivative.getCount() - 1), DELTA,
                    "Производная в последней точке должна быть 2.0");
        }
    }

    @ParameterizedTest
    @MethodSource("provideFactories")
    @DisplayName("Производная квадратичной функции f(x) = x² должна быть f'(x) = 2x")
    void testQuadraticFunctionDerivative(TabulatedFunctionFactory factory) {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);
        TabulatedFunction quadraticFunction = factory.create(X_QUADRATIC, Y_QUADRATIC);
        TabulatedFunction derivative = operator.derive(quadraticFunction);

        assertEquals(X_QUADRATIC.length, derivative.getCount(), "Количество точек должно сохраняться");

        for (int i = 0; i < derivative.getCount(); i++) {
            double x = derivative.getX(i);
            double expected = 2 * x;
            assertEquals(expected, derivative.getY(i), DELTA,
                    "Производная в точке x=" + x + " должна быть " + expected);
        }

        int middleIndex = derivative.getCount() / 2;
        double x = derivative.getX(middleIndex);
        double expected = 2 * x;
        assertEquals(expected, derivative.getY(middleIndex), DELTA,
                "Производная в средней точке x=" + x + " должна быть " + expected);
    }

    @ParameterizedTest
    @MethodSource("provideFactories")
    @DisplayName("Производная функции из двух точек должна быть постоянной (разностное приближение)")
    void testTwoPointFunction(TabulatedFunctionFactory factory) {
        double[] x = {1.0, 3.0};
        double[] y = {2.0, 6.0}; // f(x) = 2x → производная = 2

        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);
        TabulatedFunction function = factory.create(x, y);
        TabulatedFunction derivative = operator.derive(function);

        assertEquals(2, derivative.getCount(), "Должно быть две точки");

        double expectedDerivative = (y[1] - y[0]) / (x[1] - x[0]); // = 2.0

        assertEquals(expectedDerivative, derivative.getY(0), DELTA, "Производная в первой точке");
        assertEquals(expectedDerivative, derivative.getY(1), DELTA, "Производная во второй точке");
    }

    @Test
    @DisplayName("Проверка работы с разными фабриками")
    void testDifferentFactories() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {0.0, 1.0, 4.0};

        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedDifferentialOperator arrayOperator = new TabulatedDifferentialOperator(arrayFactory);
        TabulatedFunction derivative1 = arrayOperator.derive(arrayFactory.create(x, y));
        assertTrue(derivative1 instanceof functions.ArrayTabulatedFunction,
                "Должна создаваться ArrayTabulatedFunction");

        TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
        TabulatedDifferentialOperator linkedListOperator = new TabulatedDifferentialOperator(linkedListFactory);
        TabulatedFunction derivative2 = linkedListOperator.derive(linkedListFactory.create(x, y));
        assertTrue(derivative2 instanceof functions.LinkedListTabulatedFunction,
                "Должна создаваться LinkedListTabulatedFunction");
    }

    @Test
    @DisplayName("Конструктор по умолчанию должен использовать ArrayTabulatedFunctionFactory")
    void testDefaultConstructor() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        assertNotNull(operator.getFactory(), "Фабрика не должна быть null");
        assertTrue(operator.getFactory() instanceof ArrayTabulatedFunctionFactory,
                "По умолчанию должна использоваться ArrayTabulatedFunctionFactory");
    }

    @Test
    @DisplayName("Проверка сеттера и геттера фабрики")
    void testFactorySetterGetter() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();

        operator.setFactory(newFactory);
        assertSame(newFactory, operator.getFactory(), "Геттер должен возвращать установленную фабрику");
    }

    @Test
    @DisplayName("Установка null фабрики должна вызывать IllegalArgumentException")
    void testSetNullFactory() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        assertThrows(
                IllegalArgumentException.class,
                () -> operator.setFactory(null),
                "Должно бросаться исключение при установке null фабрики"
        );
    }

    @Test
    @DisplayName("deriveSynchronously: совпадение с derive() для ArrayTabulatedFunction")
    public void testDeriveSynchronouslyEqualsDerive_Array() {
        double[] x = {0.0, 1.0, 2.0, 3.0};
        double[] y = {0.0, 1.0, 4.0, 9.0}; // f(x) = x^2
        TabulatedFunction func = new ArrayTabulatedFunction(x, y);

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();

        TabulatedFunction deriv1 = op.derive(func);
        TabulatedFunction deriv2 = op.deriveSynchronously(func);

        assertEquals(deriv1.getCount(), deriv2.getCount());
        for (int i = 0; i < deriv1.getCount(); i++) {
            assertEquals(deriv1.getY(i), deriv2.getY(i), 1e-10,
                    "Результаты должны совпадать для точки " + i);
        }
    }

    @Test
    @DisplayName("deriveSynchronously: совпадение с derive() для LinkedListTabulatedFunction")
    public void testDeriveSynchronouslyEqualsDerive_LinkedList() {
        double[] x = {0.0, 0.5, 1.0};
        double[] y = {0.0, 0.25, 1.0}; // f(x) = x^2
        TabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction deriv1 = op.derive(func);
        TabulatedFunction deriv2 = op.deriveSynchronously(func);

        assertEquals(deriv1.getCount(), deriv2.getCount());
        for (int i = 0; i < deriv1.getCount(); i++) {
            assertEquals(deriv1.getY(i), deriv2.getY(i), 1e-10);
        }
    }

    @Test
    @DisplayName("deriveSynchronously: входная функция уже SynchronizedTabulatedFunction")
    public void testDeriveSynchronouslyWithAlreadySynchronized() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 4.0, 9.0}; // f(x) = x^2
        TabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(base);

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();

        TabulatedFunction result = op.deriveSynchronously(syncFunc);

        assertNotNull(result);
        assertEquals(3, result.getCount());

        //f'(x) = 2x → f'(1)=2, f'(2)=4, f'(3)=6
        assertEquals(2.0, result.getY(0), 0.5);
        assertEquals(4.0, result.getY(1), 0.1);
        assertEquals(6.0, result.getY(2), 0.5);
    }


    @Test
    @DisplayName("deriveSynchronously: исключение при null")
    public void testDeriveSynchronouslyNull() {
        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        assertThrows(IllegalArgumentException.class, () -> {
            op.deriveSynchronously(null);
        });
    }
}