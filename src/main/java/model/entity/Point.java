package model.entity;

public class Point {
    private double xval;
    private double yval;

    public Point(double x, double y) {
        this.xval = x;
        this.yval = y;
    }

    public double getXval() { return xval; }
    public double getYval() { return yval; }
}