package operations;

import functions.MathFunction;
import java.util.concurrent.RecursiveTask;

public class TrapezoidalIntegral extends RecursiveTask<Double> {
    private final MathFunction func;
    private final double a, b;
    private final long n;
    private final long THRESHOLD;

    public TrapezoidalIntegral(MathFunction func, double a, double b, long n) {
        this.func = func;
        this.a = a;
        this.b = b;
        this.n = n;
        this.THRESHOLD = Math.max(1000, n / 10); // Порог для переключения на последовательное вычисление
    }

    @Override
    protected Double compute() {
        if (n <= THRESHOLD) {
            return computeTrapezoidalSequential();
        } else {
            // Делим интервал пополам для параллельного вычисления
            double mid = (a + b) / 2.0;
            long halfN = n / 2;

            TrapezoidalIntegral left = new TrapezoidalIntegral(func, a, mid, halfN);
            TrapezoidalIntegral right = new TrapezoidalIntegral(func, mid, b, halfN);

            left.fork();
            double rightResult = right.compute();
            double leftResult = left.join();

            return leftResult + rightResult;
        }
    }

    private double computeTrapezoidalSequential() {
        double h = (b - a) / n;
        double sum = 0.5 * (func.apply(a) + func.apply(b)); // Первое и последнее значение с коэффициентом 0.5

        // Суммируем промежуточные значения
        for (int i = 1; i < n; i++) {
            double x = a + i * h;
            sum += func.apply(x);
        }

        return sum * h;
    }
}