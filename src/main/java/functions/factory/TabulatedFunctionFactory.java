// src/main/java/functions/factory/TabulatedFunctionFactory.java
package functions.factory;

import functions.MathFunction;
import functions.TabulatedFunction;
import functions.StrictTabulatedFunction;
import functions.UnmodifiableTabulatedFunction;

public interface TabulatedFunctionFactory {
    // Существующий метод
    TabulatedFunction create(double[] xValues, double[] yValues);

    // НОВЫЙ метод - второй способ создания
    TabulatedFunction create(MathFunction mathFunction, double xFrom, double xTo, int count);

    // Существующие default методы остаются
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