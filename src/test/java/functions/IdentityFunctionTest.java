package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для IdentityFunction")
class IdentityFunctionTest{

    private final static double delta = 1e-8;

    @Test
    @DisplayName("Тест на проверку нуля")
    void testApplyWithZero() {
        IdentityFunction function = new IdentityFunction();
        assertEquals(0.0, function.apply(0.0),delta,"0 = 0, GOOD");
    }


    @Test
    @DisplayName("Тест на проверку положительных чисел")
    void testApplyWithPositiveNumbers(){
        IdentityFunction function = new IdentityFunction();
        assertEquals(1.0, function.apply(1.0), delta, "1 = 1, GOOD");
        assertEquals(45.0, function.apply(45.0), delta, "45 = 45, GOOD");
        assertEquals(1111111111.0, function.apply(1111111111.0), delta, "1111111111 = 1111111111, GOOD");
        assertEquals(345.0, function.apply(345.0), delta, "345 = 345, GOOD");
        assertEquals(6211111.0, function.apply(6211111.0), delta, "6211111 = 6211111, GOOD");
        assertEquals(543.0, function.apply(543.0), delta, "543 = 543, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку дробных положительных чисел")
    void testApplyWithPositiveFractionalNumbers(){
        IdentityFunction function = new IdentityFunction();
        assertEquals(0.00000000000001, function.apply(0.00000000000001), delta, "0.00000000000001 = 0.00000000000001, GOOD");
        assertEquals(45.54, function.apply(45.54), delta, "45.54 = 45.54, GOOD");
        assertEquals(1.1111111111111, function.apply(1.1111111111111), delta, "1.1111111111111 = 1.1111111111111, GOOD");
        assertEquals(5.252, function.apply(5.252), delta, "5.252 = 5.252, GOOD");
        assertEquals(111111111111.2, function.apply(111111111111.2), delta, "111111111111.2 = 111111111111.2, GOOD");
        assertEquals(99999999999999.345, function.apply(99999999999999.345), delta, "99999999999999.345 = 99999999999999.345, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку отрицательных чисел")
    void testApplyWithNegativeNumbers(){
        IdentityFunction function = new IdentityFunction();
        assertEquals(-1.0, function.apply(-1.0), delta, "-1 = -1, GOOD");
        assertEquals(-45.0, function.apply(-45.0), delta, "-45 = -45, GOOD");
        assertEquals(-1111111111.0, function.apply(-1111111111.0), delta, "-1111111111 = -1111111111, GOOD");
        assertEquals(-345.0, function.apply(-345.0), delta, "-345 = -345, GOOD");
        assertEquals(-6211111.0, function.apply(-6211111.0), delta, "-6211111 = -6211111, GOOD");
        assertEquals(-543.0, function.apply(-543.0), delta, "-543 = -543, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку отрицательных дробных чисел")
    void testApplyWithNegativeFractionalNumbers(){
        IdentityFunction function = new IdentityFunction();
        assertEquals(-0.00000000000001, function.apply(-0.00000000000001), delta, "-0.00000000000001 = -0.00000000000001, GOOD");
        assertEquals(-45.54, function.apply(-45.54), delta, "-45.54 = -45.54, GOOD");
        assertEquals(-1.1111111111111, function.apply(-1.1111111111111), delta, "-1.1111111111111 = -1.1111111111111, GOOD");
        assertEquals(-5.252, function.apply(-5.252), delta, "-5.252 = -5.252, GOOD");
        assertEquals(-111111111111.2, function.apply(-111111111111.2), delta, "-111111111111.2 = -111111111111.2, GOOD");
        assertEquals(-99999999999999.345, function.apply(-99999999999999.345), delta, "-99999999999999.345 = -99999999999999.345, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку экстремумов")
    void testApplyWithExtremeValues(){
        IdentityFunction function = new IdentityFunction();
        assertEquals(Double.MIN_VALUE, function.apply(Double.MIN_VALUE), delta, "4.9E-324 = 4.9E-324, GOOD");
        assertEquals(Double.MAX_VALUE, function.apply(Double.MAX_VALUE), delta, "1.7976931348623157E308 = 1.7976931348623157E308, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку NaN")
    void testApplyWithNaN(){
        IdentityFunction function = new IdentityFunction();
        assertTrue(Double.isNaN(function.apply(Double.NaN)), "NaN = NaN, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку бесконечностей")
    void testApplyWithInfinity(){
        IdentityFunction function = new IdentityFunction();
        assertEquals(Double.POSITIVE_INFINITY, function.apply(Double.POSITIVE_INFINITY), delta, "+∞ = +∞, GOOD");
        assertEquals(Double.NEGATIVE_INFINITY, function.apply(Double.NEGATIVE_INFINITY), delta, "-∞ = -∞, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку многочисленных запросов")
    void testMultipCalls(){
        IdentityFunction function = new IdentityFunction();
        for (int i = - 100; i <= 100; i++){
            double value = i * 0.5;
            assertEquals(value, function.apply(value),delta,value + " = " + function.apply(value) + ", GOOD");
        }
    }

}