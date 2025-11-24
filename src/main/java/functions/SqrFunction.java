// src/main/java/functions/SqrFunction.java
package functions;

import core.utils.FunctionInfo;

@FunctionInfo(displayName = "Квадратичная функция", priority = 10)
public class SqrFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return Math.pow(x, 2);
    }

    public SqrFunction() {}
}