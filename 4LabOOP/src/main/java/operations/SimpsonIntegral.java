package operations;

import functions.MathFunction;

import java.util.concurrent.RecursiveTask;

public class SimpsonIntegral extends RecursiveTask<Double> {
    private final MathFunction func;
    private final double a, b;
    private final long n; // должно быть чётным
    private final long THRESHOLD;

    public SimpsonIntegral(MathFunction func, double a, double b, long n) {
        this.func = func;
        this.a = a;
        this.b = b;
        this.n = (n % 2 == 0) ? n : n + 1;
        this.THRESHOLD = Math.max(5000, n/50);
    }

    @Override
    protected Double compute() {
        if (n <= THRESHOLD) {
            return computeSimpsonSequential(a, b, n);
        } else {
            //Делим отрезок и число разбиений пополам
            double mid = a + (b - a) / 2.0;
            long halfN = n / 2;

            SimpsonIntegral left = new SimpsonIntegral(func, a, mid, halfN);
            SimpsonIntegral right = new SimpsonIntegral(func, mid, b, halfN);

            left.fork();
            double rightResult = right.compute();
            double leftResult = left.join();

            return leftResult + rightResult;
        }
    }

    private double computeSimpsonSequential(double a, double b, long n) {
        double h = (b - a) / n;
        double sum = func.apply(a) + func.apply(b);

        for (int i = 1; i < n; i++) {
            double x = a + i * h;
            sum += (i % 2 == 0) ? 2.0 * func.apply(x) : 4.0 * func.apply(x);
        }

        return sum * h / 3.0;
    }
}