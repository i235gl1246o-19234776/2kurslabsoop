package operations;

import functions.MathFunction;

public class RightSteppingDifferentialOperator extends SteppingDifferentialOperator<MathFunction>{

    public RightSteppingDifferentialOperator(double step) {
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
                return (f.apply(x + step)- f.apply(x)) / step;
            }
        };
    }
}
