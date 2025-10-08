package functions;

public class RungeKuttaFunction implements MathFunction{

    private final MathFunction dEquation;
    private final double x0;
    private final double y0;
    private final double step;

    //Конструктор
    public RungeKuttaFunction(MathFunction dEquation, double x0, double y0, double step){
        this.dEquation = dEquation;
        this.x0 = x0;
        this.y0 = y0;
        this.step = step;
    }

    //Переопределение метода apply
    @Override
    public double apply(double x){
        if (x == x0){//Если x == начальной точке, то возвращаем все изначальный y
            return y0;
        }

        //Считаем кол-во шагов
        int steps = (int) Math.round((x - x0)/step);
        double currX = x0;
        double currY = y0;

        for(int i = 0; i < steps; i++){//для каждого шага вычисляем расчет шага
            currY = rungeKuttaStep(currX, currY, step);
            currX += step;
        }

        return currY;
    }

    //Метод, который вычисляет шаг, зависящий от текущих данных
    private double rungeKuttaStep(double x, double y, double h){
        double k1 = h * dEquation.apply(x);//Наклон в начале интервала
        double k2 = h * dEquation.apply(x + h/2);//Наклон в середине (через k1)
        double k3 = h * dEquation.apply(x + h/2);//Другой наклон в середине(через k2)
        double k4 = h * dEquation.apply(x + h);//Наклон в конце интервала

        return y + (k1 + 2*k2 + 2*k3 + k4) / 6;
    }


}
