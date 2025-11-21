// src/main/java/functions/factory/ArrayTabulatedFunctionFactory.java
package functions.factory;

import functions.ArrayTabulatedFunction;
import functions.MathFunction;
import functions.TabulatedFunction;

public class ArrayTabulatedFunctionFactory implements TabulatedFunctionFactory{
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues){
        return new ArrayTabulatedFunction(xValues, yValues);
    }

    // НОВЫЙ метод
    @Override
    public TabulatedFunction create(MathFunction mathFunction, double xFrom, double xTo, int count) {
        // Вызываем конструктор ArrayTabulatedFunction, который принимает MathFunction
        return new ArrayTabulatedFunction(mathFunction, xFrom, xTo, count);
    }
}