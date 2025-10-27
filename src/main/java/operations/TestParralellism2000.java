package operations;

import functions.MathFunction;

public class TestParralellism2000 {
    private static final double NANOS_TO_MILLIS = 1_000_000.0;

    public static void main(String[] args) {
        MathFunction complexFunc = x -> (1.0 + x * x)/ (1.0 + x * x * x * x) ;
        double a = -500.0;
        double b = 500.0;
        long n = 1000000009;

        int[] parallelismLevels = {1, 2, 4, 8, 16};

        System.out.println("\n====Сравнение параллелизма====");
        System.out.printf("n = %d%n", n);

        for (int parallelism : parallelismLevels) {
            ParallelIntegrator.IntegrationResult result = ParallelIntegrator.integrateWithFixedPool(complexFunc, a, b, n, parallelism);
            double integralValue = result.result();
            long durationNanos = result.duration();

            double durationMillis = durationNanos / NANOS_TO_MILLIS;

            System.out.printf("Parallelism %d: %.3f мс, результат: %.10f%n",
                    parallelism, durationMillis, integralValue);
        }
    }
}
