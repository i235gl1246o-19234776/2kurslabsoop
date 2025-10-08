package functions;

public class CompositeFunction implements MathFunction{

    private final MathFunction firstFunction;
    private final MathFunction secondFunction;

    public CompositeFunction(MathFunction firstFunction, MathFunction secondFunction){
        this.firstFunction = firstFunction;
        this.secondFunction = secondFunction;
    }

    @Override
    public double apply(double x){
        double interResult = secondFunction.apply(x);//g(x)
        return firstFunction.apply(interResult);//f(g(x))
    }

}
