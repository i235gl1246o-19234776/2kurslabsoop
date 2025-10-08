package operations;

import functions.MathFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для класса SimpsonIntegral")
class SimpsonIntegralTest {

    @Test
    @DisplayName("Конструктор должен корректно инициализировать объект")
    void testConstructor() {
        MathFunction constant = x -> 1.0;
        double a = 0.0;
        double b = 1.0;
        int n = 100;

        SimpsonIntegral integral = new SimpsonIntegral(constant, a, b, n);

        assertNotNull(integral, "SimpsonIntegral должен создаваться");
    }

    @Test
    @DisplayName("Конструктор должен делать n чётным")
    void testConstructorMakesEvenN() {
        MathFunction constant = x -> 1.0;
        double a = 0.0;
        double b = 1.0;
        int oddN = 999;

        SimpsonIntegral integral = new SimpsonIntegral(constant, a, b, oddN);
        double result = integral.compute();

        assertEquals(1.0, result, 1e-8,
                "Вычисление должно работать даже при нечётном n в конструкторе");
    }

    @Test
    @DisplayName("Метод compute() должен корректно вычислять интеграл константной функции")
    void testComputeConstantFunction() {
        MathFunction constant = x -> 5.0;
        double a = 0.0;
        double b = 10.0;
        int n = 100;
        double expected = 5.0 * (b - a); //50

        SimpsonIntegral integral = new SimpsonIntegral(constant, a, b, n);

        double result = integral.compute();

        assertEquals(expected, result, 1e-10,
                "Интеграл константы 5 на [0,10] должен быть 50");
    }

    @Test
    @DisplayName("Метод compute() должен корректно вычислять интеграл линейной функции")
    void testComputeLinearFunction() {
        //f(x) = 2x + 1, ∫(2x+1)dx = x² + x
        MathFunction linear = x -> 2 * x + 1;
        double a = 0.0;
        double b = 5.0;
        int n = 1000;
        double expected = (b * b + b) - (a * a + a); //30

        SimpsonIntegral integral = new SimpsonIntegral(linear, a, b, n);

        double result = integral.compute();

        assertEquals(expected, result, 1e-8,
                "Интеграл 2x+1 на [0,5] должен быть 30");
    }

    @Test
    @DisplayName("Метод compute() должен корректно вычислять интеграл квадратичной функции")
    void testComputeQuadraticFunction() {
        //f(x) = x², ∫x²dx = x³/3
        MathFunction quadratic = x -> x * x;
        double a = 0.0;
        double b = 3.0;
        int n = 1000;
        double expected = (b * b * b / 3) - (a * a * a / 3); // 9

        SimpsonIntegral integral = new SimpsonIntegral(quadratic, a, b, n);

        double result = integral.compute();

        assertEquals(expected, result, 1e-8,
                "Интеграл x² на [0,3] должен быть 9");
    }

    @Test
    @DisplayName("Метод compute() должен корректно вычислять интеграл кубической функции")
    void testComputeCubicFunction() {
        //f(x) = x³, ∫x³dx = x⁴/4
        MathFunction cubic = x -> x * x * x;
        double a = 0.0;
        double b = 2.0;
        int n = 1000;
        double expected = (Math.pow(b, 4) / 4) - (Math.pow(a, 4) / 4); // 4

        SimpsonIntegral integral = new SimpsonIntegral(cubic, a, b, n);

        double result = integral.compute();

        assertEquals(expected, result, 1e-8,
                "Интеграл x³ на [0,2] должен быть 4");
    }

    @Test
    @DisplayName("Метод compute() должен корректно вычислять интеграл синуса")
    void testComputeSinFunction() {
        //f(x) = sin(x), ∫sin(x)dx = -cos(x)
        MathFunction sinFunc = Math::sin;
        double a = 0.0;
        double b = Math.PI;
        int n = 10000;
        double expected = -Math.cos(b) + Math.cos(a); //2

        SimpsonIntegral integral = new SimpsonIntegral(sinFunc, a, b, n);

        double result = integral.compute();

        assertEquals(expected, result, 1e-8,
                "Интеграл sin(x) на [0,π] должен быть 2");
    }

    @Test
    @DisplayName("Метод compute() должен корректно вычислять интеграл косинуса")
    void testComputeCosFunction() {
        //f(x) = cos(x), ∫cos(x)dx = sin(x)
        MathFunction cosFunc = Math::cos;
        double a = 0.0;
        double b = Math.PI / 2;
        int n = 1000;
        double expected = Math.sin(b) - Math.sin(a); //1

        SimpsonIntegral integral = new SimpsonIntegral(cosFunc, a, b, n);

        double result = integral.compute();

        assertEquals(expected, result, 1e-8,
                "Интеграл cos(x) на [0,π/2] должен быть 1");
    }

    @Test
    @DisplayName("Метод compute() должен корректно вычислять интеграл экспоненты")
    void testComputeExpFunction() {
        //f(x) = e^x, ∫e^xdx = e^x
        MathFunction expFunc = Math::exp;
        double a = 0.0;
        double b = 1.0;
        int n = 10000;
        double expected = Math.exp(b) - Math.exp(a); // e - 1

        SimpsonIntegral integral = new SimpsonIntegral(expFunc, a, b, n);

        double result = integral.compute();

        assertEquals(expected, result, 1e-8,
                "Интеграл e^x на [0,1] должен быть e - 1");
    }

    @Test
    @DisplayName("Последовательное вычисление для n <= THRESHOLD")
    void testSequentialComputation() {
        MathFunction constant = x -> 1.0;
        double a = 0.0;
        double b = 1.0;
        int smallN = 500; //Меньше порога

        SimpsonIntegral integral = new SimpsonIntegral(constant, a, b, smallN);

        double result = integral.compute();

        assertEquals(1.0, result, 1e-8,
                "Последовательное вычисление должно работать корректно");
    }

    @Test
    @DisplayName("Параллельное вычисление для n > THRESHOLD")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testParallelComputation() {
        MathFunction quadratic = x -> x * x;
        double a = 0.0;
        double b = 10.0;
        int largeN = 2000; //Больше порога

        SimpsonIntegral integral = new SimpsonIntegral(quadratic, a, b, largeN);

        double result = integral.compute();

        double expected = 333.3333333333333; // ∫x²dx от 0 до 10
        assertEquals(expected, result, 1e-6,
                "Параллельное вычисление должно работать корректно");
    }

    @Test
    @DisplayName("Метод compute() должен работать при a > b")
    void testComputeWithReversedInterval() {
        MathFunction constant = x -> 2.0;
        double a = 10.0;
        double b = 0.0;
        int n = 100;
        double expected = -20.0; // 2 * (0 - 10) = -20

        SimpsonIntegral integral = new SimpsonIntegral(constant, a, b, n);

        double result = integral.compute();

        assertEquals(expected, result, 1e-10,
                "Интеграл при a > b должен быть отрицательным");
    }

    @Test
    @DisplayName("Метод compute() должен возвращать 0 при a = b")
    void testComputeWithZeroInterval() {
        MathFunction func = x -> x * x + 2 * x + 1;
        double a = 5.0;
        double b = 5.0;
        int n = 100;

        SimpsonIntegral integral = new SimpsonIntegral(func, a, b, n);

        double result = integral.compute();

        assertEquals(0.0, result, 1e-10,
                "Интеграл на интервале нулевой длины должен быть 0");
    }

    @Test
    @DisplayName("Интеграл нечётной функции на симметричном интервале должен быть 0")
    void testOddFunctionSymmetricInterval() {
        MathFunction oddFunction = x -> x * x * x;
        double a = -2.0;
        double b = 2.0;
        long n = 1000;

        SimpsonIntegral integral = new SimpsonIntegral(oddFunction, a, b, n);

        double result = integral.compute();

        assertEquals(0.0, result, 1e-10,
                "Интеграл нечётной функции на симметричном интервале должен быть 0");
    }

    @Test
    @DisplayName("Точность должна увеличиваться с ростом n")
    void testAccuracyIncreasesWithN() {
        MathFunction func = x -> x * x * x * x;
        double a = 0.0;
        double b = 2.0;
        double exact = (Math.pow(b, 5) / 5) - (Math.pow(a, 5) / 5); //6.4

        SimpsonIntegral integral100 = new SimpsonIntegral(func, a, b, 100);
        SimpsonIntegral integral1000 = new SimpsonIntegral(func, a, b, 1000);
        SimpsonIntegral integral10000 = new SimpsonIntegral(func, a, b, 10000);

        double result100 = integral100.compute();
        double result1000 = integral1000.compute();
        double result10000 = integral10000.compute();

        double error100 = Math.abs(result100 - exact);
        double error1000 = Math.abs(result1000 - exact);
        double error10000 = Math.abs(result10000 - exact);

        assertTrue(error10000 < error1000,
                "Точность при n=10000 должна быть выше чем при n=1000");
        assertTrue(error1000 < error100,
                "Точность при n=1000 должна быть выше чем при n=100");
    }

    @Test
    @DisplayName("Метод compute() должен работать со сложными составными функциями")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testComputeWithComplexFunction() {
        MathFunction complexFunc = x -> Math.sin(x) * Math.cos(x) * Math.exp(-x * x);
        double a = -5.0;
        double b = 5.0;
        int n = 50000;

        SimpsonIntegral integral = new SimpsonIntegral(complexFunc, a, b, n);

        double result = integral.compute();

        assertTrue(Double.isFinite(result), "Результат должен быть конечным числом");
        assertFalse(Double.isNaN(result), "Результат не должен быть NaN");
        assertTrue(Math.abs(result) < 10, "Результат должен быть разумным по величине");
    }

    @Test
    @DisplayName("Метод compute() должен быть идемпотентным")
    void testComputeIsIdempotent() {
        MathFunction func = x -> 3 * x + 2;
        double a = 0.0;
        double b = 5.0;
        int n = 1000;

        SimpsonIntegral integral = new SimpsonIntegral(func, a, b, n);

        double result1 = integral.compute();
        double result2 = integral.compute();
        double result3 = integral.compute();

        assertEquals(result1, result2, 1e-10, "Результаты должны быть одинаковыми");
        assertEquals(result2, result3, 1e-10, "Результаты должны быть одинаковыми");
    }

    @Test
    @DisplayName("SimpsonIntegral должен работать в ForkJoinPool")
    void testWithForkJoinPool() {
        MathFunction func = x -> x * x;
        double a = 0.0;
        double b = 10.0;
        int n = 2000; //Для параллельного выполнения

        SimpsonIntegral integral = new SimpsonIntegral(func, a, b, n);

        ForkJoinPool pool = new ForkJoinPool();
        double result = pool.invoke(integral);

        double expected = 333.3333333333333; // ∫x²dx от 0 до 10
        assertEquals(expected, result, 1e-6,
                "Результат выполнения в ForkJoinPool должен быть корректным");
    }

    @Test
    @DisplayName("Метод compute() должен корректно работать с логарифмической функцией")
    void testComputeWithLogFunction() {
        //f(x) = ln(x), ∫ln(x)dx = x·ln(x) - x
        MathFunction logFunc = Math::log;
        double a = 1.0;
        double b = Math.E;
        int n = 10000;
        double expected = (b * Math.log(b) - b) - (a * Math.log(a) - a); // 1

        SimpsonIntegral integral = new SimpsonIntegral(logFunc, a, b, n);

        double result = integral.compute();

        assertEquals(expected, result, 1e-8,
                "Интеграл ln(x) на [1,e] должен быть 1");
    }

    @Test
    @DisplayName("Метод compute() должен корректно работать с функцией 1/(1+x²)")
    void testComputeWithRationalFunction() {
        //f(x) = 1/(1+x²), ∫dx/(1+x²) = arctg(x)
        MathFunction rational = x -> 1.0 / (1.0 + x * x);
        double a = -1.0;
        double b = 1.0;
        int n = 10000;
        double expected = Math.atan(b) - Math.atan(a); // π/2

        SimpsonIntegral integral = new SimpsonIntegral(rational, a, b, n);

        double result = integral.compute();

        assertEquals(expected, result, 1e-8,
                "Интеграл 1/(1+x²) на [-1,1] должен быть π/2");
    }
}