package functions;

import java.util.Iterator;

public class MockTabulatedFunction extends AbstractTabulatedFunction{
    private final double x0;
    private final double x1;
    private final double y0;
    private final double y1;

    public MockTabulatedFunction(double x0, double x1, double y0, double y1){
        if(x0 >= x1){
            throw new IllegalArgumentException("x0 > x1, NOT GOOD");
        }
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.count = 2;
    }

    @Override
    protected int floorIndexOfX(double x){
        if(x < x0){
            return 0; // все больше заданного - 0
        }else if(x > x1){
            return count; //все меньше заданного - count
        } else if(x == x0){
            return 0;
        } else if(x == x1){
            return 1;
        } else{
            return 0;
        }
    }

    @Override
    protected double extrapolateLeft(double x){
        return interpolate(x, x0, x1, y0, y1);
    }

    @Override
    protected double extrapolateRight(double x){
        return interpolate(x, x0, x1, y0, y1);
    }

    @Override
    protected double interpolate(double x, int floorIndex){
        return interpolate(x,x0,x1,y0,y1);
    }

    @Override
    public int getCount(){
        return count;
    }

    @Override
    public double getX(int index) {
        if (index == 0) return x0;
        if (index == 1) return x1;
        throw new IllegalArgumentException("Индекс выходит за пределы "+ index);
    }

    @Override
    public double getY(int index) {
        if (index == 0) return y0;
        if (index == 1) return y1;
        throw new IllegalArgumentException("Индекс выходит за пределы "+ index);
    }

    @Override
    public void setY(int index, double value) {
        throw new UnsupportedOperationException("Mock неизменяемый");
    }

    @Override
    public int indexOfX(double x) {
        if (Math.abs(x - x0) <= 1e-10) return 0;
        if (Math.abs(x - x1) <= 1e-10) return 1;
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        if (Math.abs(y - y0) <= 1e-10) return 0;
        if (Math.abs(y - y1) <= 1e-10) return 1;
        return -1;
    }

    @Override
    public double leftBound() {
        return x0;
    }

    @Override
    public double rightBound() {
        return x1;
    }

    @Override
    public Iterator<Point> iterator() {
        return null;
    }
}
