package functions;

import java.util.Iterator;

public class UnmodifiableTabulatedFunction implements TabulatedFunction {

    private final TabulatedFunction tabulatedFunction;

    public UnmodifiableTabulatedFunction(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            throw new IllegalArgumentException("TabulatedFunction не 0");
        }
        this.tabulatedFunction = tabulatedFunction;
    }

    @Override
    public double apply(double x) {
        return tabulatedFunction.apply(x);
    }

    @Override
    public int getCount() {
        return tabulatedFunction.getCount();
    }

    @Override
    public double getX(int index) {
        return tabulatedFunction.getX(index);
    }

    @Override
    public double getY(int index) {
        return tabulatedFunction.getY(index);
    }

    @Override
    public void setY(int index, double value) {
        throw new UnsupportedOperationException("Нельзя это использовать");
    }

    @Override
    public int indexOfX(double x) {
        return tabulatedFunction.indexOfX(x);
    }

    @Override
    public int indexOfY(double y) {
        return tabulatedFunction.indexOfY(y);
    }

    @Override
    public double leftBound() {
        return 0;
    }

    @Override
    public double rightBound() {
        return 0;
    }


    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private final Iterator<Point> internalIterator = tabulatedFunction.iterator();

            @Override
            public boolean hasNext() {
                return internalIterator.hasNext();
            }

            @Override
            public Point next() {
                Point point = internalIterator.next();
                return point;
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Нельзя так делать");
            }
        };
    }
}