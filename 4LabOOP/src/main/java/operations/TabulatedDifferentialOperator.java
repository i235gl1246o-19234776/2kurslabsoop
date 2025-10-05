package operations;

import concurrent.SynchronizedTabulatedFunction;
import functions.TabulatedFunction;
import functions.Point;
import functions.factory.TabulatedFunctionFactory;
import functions.factory.ArrayTabulatedFunctionFactory;


public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {

    private TabulatedFunctionFactory factory;

    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("TabulatedFunctionFactory не может быть равна null");
        }
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        int count = points.length;

        double[] xValues = new double[count];
        double[] yValues = new double[count];

        for (int i = 0; i < count; i++) {
            xValues[i] = points[i].x;
        }

        if (count == 2) {
            double h = points[1].x - points[0].x;
            double deriv = (points[1].y - points[0].y) / h;
            yValues[0] = deriv;
            yValues[1] = deriv;
        } else {
            double h = points[1].x - points[0].x;

            yValues[0] = (-3 * points[0].y + 4 * points[1].y - points[2].y) / (2 * h);

            for (int i = 1; i < count - 1; i++) {
                yValues[i] = (points[i + 1].y - points[i - 1].y) / (2 * h);
            }

            yValues[count - 1] = (3 * points[count - 1].y - 4 * points[count - 2].y + points[count - 3].y) / (2 * h);
        }

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction deriveSynchronously(TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Функция не может быть null");
        }

        SynchronizedTabulatedFunction syncFunction;
        if (function instanceof SynchronizedTabulatedFunction) {
            syncFunction = (SynchronizedTabulatedFunction) function;
        } else {
            syncFunction = new SynchronizedTabulatedFunction(function);
        }

        return syncFunction.doSynchronously(this::derive);
    }
}