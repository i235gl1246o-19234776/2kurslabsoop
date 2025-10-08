package operations;

import functions.MathFunction;

public class TestParralellism2000 {
    private static final double NANOS_TO_MILLIS = 1_000_000.0;

    public static void main(String[] args) {
        MathFunction complexFunc = x -> 1.0 / (1.0 + x * x) ;
        double a = -5.0;
        double b = 5.0;
        long n = 6_000_000_000L;

        int[] parallelismLevels = {1, 2, 4, 8, 16};

        System.out.println("\n====Сравнение параллелизма====");
        System.out.printf("n = %d%n", n);

        double baseResult = 0;
        long baseTime = 0;

        for (int parallelism : parallelismLevels) {
            Object[] result = ParallelIntegrator.integrateWithFixedPool(complexFunc, a, b, n, parallelism);
            double integralValue = (Double) result[0];
            long durationNanos = (Long) result[1];
            double durationMillis = durationNanos / NANOS_TO_MILLIS;

            if (parallelism == 1) {
                baseTime = durationNanos;
            }

            System.out.printf("Parallelism %d: %.3f мс, результат: %.10f%n",
                    parallelism, durationMillis, integralValue);
        }

        System.out.printf("Ускорение (1 поток как база): %.2f%%%n",
                (baseTime > 0 ? (double) baseTime / baseTime * 100 : 0));
    }
}
