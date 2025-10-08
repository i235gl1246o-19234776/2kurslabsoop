package functions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MathFunctionTest {

    @Test
    @DisplayName("Композиция g.andThen(f): f(g(x)) = (x² + 1), где g(x)=x², f(x)=x+1 — проверка результата")
    public void testAndThenSingle() {
        // f(x) = x + 1
        MathFunction f = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        // g(x) = x²
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        // composite = f.andThen(g) → f(g(x)) = (x²) + 1
        MathFunction composite = f.andThen(g);

        assertEquals(2.0, composite.apply(1.0), 1e-10, "g(1)=1, f(1)=2 → (1²)+1=2 GOOD");
        assertEquals(5.0, composite.apply(2.0), 1e-10, "g(2)=4, f(4)=5 → (2²)+1=5 GOOD");
    }

    @Test
    @DisplayName("Цепочка из трёх функций: h.andThen(g).andThen(f) = h(g(f(x))) = (2*(x+1))² — проверка последовательной композиции")
    public void testAndThenChainThree() {
        // f(x) = x + 1
        MathFunction f = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        // g(x) = 2x
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return 2 * x;
            }
        };

        // h(x) = x²
        MathFunction h = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        // composite = h.andThen(g).andThen(f) → h(g(f(x))) = (2*(x+1))²
        MathFunction composite = h.andThen(g).andThen(f);

        assertEquals(16.0, composite.apply(1.0), 1e-10, "f(1)=2 → g(2)=4 → h(4)=16 GOOD");
        assertEquals(4.0, composite.apply(0.0), 1e-10, "f(0)=1 → g(1)=2 → h(2)=4 GOOD");
    }

    @Test
    @DisplayName("Композиция exp.andThen(log): log(exp(x)) = x — проверка обратных функций")
    public void testAndThenWithExpAndLog() {
        // exp(x) = e^x
        MathFunction exp = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.exp(x);
            }
        };

        // log(x) = ln(x)
        MathFunction log = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.log(x);
            }
        };

        // composed = exp.andThen(log) → log(exp(x)) = x
        MathFunction composed = exp.andThen(log);

        assertEquals(0.0, composed.apply(0.0), 1e-10, "log(exp(0)) = log(1) = 0 GOOD");
        assertEquals(1.0, composed.apply(1.0), 1e-10, "log(exp(1)) ≈ 1 GOOD");
        assertEquals(2.0, composed.apply(2.0), 1e-10, "log(exp(2)) ≈ 2 GOOD");
    }

    @Test
    @DisplayName("Порядок композиции важен: g.andThen(f) ≠ f.andThen(g) — проверка некоммутативности")
    public void testAndThenOrderMatters() {
        // f(x) = x + 1
        MathFunction f = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        // g(x) = x²
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        // g.andThen(f) = f(g(x)) = x² + 1
        MathFunction gf = g.andThen(f);
        assertEquals(4.0, gf.apply(1.0), 1e-10, "g(1)=1, f(1)=2 → 1²+1=2 GOOD");

        // f.andThen(g) = f(g(x)) = (x+1)²
        MathFunction fg = f.andThen(g);
        assertEquals(2.0, fg.apply(1.0), 1e-10, "f(1)=2, g(2)=4 → (1+1)²=4 GOOD");

        assertNotEquals(fg.apply(1.0), gf.apply(1.0), "Результаты разные: порядок композиции влияет GOOD");
    }

    @Test
    @DisplayName("Композиция с identity-функцией не меняет результат — identity как нейтральный элемент")
    public void testEmptyChain() {
        // identity(x) = x
        MathFunction identity = new MathFunction() {
            @Override
            public double apply(double x) {
                return x;
            }
        };

        // f(x) = x + 1
        MathFunction f = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        // identity.andThen(f) = f(identity(x)) = f(x)
        MathFunction result1 = identity.andThen(f);
        assertEquals(3.0, result1.apply(2.0), 1e-10, "identity(2)=2, f(2)=3 → 2+1=3 GOOD");

        // f.andThen(identity) = identity(f(x)) = f(x)
        MathFunction result2 = f.andThen(identity);
        assertEquals(3.0, result2.apply(2.0), 1e-10, "f(2)=3, identity(3)=3 → 2+1=3 GOOD");
    }

    @Test
    @DisplayName("Сложная цепочка: f.andThen(g).andThen(h) = f(g(h(x))) = ((2x)+3)² — проверка глубокой композиции")
    public void testDirectComposition() {
        // h(x) = 2x
        MathFunction h = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * 2;
            }
        };

        // g(x) = x + 3
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 3;
            }
        };

        // f(x) = x²
        MathFunction f = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        // composite = f.andThen(g).andThen(h) → f(g(h(x))) = (2x + 3)²
        MathFunction composite = f.andThen(g).andThen(h);

        assertEquals(25.0, composite.apply(1.0), 1e-10, "h(1)=2 → g(2)=5 → f(5)=25 GOOD");
        assertEquals(49.0, composite.apply(2.0), 1e-10, "h(2)=4 → g(4)=7 → f(7)=49 GOOD");
    }
}