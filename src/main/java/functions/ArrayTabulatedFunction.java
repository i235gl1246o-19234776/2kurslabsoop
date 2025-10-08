package functions;

import exception.InterpolationException;
import java.awt.*;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Arrays;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable {
    private static final long serialVersionUID = -2407695699800373971L;

    @JsonProperty("xVal")
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    private double[] xVal;

    @JsonProperty("yVal")
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    private double[] yVal;

    @JsonProperty("count")
    private int count;

    @JsonCreator
    public ArrayTabulatedFunction(@JsonProperty(value = "xVal") double[] xVal, @JsonProperty(value = "yVal") double[] yVal) {
        if (xVal.length < 2) {
            throw new IllegalArgumentException("Таблица должна содержать как минимум 2 точки");
        }
        checkLengthIsTheSame(xVal, yVal);
        checkSorted(xVal);

        this.count = xVal.length;
        this.xVal = Arrays.copyOf(xVal, count);
        this.yVal = Arrays.copyOf(yVal, count);
    }


    public ArrayTabulatedFunction(MathFunction s, double xFrom, double xTo, int count){

        if(count < 2){
            throw new IllegalArgumentException("Меньше 2х элементов");
        }

        this.count = count;
        this.xVal = new double[count];
        this.yVal = new double[count];

        if (xFrom == xTo){
            Arrays.fill(xVal, xFrom);
            Arrays.fill(yVal, s.apply(xFrom));
        }
        if (xFrom > xTo){
            double t = xFrom;
            xFrom = xTo;
            xTo = t;
        }

        //Дискретизация
        double step = (xTo - xFrom)/(count - 1);
        for(int i=0; i<count; i++){
            double x = xFrom + i*step;
            xVal[i] = x;
            yVal[i] = s.apply(x);
        }

    }

    @JsonProperty("xVal")
    public double[] getXVal() {
        return Arrays.copyOf(xVal, count);
    }

    @JsonProperty("yVal")
    public double[] getYVal() {
        return Arrays.copyOf(yVal, count);
    }


    @Override
    public double extrapolateLeft(double x) {
        if (count < 2) throw new IllegalArgumentException();
        int floorIndex = 0;
        return interpolate(x, xVal[floorIndex], xVal[floorIndex+1], yVal[floorIndex], yVal[floorIndex+1]);
    }

    @Override
    public double extrapolateRight(double x) {
        if (count < 2) throw new IllegalArgumentException();
        int floorIndex = count - 2;
        return interpolate(x, xVal[floorIndex], xVal[floorIndex+1], yVal[floorIndex], yVal[floorIndex+1]);
    }

    @Override
    public double interpolate(double x, int floorIndex) {
        if (count == 1) {
            return yVal[0];
        }

        if (floorIndex < 0 || floorIndex >= count - 1) {
            throw new IndexOutOfBoundsException("Аут оф индекс: " + floorIndex);

        }

        double x1 = xVal[floorIndex];
        double x2 = xVal[floorIndex + 1];
        double y1 = yVal[floorIndex];
        double y2 = yVal[floorIndex + 1];

        if (x < x1 || x > x2) {
            throw new InterpolationException(
                    "Значение x = " + x + " не находится в интервале [" + x1 + ", " + x2 + "]");
        }

        return interpolate(x, x1, x2, y1, y2);
    }

    @Override
    public int getCount(){
        return count;
    }

    @Override
    public double getX(int index){
        if (index < 0 || index >= count){
            throw new IllegalArgumentException("Index: " + index + ", Size: " + count);
        }
        return xVal[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        return yVal[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index: " + index + ", Size: " + count);
        }
        yVal[index] = value;
    }

    @Override
    public int indexOfX(double x) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(xVal[i] - x) < 1e-10) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(yVal[i] - y) < 1e-10) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public double leftBound() {
        if (count == 0) {
            throw new IndexOutOfBoundsException("Нет точек в функции: невозможно определить левую границу");
        }
        return xVal[0];
    }

    @Override
    public double rightBound() {
        if (count == 0) {
            throw new IndexOutOfBoundsException("Нет точек в функции: невозможно определить правую границу");
        }
        return xVal[count-1];
    }

    @Override
    public CompositeFunction andThen(MathFunction afterFunction) {
        return super.andThen(afterFunction);
    }

    //должен возвращать индекс наибольшего элемента массива, который меньше или равен заданному x
    @Override
    public int floorIndexOfX(double x) {
        if (x < leftBound()) {
            throw new IllegalArgumentException("Значение x = " + x + " меньше левой границы таблицы ");
        }
        if (x >  rightBound()) {
            throw new IllegalArgumentException("Значение x = " + x + " больше правой границы таблицы ");
        }

        for (int i = 0; i < count - 1; i++) {
            if (x < xVal[i+1]) { // Разбейте на две строки
                return i;
            }
        }
        return count - 1;
    }

    @Override
    public double apply(double x) {
        if (x < leftBound()) {
            return extrapolateLeft(x);
        } else if (x > rightBound()) {
            return extrapolateRight(x);
        } else {
            int index = indexOfX(x);
            if (index != -1) {
                return getY(index);
            } else {
                return interpolate(x, floorIndexOfX(x));
            }
        }
    }

    @Override
    public void insert(double x, double y) {
        int existingIndex = indexOfX(x);
        if (existingIndex != -1) {
            yVal[existingIndex] = y;
            return;
        }

        int insertIndex = 0;
        while (insertIndex < count && xVal[insertIndex] < x) {
            insertIndex++;
        }

        if (count >= xVal.length) {
            int newCapacity = (int) (xVal.length) + 1;
            xVal = Arrays.copyOf(xVal, newCapacity);
            yVal = Arrays.copyOf(yVal, newCapacity);
        }

        if (insertIndex < count) {
            System.arraycopy(xVal, insertIndex, xVal, insertIndex + 1, count - insertIndex);
            System.arraycopy(yVal, insertIndex, yVal, insertIndex + 1, count - insertIndex);
        }

        xVal[insertIndex] = x;
        yVal[insertIndex] = y;
        count++;
    }

    @Override
    public void remove(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Индекс: " + index + ", размер: " + count);
        }

        for (int i = index; i < count - 1; i++) {
            xVal[i] = xVal[i + 1];
            yVal[i] = yVal[i + 1];
        }

        //Уменьшаем количество точек
        count--;
    }
    public double[] getxVal() {
        return Arrays.copyOf(xVal, count);
    }
    public double[] getyVal() {
        return Arrays.copyOf(yVal, count);
    }

    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < count;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("Элементов больше нет, ы");
                }
                Point point = new Point(xVal[i], yVal[i]);
                i++;
                return point;
            }
        };
    }

}