package model.dto;

import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;

public class TabulatedFunctionDifferentialOperator {
    private final TabulatedFunctionFactory factory;

    public TabulatedFunctionDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public TabulatedFunction derive(TabulatedFunction f) {
        if (f == null || f.getCount() < 2) {
            throw new IllegalArgumentException("Функция должна содержать минимум 2 точки");
        }

        double[] xValues = new double[f.getCount() - 2];
        double[] yValues = new double[f.getCount() - 2];

        // Центральная разность: f'(x_i) ≈ (f(x_{i+1}) - f(x_{i-1})) / (x_{i+1} - x_{i-1})
        for (int i = 1; i < f.getCount() - 1; i++) {
            double xPrev = f.getX(i - 1);
            double xNext = f.getX(i + 1);
            double yPrev = f.getY(i - 1);
            double yNext = f.getY(i + 1);

            double dx = xNext - xPrev;
            if (Math.abs(dx) < 1e-12) {
                throw new ArithmeticException("Нулевой шаг по X между точками: " + xPrev + " и " + xNext);
            }

            xValues[i - 1] = f.getX(i);
            yValues[i - 1] = (yNext - yPrev) / dx;
        }

        return factory.create(xValues, yValues);
    }
}