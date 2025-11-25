package functions;

import functions.factory.TabulatedFunctionFactory;

public class InterpolationOperations {

    public static TabulatedFunction interpolateLinear(TabulatedFunction source,
                                                      double start, double end, int pointCount,
                                                      TabulatedFunctionFactory factory) {
        validateParameters(source, start, end, pointCount);

        double[] xValues = new double[pointCount];
        double[] yValues = new double[pointCount];

        double step = (end - start) / (pointCount - 1);

        for (int i = 0; i < pointCount; i++) {
            xValues[i] = start + i * step;
            yValues[i] = linearInterpolate(source, xValues[i]);
        }

        return factory.create(xValues, yValues);
    }

    private static double linearInterpolate(TabulatedFunction func, double x) {
        int n = func.getCount();

        // Если x за пределами области определения, возвращаем крайние значения
        if (x <= func.getX(0)) return func.getY(0);
        if (x >= func.getX(n - 1)) return func.getY(n - 1);

        // Находим интервал, содержащий x
        int i = findIntervalIndex(func, x);

        double x0 = func.getX(i);
        double x1 = func.getX(i + 1);
        double y0 = func.getY(i);
        double y1 = func.getY(i + 1);

        // Линейная интерполяция: y = y0 + (y1 - y0) * (x - x0) / (x1 - x0)
        return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
    }

    /**
     * Интерполяция Ньютона (вперед) для одной точки
     */


    private static int findIntervalIndex(TabulatedFunction func, double x) {
        int n = func.getCount();

        // Бинарный поиск для эффективности
        int left = 0;
        int right = n - 2;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (func.getX(mid) <= x && func.getX(mid + 1) >= x) {
                return mid;
            } else if (func.getX(mid) > x) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        // Если не нашли точный интервал, возвращаем ближайший
        if (x < func.getX(0)) return 0;
        return n - 2;
    }

    private static void validateParameters(TabulatedFunction source, double start, double end, int pointCount) {
        if (source == null) {
            throw new IllegalArgumentException("Исходная функция не может быть null");
        }

        if (source.getCount() < 2) {
            throw new IllegalArgumentException("Исходная функция должна содержать хотя бы 2 точки");
        }

        if (start >= end) {
            throw new IllegalArgumentException("Начало интервала должно быть меньше конца");
        }

        if (pointCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }

        // Проверяем равномерность сетки для методов Ньютона
        if (!isUniformGrid(source)) {
            throw new IllegalArgumentException("Методы Ньютона требуют равномерную сетку узлов");
        }
    }

    /**
     * Проверяет, является ли сетка узлов равномерной
     */
    private static boolean isUniformGrid(TabulatedFunction func) {
        int n = func.getCount();
        if (n < 2) return true;

        double h = func.getX(1) - func.getX(0);
        double tolerance = 1e-10;

        for (int i = 1; i < n - 1; i++) {
            double currentStep = func.getX(i + 1) - func.getX(i);
            if (Math.abs(currentStep - h) > tolerance) {
                return false;
            }
        }

        return true;
    }

    private static double[][] computeSplineCoefficients(TabulatedFunction func) {
        int n = func.getCount();
        double[] x = new double[n];
        double[] y = new double[n];

        for (int i = 0; i < n; i++) {
            x[i] = func.getX(i);
            y[i] = func.getY(i);
        }

        double[][] coefficients = new double[4][n - 1]; // a, b, c, d для каждого сегмента
        double[] h = new double[n - 1];
        double[] alpha = new double[n - 1];
        double[] l = new double[n];
        double[] mu = new double[n];
        double[] z = new double[n];

        // Вычисляем h[i] = x[i+1] - x[i]
        for (int i = 0; i < n - 1; i++) {
            h[i] = x[i + 1] - x[i];
        }

        // Вычисляем alpha[i]
        for (int i = 1; i < n - 1; i++) {
            alpha[i] = (3 / h[i]) * (y[i + 1] - y[i]) - (3 / h[i - 1]) * (y[i] - y[i - 1]);
        }

        // Прогонка (Thomas algorithm)
        l[0] = 1;
        mu[0] = 0;
        z[0] = 0;

        for (int i = 1; i < n - 1; i++) {
            l[i] = 2 * (x[i + 1] - x[i - 1]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l[i];
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
        }

        l[n - 1] = 1;
        z[n - 1] = 0;

        // Вычисляем коэффициенты c, b, d
        double[] c = new double[n];
        double[] b = new double[n - 1];
        double[] d = new double[n - 1];

        c[n - 1] = 0;

        for (int j = n - 2; j >= 0; j--) {
            c[j] = z[j] - mu[j] * c[j + 1];
            b[j] = (y[j + 1] - y[j]) / h[j] - h[j] * (c[j + 1] + 2 * c[j]) / 3;
            d[j] = (c[j + 1] - c[j]) / (3 * h[j]);
        }

        // Сохраняем коэффициенты
        for (int i = 0; i < n - 1; i++) {
            coefficients[0][i] = y[i]; // a
            coefficients[1][i] = b[i]; // b
            coefficients[2][i] = c[i]; // c
            coefficients[3][i] = d[i]; // d
        }

        return coefficients;
    }

    /**
     * Интерполяция сплайнами для одной точки
     */
}