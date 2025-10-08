package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для ZeroFunction")
class ZeroFunctionTest {

    @Test
    @DisplayName("Тест на проверку возвращения 0")
    void testApplyAlwaysReturnsZero() {
        ZeroFunction zero = new ZeroFunction();

        assertEquals(0.0, zero.apply(0.0), 1e-10, "Должен возвращать 0.0 для x=0, GOOD");
        assertEquals(0.0, zero.apply(1.0), 1e-10, "Должен возвращать 0.0 для x=1, GOOD");
        assertEquals(0.0, zero.apply(-1.0), 1e-10, "Должен возвращать 0.0 для x=-1, GOOD");
        assertEquals(0.0, zero.apply(100.0), 1e-10, "Должен возвращать 0.0 для x=100, GOOD");
        assertEquals(0.0, zero.apply(-50.0), 1e-10, "Должен возвращать 0.0 для x=-50, GOOD");
        assertEquals(0.0, zero.apply(999.999), 1e-10, "Должен возвращать 0.0 для x=999.999, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку наследования от постоянной функции")
    void testInheritanceFromConstantFunction() {
        ZeroFunction zero = new ZeroFunction();
        assertTrue(zero instanceof ConstantFunction, "ZeroFunction должен наследовать от ConstantFunction, GOOD");

        assertTrue(zero instanceof MathFunction, "ZeroFunction должен реализовывать MathFunction, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку метода getConstant")
    void testGetConstantReturnsZero() {
        ZeroFunction zero = new ZeroFunction();

        assertEquals(0.0, zero.getConstant(), 1e-10, "getConstant() должен возвращать 0.0, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку без аргументов")
    void testConstructorNoArguments() {
        ZeroFunction zero = new ZeroFunction();
        assertNotNull(zero, "Конструктор без аргументов должен создавать объект, GOOD");

        assertEquals(0.0, zero.apply(5.0), 1e-10, "Должен возвращать 0.0 после создания через конструктор без аргументов, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку дополнительных полей")
    void testNoAdditionalFields() {
        ZeroFunction zero = new ZeroFunction();

        assertEquals(0.0, zero.getConstant(), 1e-10, "Должен вести себя как ConstantFunction(0.0), GOOD");
    }

}