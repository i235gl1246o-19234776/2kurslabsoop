// src/main/java/functions/ZeroFunction.java
package functions;

import core.utils.FunctionInfo;

@FunctionInfo(displayName = "Нулевая функция", priority = 30)
public class ZeroFunction extends ConstantFunction {
    public ZeroFunction() {
        super(0.0);
    }
}