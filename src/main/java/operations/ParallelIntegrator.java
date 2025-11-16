package operations;
import functions.MathFunction;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ParallelIntegrator {
    public static double integrate(MathFunction func, double a, double b, long n) {
        if (a == b) {
            return 0;
        }
        if (n <= 0) {
            throw new IllegalArgumentException("n должно быть положительным");
        }

        SimpsonIntegral task;
        if (a > b) {
            task = new SimpsonIntegral(func, b, a, n);
            return -ForkJoinPool.commonPool().invoke(task);
        } else {
            task = new SimpsonIntegral(func, a, b, n);
            return ForkJoinPool.commonPool().invoke(task);
        }
    }

    public record IntegrationResult(double result, long duration) {} //создает неизменяемый объект с геттерами, equals(), hashCode() и toString()

    public static IntegrationResult integrateWithFixedPool(MathFunction func, double a, double b, long n, int parallelism) {
        if (a == b) {
            return new IntegrationResult(0.0, 0L);
        }
        if (n <= 0) {
            throw new IllegalArgumentException("n должно быть положительным");
        }
        if (parallelism <= 0) {
            throw new IllegalArgumentException("parallelism должно быть положительным");
        }

        long startTime = System.nanoTime();
        ForkJoinPool customPool = new ForkJoinPool(parallelism);
        try {
            SimpsonIntegral task;
            double result;

            if (a > b) {
                task = new SimpsonIntegral(func, b, a, n);
                result = -customPool.invoke(task);
            } else {
                task = new SimpsonIntegral(func, a, b, n);
                result = customPool.invoke(task);
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            return new IntegrationResult(result, duration);
        } finally {
            customPool.shutdown();
            try {
                if (!customPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    customPool.shutdownNow(); // принудительно завершить
                }
            } catch (InterruptedException e) {
                customPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
