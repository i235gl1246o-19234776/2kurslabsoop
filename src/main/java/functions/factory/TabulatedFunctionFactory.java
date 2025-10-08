package functions.factory;

import functions.StrictTabulatedFunction;
import functions.TabulatedFunction;
import functions.UnmodifiableTabulatedFunction;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);

    default TabulatedFunction createStrict(double[] xValues, double[] yValues){
        if (xValues == null || yValues == null) {
            throw new IllegalArgumentException("Должны быть непустые массивы");
        }
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Длины ОШИБКА");
        }
        TabulatedFunction baseFunction = create(xValues, yValues);
        return new StrictTabulatedFunction(baseFunction);
    }
    default TabulatedFunction createUnmodifiable(double[] xValues, double[] yValues) {
        if (xValues == null || yValues == null) {
            throw new IllegalArgumentException("Должны быть непустые массивы");
        }
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Длины ОШИБКА");
        }
        return new UnmodifiableTabulatedFunction(create(xValues, yValues));
    }

    default TabulatedFunction createStrictUnmodifiable(double[] xValues, double[] yValues){
        TabulatedFunction baseFunction = create(xValues, yValues);
        TabulatedFunction strictFunction = new StrictTabulatedFunction(baseFunction);
        return new UnmodifiableTabulatedFunction(strictFunction);
    }

}
