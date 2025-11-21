// src/main/java/functions/factory/LinkedListTabulatedFunctionFactory.java
package functions.factory;

import functions.LinkedListTabulatedFunction;
import functions.MathFunction;
import functions.TabulatedFunction;

public class LinkedListTabulatedFunctionFactory implements TabulatedFunctionFactory{
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues){
        return new LinkedListTabulatedFunction(xValues, yValues);
    }

    // НОВЫЙ метод
    @Override
    public TabulatedFunction create(MathFunction mathFunction, double xFrom, double xTo, int count) {
        // Вызываем конструктор LinkedListTabulatedFunction, который принимает MathFunction
        return new LinkedListTabulatedFunction(mathFunction, xFrom, xTo, count);
    }
}