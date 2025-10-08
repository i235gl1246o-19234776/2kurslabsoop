package operations;

import functions.TabulatedFunction;
import functions.Point;
import java.util.Iterator;
import functions.factory.*;
import exception.*;

public class TabulatedFunctionOperationService {

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            throw new IllegalArgumentException("TabulatedFunction cannot be null");
        }

        int count = tabulatedFunction.getCount();
        Point[] points = new Point[count];

        int i = 0;
        Iterator<Point> iterator = tabulatedFunction.iterator();
        while (iterator.hasNext()) {
            Point originalPoint = iterator.next();
            points[i] = new Point(originalPoint.x, originalPoint.y);
            i++;
        }

        return points;
    }
    private TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }
        this.factory = factory;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }


    public void setFactory(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }
        this.factory = factory;
    }

    @FunctionalInterface
    private interface BiOperation {
        double apply(double u, double v);
    }

    private TabulatedFunction doOperation(
            TabulatedFunction a,
            TabulatedFunction b,
            BiOperation operation
    ) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("TabulatedFunction cannot be null");
        }

        int countA = a.getCount();
        int countB = b.getCount();

        if (countA != countB) {
            throw new InconsistentFunctionsException(
                    "Размеры не совпадают: "+ countA + " и"+ countB);
        }
        Point[] pointsA = asPoints(a);
        Point[] pointsB = asPoints(b);

        double[] xValues = new double[countA];
        double[] yValues = new double[countA];

        for (int i = 0; i < countA; i++) {
            double xA = pointsA[i].x;
            double xB = pointsB[i].x;

            if (xA != xB) {
                throw new InconsistentFunctionsException(
                        "X не совпадают!");
            }

            xValues[i] = xA;
            yValues[i] = operation.apply(pointsA[i].y, pointsB[i].y);
        }

        return factory.create(xValues, yValues);
    }
    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u + v);
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u - v);
    }

    public TabulatedFunction multiply(TabulatedFunction a, TabulatedFunction b){
        return doOperation(a, b, (u,v) -> u * v);
    }

    public TabulatedFunction divide(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> {
            if (v == 0.0) {
                throw new ArithmeticException("Делить на ноль нельзя, айайай"); // Более точное сообщение можно получить в цикле
            }
            return u / v;
        });
    }

}