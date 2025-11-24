package functions;

@FunctionDescription(
        displayName = "Идентити функцион",
        displayPriority = 2
)

public class IdentityFunction implements MathFunction {

    @Override
    public double apply(double x){
        return x;
    }

}
