package operations;

import functions.MathFunction;
import functions.SqrFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SteppingDifferentialOperatorsTest {

    private static final double DELTA = 1e-6;
    private static final double STEP = 0.001;

    @Test
    @DisplayName("LeftSteppingDifferentialOperator: f(x) = x² → f'(x) ≈ 2x")
    void testLeftDerivative() {
        MathFunction sqr = new SqrFunction();
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(STEP);
        MathFunction derivative = operator.derive(sqr);

        assertDerivative(derivative, 1.0, 2.0);
        assertDerivative(derivative, 2.0, 4.0);
        assertDerivative(derivative, 0.0, 0.0);
        assertDerivative(derivative, -1.0, -2.0);
    }

    @Test
    @DisplayName("RightSteppingDifferentialOperator: f(x) = x² → f'(x) ≈ 2x")
    void testRightDerivative() {
        MathFunction sqr = new SqrFunction();
        RightSteppingDifferentialOperator operator = new RightSteppingDifferentialOperator(STEP);
        MathFunction derivative = operator.derive(sqr);

        assertDerivative(derivative, 1.0, 2.0);
        assertDerivative(derivative, 2.0, 4.0);
        assertDerivative(derivative, 0.0, 0.0);
        assertDerivative(derivative, -1.0, -2.0);
    }

    @Test
    @DisplayName("MiddleSteppingDifferentialOperator: f(x) = x² → f'(x) ≈ 2x (точнее)")
    void testMiddleDerivative() {
        MathFunction sqr = new SqrFunction();
        MiddleSteppingDifferentialOperator operator = new MiddleSteppingDifferentialOperator(STEP);
        MathFunction derivative = operator.derive(sqr);

        assertDerivative(derivative, 1.0, 2.0);
        assertDerivative(derivative, 2.0, 4.0);
        assertDerivative(derivative, 0.0, 0.0);
        assertDerivative(derivative, -1.0, -2.0);
    }

    @Test
    @DisplayName("Проверка исключения при step <= 0")
    void testInvalidStepThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new LeftSteppingDifferentialOperator(0.0),
                "Step = 0 должен вызывать исключение");

        assertThrows(IllegalArgumentException.class,
                () -> new LeftSteppingDifferentialOperator(-0.1),
                "Отрицательный step должен вызывать исключение");

        assertThrows(IllegalArgumentException.class,
                () -> new LeftSteppingDifferentialOperator(Double.POSITIVE_INFINITY),
                "Бесконечный step должен вызывать исключение");

        assertThrows(IllegalArgumentException.class,
                () -> new LeftSteppingDifferentialOperator(Double.NaN),
                "NaN step должен вызывать исключение");
    }

    @Test
    @DisplayName("Проверка сеттера step")
    void testStepSetter() {
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(0.1);

        operator.setStep(0.5);
        assertEquals(0.5, operator.getStep(), DELTA, "Геттер должен возвращать установленное значение");

        assertThrows(IllegalArgumentException.class,
                () -> operator.setStep(-1.0),
                "Сеттер должен проверять корректность значения");
        assertThrows(IllegalArgumentException.class,
                () -> operator.setStep(Double.POSITIVE_INFINITY),
                "Сеттер должен проверять корректность значения");
        assertThrows(IllegalArgumentException.class,
                () -> operator.setStep(Double.NaN),
                "Сеттер должен проверять корректность значения");
    }

    @Test
    @DisplayName("Проверка исключения при null-функции")
    void testNullFunctionThrowsException() {
        LeftSteppingDifferentialOperator left = new LeftSteppingDifferentialOperator(STEP);
        RightSteppingDifferentialOperator right = new RightSteppingDifferentialOperator(STEP);
        MiddleSteppingDifferentialOperator middle = new MiddleSteppingDifferentialOperator(STEP);

        assertThrows(IllegalArgumentException.class, () -> left.derive(null));
        assertThrows(IllegalArgumentException.class, () -> right.derive(null));
        assertThrows(IllegalArgumentException.class, () -> middle.derive(null));
    }
    @Test
    @DisplayName("Проверка derive")
    void derive() {
        MathFunction f = new MathFunction() {

            @Override
            public double apply(double x) {
                return 5;
            }
        };
        SteppingDifferentialOperator operator = new SteppingDifferentialOperator(STEP);
        MathFunction derivative = operator.derive(f);

        assertNull(operator.derive(f), "Метод должен возвращать null");
    }

    private void assertDerivative(MathFunction derivative, double x, double expected) {
        double actual = derivative.apply(x);
        assertEquals(expected, actual, 0.01,
                "Производная в точке x=" + x + " должна быть ≈ " + expected + ", получено: " + actual);
    }
}