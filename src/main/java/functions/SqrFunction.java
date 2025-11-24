package functions;

@FunctionDescription(
        displayName = "Квадратичная функция",
        displayPriority = 1
)

public class SqrFunction implements MathFunction{

    @Override
    public double apply(double x){
        return Math.pow(x,2);
    }

}
