package functions;
import exception.ArrayIsNotSortedException;
import exception.DifferentLengthOfArraysException;

public abstract class AbstractTabulatedFunction implements TabulatedFunction {

    protected int count;

    protected abstract int floorIndexOfX(double x);
    protected abstract double extrapolateLeft(double x);
    protected abstract double extrapolateRight(double x);
    protected abstract double interpolate(double x, int floorIndex);

    protected double interpolate(double x, double leftX, double rightX,
                                 double leftY, double rightY) {
        return leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX);
    }

    @Override
    public double apply(double x) {
        if (x < getX(0)) {
            return extrapolateLeft(x);
        }

        if (x > getX(count - 1)) {
            return extrapolateRight(x);
        }

        int exactIndex = indexOfX(x);
        if (exactIndex != -1) {
            return getY(exactIndex);
        }

        int floorIndex = floorIndexOfX(x);
        return interpolate(x, floorIndex);
    }
    protected static void checkLengthIsTheSame(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new DifferentLengthOfArraysException("Длины ОШИБКА");
        }
    }
    protected static void checkSorted(double[] xValues) {
        for (int i = 1; i < xValues.length; i++) {
            if (xValues[i] <= xValues[i - 1]) {
                throw new ArrayIsNotSortedException("Массив должен возрастать");
            }
        }
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getClass().getSimpleName())
                .append(" size = ")
                .append(getCount())
                .append("\n");

        for (Point point : this){
            stringBuilder.append("[")
                    .append(point.x)
                    .append("; ")
                    .append(point.y)
                    .append("]\n");
        }

        return stringBuilder.toString();
    }


    //Абстрактные методы из интерфейса TabulatedFunction
    public abstract int getCount();
    public abstract double getX(int index);
    public abstract double getY(int index);
    public abstract void setY(int index, double value);
    public abstract int indexOfX(double x);
    public abstract int indexOfY(double y);
    public abstract double leftBound();
    public abstract double rightBound();
}