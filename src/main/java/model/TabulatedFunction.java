package model;

public class TabulatedFunction {
    private Long id;
    private Long functionId;
    private Double xVal;
    private Double yVal;

    public TabulatedFunction() {}

    public TabulatedFunction(Long functionId, Double xVal, Double yVal) {
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public Double getXVal() { return xVal; }
    public void setXVal(Double xVal) { this.xVal = xVal; }
    public Double getYVal() { return yVal; }
    public void setYVal(Double yVal) { this.yVal = yVal; }

    @Override
    public String toString() {
        return String.format("TabulatedFunction{id=%d, functionId=%d, x=%.2f, y=%.2f}",
                id, functionId, xVal, yVal);
    }
}