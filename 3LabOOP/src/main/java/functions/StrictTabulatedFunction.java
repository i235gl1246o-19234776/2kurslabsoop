package functions;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class StrictTabulatedFunction implements TabulatedFunction{
    private TabulatedFunction function;

    public StrictTabulatedFunction(TabulatedFunction function){
        this.function = function;
    }

    @Override
    public double apply(double x){
        int index = function.indexOfX(x);
        if (index == -1){
            throw new UnsupportedOperationException(
                    "Я запрещаю интерполировать. Значение x = " + x + "не найдено в таблице"
            );
        }
        return function.getY(index);
    }

    @Override
    public int getCount(){
        return function.getCount();
    }

    @Override
    public double getX(int index){
        return function.getX(index);
    }

    @Override
    public double getY(int index){
        return function.getY(index);
    }

    @Override
    public void setY(int index, double value){
        function.setY(index, value);
    }

    @Override
    public int indexOfX(double x){
        return function.indexOfX(x);
    }

    @Override
    public int indexOfY(double y){
        return function.indexOfY(y);
    }

    @Override
    public double leftBound(){
        return function.leftBound();
    }

    @Override
    public double rightBound(){
        return function.rightBound();
    }

    @Override
    public Iterator<Point> iterator(){
        return function.iterator();
    }

}