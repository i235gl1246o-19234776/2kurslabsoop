package functions;

import exception.InterpolationException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.Serializable;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable {

    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private Node currNode = head;
            private int visitedCount = 0;

            @Override
            public boolean hasNext() {
                return visitedCount < count;
            }

            @Override
            public Point next() {
                if (!hasNext()){
                    throw new NoSuchElementException("Элементов больше нет, ы");
                }

                Point point = new Point(currNode.x, currNode.y);
                currNode = currNode.next;
                visitedCount++;
                return point;
            }
        };
    }

    private static final long serialVersionUID = 8720509751947980864L;

    public static class Node implements Serializable{
        private static final long serialVersionUID = -5791952235901357690L;

        public Node next;
        public Node prev;
        public double x;
        public double y;

        public Node(double x, double y) {
            this.x = x;
            this.y = y;
            this.next = null;
            this.prev = null;
        }
    }

    private Node head;
    private int count;

    //конструктор с массивами значений
    public LinkedListTabulatedFunction(double[] xVal, double[] yVal){
        if (xVal.length < 2) {
            throw new IllegalArgumentException("Длина таблицы должна быть не менее 2 точек");
        }
        checkLengthIsTheSame(xVal, yVal);
        checkSorted(xVal);

        this.count = 0;
        this.head = null;

        for(int i = 0; i < xVal.length;i++){
            addNode(xVal[i], yVal[i]);
        }
    }

    //конструктор с дискретизацией функции
    public LinkedListTabulatedFunction(MathFunction s, double xFrom, double xTo, int count){
        if (count < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }

        if (xFrom > xTo){
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        this.count = 0;
        this.head = null;

        if(xFrom == xTo){
            double yVal = s.apply(xFrom);
            for(int i = 0; i < count; i++){
                addNode(xFrom, yVal);
            }
        }else{
            double step = (xTo - xFrom) / (count - 1);
            for(int i = 0; i < count; i++){
                double x = xFrom + i * step;
                double y = s.apply(x);
                addNode(x, y);
            }
        }
    }

    private void addNode(double x, double y){
        Node nwNode = new Node(x, y);

        if (head == null){
            head = nwNode;
            head.next = head;
            head.prev = head;
        }else{
            Node last = head.prev;

            last.next = nwNode;
            nwNode.prev = last;

            nwNode.next = head;
            head.prev = nwNode;
        }

        count++;
    }

    private Node getNode(int index){
        if (index < 0 || index >= count){
            throw new IndexOutOfBoundsException("Индекс: " + index + ", Размер: " + count);
        }

        if(index < count / 2){
            Node curr = head;
            for(int i = 0; i < index; i++){
                curr = curr.next;
            }
            return curr;
        } else{
            Node curr = head.prev;
            for (int i = count - 1; i > index; i--){
                curr = curr.prev;
            }
            return curr;
        }
    }

    @Override
    protected int floorIndexOfX(double x) {
        if(head == null){
            throw new IllegalArgumentException("Список пуст, ы");
        }
        if (x < head.x) {
            throw new IllegalArgumentException("x = " + x + " меньше левой границы " + head.x);
        }
        if(x > head.prev.x){
            throw new IllegalArgumentException("x = " + x + " больше левой границы " + count);
        }

        Node curr = head;
        for(int i = 0; i < count - 1; i++){
            if(x < curr.next.x){
                return i;
            }
            curr = curr.next;
        }
        return count - 1;
    }

    @Override
    protected double extrapolateLeft(double x) {
        if (count < 2) throw new IllegalArgumentException("Экстраполяция на <2 элемента");
        int floorIndex = 0;
        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;
        return interpolate(x, leftNode.x, rightNode.x, leftNode.y, rightNode.y);
    }

    @Override
    protected double extrapolateRight(double x) {
        if (count < 2) throw new IllegalArgumentException("Экстраполяция на <2 элемента");
        int floorIndex = count-2;
        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;
        return interpolate(x, leftNode.x, rightNode.x, leftNode.y, rightNode.y);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {


        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;

        if (x < leftNode.x || x > rightNode.x) {
            throw new InterpolationException(
                    String.format("x = %.3f находится вне интервала интерполирования [%.3f, %.3f]",
                            x, leftNode.x, rightNode.x)
            );
        }
        return interpolate(x, leftNode.x, rightNode.x, leftNode.y, rightNode.y);
    }

    @Override
    public int getCount(){
        return count;
    }

    @Override
    public double getX(int index){
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        return getNode(index).x;
    }

    @Override
    public double getY(int index){
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double val){
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        getNode(index).y = val;
    }

    @Override
    public int indexOfX(double x){
        if (head == null){
            throw new IllegalStateException("Список пуст, ы");
        }

        Node curr = head;
        for(int i = 0; i < count; i++){
            if(Math.abs(curr.x - x) < 1e-10){
                return i;
            }
            curr = curr.next;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y){
        if (head == null){
            return -1;
        }

        Node curr = head;
        for(int i = 0; i < count; i++){
            if(Math.abs(curr.y - y) < 1e-10){
                return i;
            }
            curr = curr.next;
        }
        return -1;
    }

    @Override
    public double leftBound() {
        if(head == null){
            throw new IllegalArgumentException("Список пуст, ы");
        }
        return head.x;
    }

    @Override
    public double rightBound() {
        if(head == null){
            throw new IllegalArgumentException("Список пуст, ы");
        }
        return head.prev.x;
    }

    protected Node floorNodeOfX(double x){
        if (head == null){
            throw new IllegalStateException("Список пуст, ы");
        }
        if (x < head.x) {
            throw new IllegalArgumentException("x = " + x + " меньше левой границы " + head.x);
        }

        if(x > head.prev.x){
            throw new IllegalArgumentException("x = " + x + " больше левой границы " + head.prev);
        }

        Node curr = head;
        for(int i = 0; i < count - 1; i++){
            if(x < curr.next.x){
                return curr;
            }
            curr = curr.next;
        }

        return head.prev;
    }

    @Override
    public double apply(double x) {
        //if (count == 1) throw new IllegalStateException(); ??
        if(x < leftBound()){
            return extrapolateLeft(x);
        }else if(x > rightBound()){
            return extrapolateRight(x);
        }else{
            Node floorNode = floorNodeOfX(x);
            if(Math.abs(floorNode.x - x) < 1e-10){
                return floorNode.y;
            }

            return interpolate(x, floorNode.x, floorNode.next.x, floorNode.y, floorNode.next.y);
        }
    }

    @Override
    public void insert(double x, double y) {
        if (head == null) {
            addNode(x, y);
            return;
        }

        Node curr = head;
        do {
            if (Math.abs(curr.x - x) < 1e-10) {
                curr.y = y;
                return;
            }
            if (x < curr.x) {
                Node newNode = new Node(x, y);

                Node prevNode = curr.prev;
                prevNode.next = newNode;
                newNode.prev = prevNode;

                newNode.next = curr;
                curr.prev = newNode;

                if (curr == head) {
                    head = newNode;
                }

                count++;
                return;
            }

            curr = curr.next;
        } while (curr != head);

        Node last = head.prev;
        Node newNode = new Node(x, y);

        last.next = newNode;
        newNode.prev = last;

        newNode.next = head;
        head.prev = newNode;

        count++;
    }


    @Override
    public void remove(int index) {
        if (head == null) {
            throw new IllegalStateException("Список пуст, ы");
        }

        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }

        Node nodeToRemove = getNode(index);

        if (count == 1) {
            head = null;
            count--;
            return;
        }

        Node prevNode = nodeToRemove.prev;
        Node nextNode = nodeToRemove.next;

        prevNode.next = nextNode;
        nextNode.prev = prevNode;

        if (nodeToRemove == head) {
            head = nextNode;
        }
        count--;
    }
    public Node getHead(){
        return head;
    }
}