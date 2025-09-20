package operations;

import functions.MathFunction;

public class LeftSteppingDifferentialOperator extends SteppingDifferentialOperator<MathFunction> {
    public LeftSteppingDifferentialOperator(double step) {
        super(step);
    }
    @Override
    public MathFunction derive(MathFunction f) {
        if (f == null) {
            throw new IllegalArgumentException("Функция не пуста");
        }
        return new MathFunction() {
            @Override
            public double apply(double x) {
                return (f.apply(x) - f.apply(x - step)) / step;
            }
        };
    }
}
