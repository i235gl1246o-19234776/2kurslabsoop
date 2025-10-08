package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для CompositeFunction")
class CompositeFunctionTest {
    
    private final static double delta = 1e-4;

    @Test
    @DisplayName("Тест на проверку Identity + Identity")
    void testDoubleIdentityComposition() {
        MathFunction identity = new IdentityFunction();
        CompositeFunction composite = new CompositeFunction(identity, identity);

        assertEquals(5.0, composite.apply(5.0), delta, "identity(identity(5)) = 5, GOOD");
        assertEquals(-3.0, composite.apply(-3.0), delta, "identity(identity(-3)) = -3, GOOD");
        assertEquals(0.0, composite.apply(0.0), delta, "identity(identity(0)) = 0, GOOD");
        assertEquals(7.5, composite.apply(7.5), delta, "identity(identity(7.5)) = 7.5, GOOD");
        assertEquals(100.0, composite.apply(100.0), delta, "identity(identity(100)) = 100, GOOD");
        assertEquals(-2.2, composite.apply(-2.2), delta, "identity(identity(-2.2)) = -2.2, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку Identity + Sqr/Sqr + Identity")
    void testSqrAndIdentityComposition() {
        MathFunction sqr = new SqrFunction();
        MathFunction identity = new IdentityFunction();
        CompositeFunction sqrThenIdentity = new CompositeFunction(sqr, identity);
        CompositeFunction identityThenSqr = new CompositeFunction(identity, sqr);

        assertEquals(25.0, sqrThenIdentity.apply(5.0), delta, "identity(sqr(5)) = 25, GOOD");
        assertEquals(25.0, identityThenSqr.apply(5.0), delta, "sqr(identity(5)) = 25, GOOD");
        assertEquals(4.0, sqrThenIdentity.apply(-2.0), delta, "identity(sqr(-2)) = 4, GOOD");
        assertEquals(4.0, identityThenSqr.apply(-2.0), delta, "sqr(identity(-2)) = 4,GOOD");
        assertEquals(0.0, sqrThenIdentity.apply(0.0), delta, "identity(sqr(0)) = 0, GOOD");
        assertEquals(0.0, identityThenSqr.apply(0.0), delta, "sqr(identity(0)) = 0, GOOD");
        assertEquals(6.25, sqrThenIdentity.apply(2.5), delta, "identity(sqr(2.5)) = 6.25, GOOD");
        assertEquals(6.25, identityThenSqr.apply(2.5), delta, "sqr(identity(2.5)) = 6.25, GOOD");
        assertEquals(6.25, sqrThenIdentity.apply(-2.5), delta, "identity(sqr(-2.5)) = 6.25, GOOD");
        assertEquals(6.25, identityThenSqr.apply(-2.5), delta, "sqr(identity(-2.5)) = 6.25, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку Sqr + Sqr")
    void testDoubleSqrComposition() {
        MathFunction sqr = new SqrFunction();
        CompositeFunction sqrSqr = new CompositeFunction(sqr, sqr);

        assertEquals(625.0, sqrSqr.apply(5.0), delta, "sqr(sqr(5)) = 625, GOOD");
        assertEquals(16.0, sqrSqr.apply(2.0), delta, "sqr(sqr(2)) = 16, GOOD");
        assertEquals(16.0, sqrSqr.apply(-2.0), delta, "sqr(sqr(-2)) = 16, GOOD");
        assertEquals(0.0, sqrSqr.apply(0.0), delta, "sqr(sqr(0)) = 0, GOOD");
        assertEquals(390625.0, sqrSqr.apply(25.0), delta, "sqr(sqr(25)) = 390625, GOOD");
        assertEquals(1.0, sqrSqr.apply(1.0), delta, "sqr(sqr(1)) = 1, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку тройной композиции")
    void testTripleComposition() {
        MathFunction sqr = new SqrFunction();
        MathFunction identity = new IdentityFunction();

        CompositeFunction firstLevel = new CompositeFunction(sqr, identity);
        CompositeFunction secondLevel = new CompositeFunction(firstLevel, sqr);
        CompositeFunction thirdLevel = new CompositeFunction(secondLevel, identity);

        assertEquals(625.0, secondLevel.apply(5.0), delta, "sqr(identity(sqr(5))) = 625, GOOD");
        assertEquals(625.0, thirdLevel.apply(5.0), delta, "identity(sqr(identity(sqr(5)))) = 625, GOOD");
        assertEquals(256.0, secondLevel.apply(4.0), delta, "sqr(identity(sqr(4))) = 256, GOOD");
        assertEquals(256.0, thirdLevel.apply(4.0), delta, "identity(sqr(identity(sqr(4)))) = 256, GOOD");
        assertEquals(16.0, secondLevel.apply(2.0), delta, "sqr(identity(sqr(2))) = 16, GOOD");
        assertEquals(16.0, thirdLevel.apply(2.0), delta, "identity(sqr(identity(sqr(2)))) = 16, GOOD");
        assertEquals(0.0, secondLevel.apply(0.0), delta, "sqr(identity(sqr(0))) = 0, GOOD");
        assertEquals(0.0, thirdLevel.apply(0.0), delta, "identity(sqr(identity(sqr(0)))) = 0, GOOD");
        assertEquals(1.0, secondLevel.apply(1.0), delta, "sqr(identity(sqr(1))) = 1, GOOD");
        assertEquals(1.0, thirdLevel.apply(1.0), delta, "identity(sqr(identity(sqr(1)))) = 1, GOOD");
        assertEquals(1.0, secondLevel.apply(-1.0), delta, "sqr(identity(sqr(-1))) = 1, GOOD");
        assertEquals(1.0, thirdLevel.apply(-1.0), delta, "identity(sqr(identity(sqr(-1)))) = 1, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку Sqr/Identity из 4 функций")
    void testComplexNestedComposition() {
        MathFunction sqr = new SqrFunction();
        MathFunction identity = new IdentityFunction();

        CompositeFunction level1 = new CompositeFunction(sqr, sqr);
        CompositeFunction level2 = new CompositeFunction(level1, identity);
        CompositeFunction level3 = new CompositeFunction(identity, level2);
        CompositeFunction level4 = new CompositeFunction(level3, sqr);

        assertEquals(625.0, level1.apply(5.0), delta, "sqr(sqr(5)) = 625, GOOD");
        assertEquals(625.0, level2.apply(5.0), delta, "identity(sqr(sqr(5))) = 625, GOOD");
        assertEquals(625.0, level3.apply(5.0), delta, "identity(sqr(sqr(5))) через level3 = 625, GOOD");
        assertEquals(390625.0, level4.apply(5.0), 1.0, "sqr(identity(sqr(sqr(5)))) = 390625, GOOD");

        assertEquals(16.0, level1.apply(2.0), delta, "sqr(sqr(2)) = 16, GOOD");
        assertEquals(16.0, level2.apply(2.0), delta, "identity(sqr(sqr(2))) = 16, GOOD");
        assertEquals(16.0, level3.apply(2.0), delta, "identity(sqr(sqr(2))) через level3 = 16, GOOD");
        assertEquals(256.0, level4.apply(2.0), delta, "sqr(identity(sqr(sqr(2)))) = 256, GOOD");

        assertEquals(0.0, level1.apply(0.0), delta, "sqr(sqr(0)) = 0, GOOD");
        assertEquals(0.0, level2.apply(0.0), delta, "identity(sqr(sqr(0))) = 0, GOOD");
        assertEquals(0.0, level3.apply(0.0), delta, "identity(sqr(sqr(0))) через level3 = 0, GOOD");
        assertEquals(0.0, level4.apply(0.0), delta, "sqr(identity(sqr(sqr(0)))) = 0, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку сложной функции с другими функциями")
    void testWithCustomFunctions() {
        MathFunction doubleFunction = x -> 2*x;

        MathFunction tripleFunction = x -> 3*x;

        CompositeFunction doubleThenTriple = new CompositeFunction(doubleFunction, tripleFunction);
        CompositeFunction tripleThenDouble = new CompositeFunction(tripleFunction, doubleFunction);

        assertEquals(30.0, doubleThenTriple.apply(5.0), delta, "triple(double(5)) = 30, GOOD");
        assertEquals(30.0, tripleThenDouble.apply(5.0), delta, "double(triple(5)) = 30, GOOD");
        assertEquals(0.0, doubleThenTriple.apply(0.0), delta, "triple(double(0)) = 0, GOOD");
        assertEquals(0.0, tripleThenDouble.apply(0.0), delta, "double(triple(0)) = 0, GOOD");
        assertEquals(-12.0, doubleThenTriple.apply(-2.0), delta, "triple(double(-2)) = -12, GOOD");
        assertEquals(-12.0, tripleThenDouble.apply(-2.0), delta, "double(triple(-2)) = -12, GOOD");
        assertEquals(7.2, doubleThenTriple.apply(1.2), delta, "triple(double(1.2)) = 7.2, GOOD");
        assertEquals(7.2, tripleThenDouble.apply(1.2), delta, "double(triple(1.2)) = 7.2, GOOD");
        assertEquals(60.0, doubleThenTriple.apply(10.0), delta, "triple(double(10)) = 60, GOOD");
        assertEquals(60.0, tripleThenDouble.apply(10.0), delta, "double(triple(10)) = 60, GOOD");
        assertEquals(-60.0, doubleThenTriple.apply(-10.0), delta, "triple(double(-10)) = -60, GOOD");
        assertEquals(-60.0, tripleThenDouble.apply(-10.0), delta, "double(triple(-10)) = -60, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку Array + Array")
    void testArrayWithArrayAndThen(){
        //f(x) = 4x - 5
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {-5.0, -1.0, 3.0, 7.0, 11.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = x + 10
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {10.0, 11.0, 12.0, 13.0, 14.0};
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(x2, y2);

        MathFunction composition = f.andThen(g);
        // f(g(x)) = 4(x + 10) - 5 = 4x + 35
        assertEquals(35.0, composition.apply(0.0), 1e-8, "f(g(0)) = f(10) = 35, GOOD");
        assertEquals(39.0, composition.apply(1.0), 1e-8, "f(g(1)) = f(11) = 39, GOOD");
        assertEquals(43.0, composition.apply(2.0), 1e-8, "f(g(2)) = f(12) = 43, GOOD");
        assertEquals(47.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(13) = 47, GOOD");
        assertEquals(51.0, composition.apply(4.0), 1e-8, "f(g(3)) = f(14) = 51, GOOD");

        assertEquals(36.0, composition.apply(0.25), 1e-8, "f(g(0.25)) = 36 интерполяция, GOOD");
        assertEquals(37.0, composition.apply(0.5), 1e-8, "f(g(0.5)) = 37 интерполяций, GOOD");
        assertEquals(38.0, composition.apply(0.75), 1e-8, "f(g(0.75)) = 38 интерполяций, GOOD");
        assertEquals(40.0, composition.apply(1.25), 1e-8, "f(g(1.25)) = 40 интерполяций, GOOD");
        assertEquals(41.0, composition.apply(1.5), 1e-8, "f(g(1.5)) = 41 интерполяций, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку Array + LinkedList")
    void testArrayWithLinkedListAndThen(){
        //f(x) = 4x - 5
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {-5.0, -1.0, 3.0, 7.0, 11.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x1, y1);

        //g(x) = x + 10
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {10.0, 11.0, 12.0, 13.0, 14.0};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        MathFunction composition = f.andThen(g);
        // f(g(x)) = 4(x + 10) - 5 = 4x + 35
        assertEquals(35.0, composition.apply(0.0), 1e-8, "f(g(0)) = f(10) = 35, GOOD");
        assertEquals(39.0, composition.apply(1.0), 1e-8, "f(g(1)) = f(11) = 39, GOOD");
        assertEquals(43.0, composition.apply(2.0), 1e-8, "f(g(2)) = f(12) = 43, GOOD");
        assertEquals(47.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(13) = 47, GOOD");
        assertEquals(51.0, composition.apply(4.0), 1e-8, "f(g(3)) = f(14) = 51, GOOD");

        assertEquals(36.0, composition.apply(0.25), 1e-8, "f(g(0.25)) = 36 интерполяция, GOOD");
        assertEquals(37.0, composition.apply(0.5), 1e-8, "f(g(0.5)) = 37 интерполяций, GOOD");
        assertEquals(38.0, composition.apply(0.75), 1e-8, "f(g(0.75)) = 38 интерполяций, GOOD");
        assertEquals(40.0, composition.apply(1.25), 1e-8, "f(g(1.25)) = 40 интерполяций, GOOD");
        assertEquals(41.0, composition.apply(1.5), 1e-8, "f(g(1.5)) = 41 интерполяций, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку LinkedList + LinkedList")
    void testLinkedListyWithLinkedListAndThen(){
        //f(x) = 4x - 5
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {-5.0, -1.0, 3.0, 7.0, 11.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = x + 10
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {10.0, 11.0, 12.0, 13.0, 14.0};
        LinkedListTabulatedFunction g = new LinkedListTabulatedFunction(x2, y2);

        MathFunction composition = f.andThen(g);
        // f(g(x)) = 4(x + 10) - 5 = 4x + 35
        assertEquals(35.0, composition.apply(0.0), 1e-8, "f(g(0)) = f(10) = 35, GOOD");
        assertEquals(39.0, composition.apply(1.0), 1e-8, "f(g(1)) = f(11) = 39, GOOD");
        assertEquals(43.0, composition.apply(2.0), 1e-8, "f(g(2)) = f(12) = 43, GOOD");
        assertEquals(47.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(13) = 47, GOOD");
        assertEquals(51.0, composition.apply(4.0), 1e-8, "f(g(3)) = f(14) = 51, GOOD");

        assertEquals(36.0, composition.apply(0.25), 1e-8, "f(g(0.25)) = 36 интерполяция, GOOD");
        assertEquals(37.0, composition.apply(0.5), 1e-8, "f(g(0.5)) = 37 интерполяций, GOOD");
        assertEquals(38.0, composition.apply(0.75), 1e-8, "f(g(0.75)) = 38 интерполяций, GOOD");
        assertEquals(40.0, composition.apply(1.25), 1e-8, "f(g(1.25)) = 40 интерполяций, GOOD");
        assertEquals(41.0, composition.apply(1.5), 1e-8, "f(g(1.5)) = 41 интерполяций, GOOD");
    }

    @Test
    @DisplayName("Тест на проверку LinkedList + Array")
    void testLinkedListyWithArrayAndThen(){
        //f(x) = 4x - 5
        double[] x1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y1 = {-5.0, -1.0, 3.0, 7.0, 11.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x1, y1);

        //g(x) = x + 10
        double[] x2 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] y2 = {10.0, 11.0, 12.0, 13.0, 14.0};
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(x2, y2);

        MathFunction composition = f.andThen(g);
        // f(g(x)) = 4(x + 10) - 5 = 4x + 35
        assertEquals(35.0, composition.apply(0.0), 1e-8, "f(g(0)) = f(10) = 35, GOOD");
        assertEquals(39.0, composition.apply(1.0), 1e-8, "f(g(1)) = f(11) = 39, GOOD");
        assertEquals(43.0, composition.apply(2.0), 1e-8, "f(g(2)) = f(12) = 43, GOOD");
        assertEquals(47.0, composition.apply(3.0), 1e-8, "f(g(3)) = f(13) = 47, GOOD");
        assertEquals(51.0, composition.apply(4.0), 1e-8, "f(g(3)) = f(14) = 51, GOOD");

        assertEquals(36.0, composition.apply(0.25), 1e-8, "f(g(0.25)) = 36 интерполяция, GOOD");
        assertEquals(37.0, composition.apply(0.5), 1e-8, "f(g(0.5)) = 37 интерполяций, GOOD");
        assertEquals(38.0, composition.apply(0.75), 1e-8, "f(g(0.75)) = 38 интерполяций, GOOD");
        assertEquals(40.0, composition.apply(1.25), 1e-8, "f(g(1.25)) = 40 интерполяций, GOOD");
        assertEquals(41.0, composition.apply(1.5), 1e-8, "f(g(1.5)) = 41 интерполяций, GOOD");
    }
}