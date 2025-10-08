package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для UnitFunction")
class UnitFunctionTest {

    @Test
    @DisplayName("Тест на проверку возвращения единицы")
    void testApplyAlwaysReturnsOne() {
        UnitFunction unit = new UnitFunction();

        assertEquals(1.0, unit.apply(0.0), 1e-10, "Должен возвращать 1.0 для x=0, GOOD");
        assertEquals(1.0, unit.apply(1.0), 1e-10, "Должен возвращать 1.0 для x=1, GOOD");
        assertEquals(1.0, unit.apply(-1.0), 1e-10, "Должен возвращать 1.0 для x=-1, GOOD");
        assertEquals(1.0, unit.apply(100.0), 1e-10, "Должен возвращать 1.0 для x=100, GOOD");
        assertEquals(1.0, unit.apply(-50.0), 1e-10, "Должен возвращать 1.0 для x=-50, GOOD");
        assertEquals(1.0, unit.apply(123.456), 1e-10, "Должен возвращать 1.0 для x=123.456, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку наследования от постоянной функции")
    void testInheritanceFromConstantFunction() {
        UnitFunction unit = new UnitFunction();
        assertTrue(unit instanceof ConstantFunction, "UnitFunction должен наследовать от ConstantFunction, GOOD");
        assertTrue(unit instanceof MathFunction, "UnitFunction должен реализовывать MathFunction, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку метода getConstant")
    void testGetConstantReturnsOne() {
        UnitFunction unit = new UnitFunction();
        assertEquals(1.0, unit.getConstant(), 1e-10, "getConstant() должен возвращать 1.0, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку без аргументов")
    void testConstructorNoArguments() {
        UnitFunction unit = new UnitFunction();
        assertNotNull(unit, "Конструктор без аргументов должен создавать объект, GOOD");
        assertEquals(1.0, unit.apply(5.0), 1e-10, "Должен возвращать 1.0 после создания через конструктор без аргументов, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку дополнительных полей")
    void testNoAdditionalFields() {
        UnitFunction unit = new UnitFunction();
        assertEquals(1.0, unit.getConstant(), 1e-10, "Должен вести себя как ConstantFunction(1.0), GOOD");
    }

    @Test
    @DisplayName("Тест на проверку идентичности ConstantFunction")
    void testComparisonWithConstantFunction() {
        UnitFunction unit = new UnitFunction();
        ConstantFunction constantOne = new ConstantFunction(1.0);

        //Проверяем, что UnitFunction ведет себя идентично ConstantFunction(1.0)
        assertEquals(constantOne.apply(10.0), unit.apply(10.0), 1e-10, "Должен вести себя как ConstantFunction(1.0), GOOD");
        assertEquals(constantOne.apply(-10.0), unit.apply(-10.0), 1e-10, "Должен вести себя как ConstantFunction(1.0), GOOD");
        assertEquals(constantOne.apply(0.0), unit.apply(0.0), 1e-10, "Должен вести себя как ConstantFunction(1.0), GOOD");

        assertEquals(constantOne.getConstant(), unit.getConstant(), 1e-10, "getConstant() должен возвращать 1.0, GOOD");
    }
}