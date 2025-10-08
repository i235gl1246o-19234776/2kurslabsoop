package functions;
// математическая функция численно находит корни уравнениея f(x) = 0
public class NewtonMethod implements MathFunction {
    private final MathFunction f;
    private final MathFunction df;
    private final double e;
    private final int maxIterations;

    public NewtonMethod(MathFunction f, MathFunction df) {
        this(f, df, 1.23e-6, 1000);
    }

    public NewtonMethod(MathFunction f, MathFunction df, double e, int maxIterations) {
        this.f = f;
        this.df = df;
        this.e = e;
        this.maxIterations = maxIterations;
    }

    @Override
    public double apply(double x0) {
        double x = x0;

        for (int i = 0; i < maxIterations; i++) {
            double fx = f.apply(x);
            double dfx = df.apply(x);

            // Проверка на нулевую производную
            if (Math.abs(dfx) < e) {
                throw new ArithmeticException("Производная близка к нулю. Метод Ньютона не работвет");
            }

            double xNew = x - fx / dfx;

            // Проверка сходимости
            if (Math.abs(xNew - x) < e) {
                return xNew;
            }

            x = xNew;
        }

        throw new RuntimeException("Метод Ньютона не сошелся за " + maxIterations + " итераций");
    }
}