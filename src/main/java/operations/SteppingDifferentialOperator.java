package operations;

import functions.MathFunction;

public class SteppingDifferentialOperator<T extends MathFunction> implements DifferentialOperator<T> {

    protected double step;

    public void setStep(double step) {
        if (step <= 0 || Double.isInfinite(step) || Double.isNaN(step)) {
            throw new IllegalArgumentException("Недопустимое значение шага");
        }
        this.step = step;
    }

    public double getStep() {
        return step;
    }

    public SteppingDifferentialOperator(double step) {
        if (step <= 0 || Double.isInfinite(step) || Double.isNaN(step)) {
            throw new IllegalArgumentException("Недопустимое значение шага");
        }
        this.step = step;
    }

    @Override
    public T derive(T function) {
        return null;
    }
}
