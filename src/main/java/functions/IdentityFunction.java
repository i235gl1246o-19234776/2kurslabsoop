// src/main/java/functions/IdentityFunction.java
package functions;

import core.utils.FunctionInfo;

@FunctionInfo(displayName = "Тождественная функция", priority = 20)
public class IdentityFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x;
    }

    public IdentityFunction() {}
}