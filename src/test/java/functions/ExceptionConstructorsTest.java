package functions;

import exception.ArrayIsNotSortedException;
import exception.DifferentLengthOfArraysException;
import exception.InconsistentFunctionsException;
import exception.InterpolationException;
import operations.TabulatedDifferentialOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты конструкторов пользовательских исключений")
class ExceptionConstructorsTest {
    private static final double DELTA = 1e-10;

    @Test
    @DisplayName("ArrayIsNotSortedException() должен создавать исключение с null-сообщением")
    void testArrayIsNotSortedExceptionDefaultConstructor() {
        ArrayIsNotSortedException exception = new ArrayIsNotSortedException();
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("ArrayIsNotSortedException(String) должен сохранять сообщение")
    void testArrayIsNotSortedExceptionMessageConstructor() {
        String message = "Массив не отсортирован!";
        ArrayIsNotSortedException exception = new ArrayIsNotSortedException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("DifferentLengthOfArraysException() должен создавать исключение с null-сообщением")
    void testDifferentLengthOfArraysExceptionDefaultConstructor() {
        DifferentLengthOfArraysException exception = new DifferentLengthOfArraysException();
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("DifferentLengthOfArraysException(String) должен сохранять сообщение")
    void testDifferentLengthOfArraysExceptionMessageConstructor() {
        String message = "Длины массивов не совпадают!";
        DifferentLengthOfArraysException exception = new DifferentLengthOfArraysException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("InterpolationException() должен создавать исключение с null-сообщением")
    void testInterpolationExceptionDefaultConstructor() {
        InterpolationException exception = new InterpolationException();
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("InterpolationException(String) должен сохранять сообщение")
    void testInterpolationExceptionMessageConstructor() {
        String message = "Ошибка интерполяции!";
        InterpolationException exception = new InterpolationException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Производная в первой точке при двух точках: (y1 - y0) / (x1 - x0)")
    void testDerivativeAtFirstPointWithTwoPoints() {
            // Arrange: две точки
        double x0 = 1.0, x1 = 3.0;
        double y0 = 2.0, y1 = 6.0; // f(x) = 2x → производная = 2

        double[] x = {x0, x1};
        double[] y = {y0, y1};
        TabulatedFunction function = new ArrayTabulatedFunction(x, y);

        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

            // Act
        TabulatedFunction derivative = operator.derive(function);

            // Assert
        assertEquals(2, derivative.getCount(), "Должно быть две точки");

        double expectedDerivative = (y1 - y0) / (x1 - x0); // = (6-2)/(3-1) = 4/2 = 2.0
        assertEquals(expectedDerivative, derivative.getY(0), DELTA,
                "Производная в первой точке должна быть " + expectedDerivative);
    }

    @Test
    @DisplayName("Производная в первой точке при трёх точках: (y1 - y0) / (x1 - x0)")
    void testDerivativeAtFirstPointWithThreePoints() {
            // Arrange: три точки
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {0.0, 2.0, 4.0}; // линейная функция, производная = 2 везде

        TabulatedFunction function = new ArrayTabulatedFunction(x, y);
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

            // Act
        TabulatedFunction derivative = operator.derive(function);

            // Assert
        assertEquals(3, derivative.getCount());

        double expectedDerivative = (y[1] - y[0]) / (x[1] - x[0]); // = (2-0)/(1-0) = 2.0
        assertEquals(expectedDerivative, derivative.getY(0), DELTA,
                "Производная в первой точке должна вычисляться как (y1 - y0) / (x1 - x0)");
    }
    @Test
    @DisplayName("Тест конструктора по умолчанию")
    void testDefaultConstructor() {

        InconsistentFunctionsException exception = new InconsistentFunctionsException();

        assertNotNull(exception, "Исключение должно быть создано");
        assertNull(exception.getMessage(), "Сообщение должно быть null");
        assertNull(exception.getCause(), "Причина должна быть null");

        assertInstanceOf(RuntimeException.class, exception,
                "Должно быть RuntimeException");
        assertInstanceOf(InconsistentFunctionsException.class, exception,
                "Должно быть InconsistentFunctionsException");
    }

    @Test
    @DisplayName("Тест конструктора с сообщением")
    void testConstructorWithMessage() {
        String expectedMessage = "Функции имеют несовместимые размеры";
        InconsistentFunctionsException exception = new InconsistentFunctionsException(expectedMessage);
        assertNotNull(exception, "Исключение должно быть создано");
        assertEquals(expectedMessage, exception.getMessage(),
                "Сообщение должно соответствовать переданному");
        assertNull(exception.getCause(), "Причина должна быть null");

        // Проверка типа исключения
        assertInstanceOf(RuntimeException.class, exception,
                "Должно быть RuntimeException");
        assertInstanceOf(InconsistentFunctionsException.class, exception,
                "Должно быть InconsistentFunctionsException");
    }
}

