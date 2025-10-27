package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FunctionPoint {
    private Long id;
    private Long functionId;
    private BigDecimal xVal;
    private Double yVal;
    private LocalDateTime computedAt;

    public FunctionPoint() {}

    public FunctionPoint(Long functionId, BigDecimal xVal, Double yVal) {
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    public FunctionPoint(Long functionId, double xVal, double yVal) {
        this.functionId = functionId;
        this.xVal = BigDecimal.valueOf(xVal);
        this.yVal = yVal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }

    public BigDecimal getXVal() { return xVal; }
    public void setXVal(BigDecimal xVal) { this.xVal = xVal; }

    public void setXVal(double xVal) {
        this.xVal = BigDecimal.valueOf(xVal);
    }

    public Double getYVal() { return yVal; }
    public void setYVal(Double yVal) { this.yVal = yVal; }

    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }

    public double getXValueAsDouble() {
        return xVal != null ? xVal.doubleValue() : 0.0;
    }

    public boolean isApproximatelyEqual(FunctionPoint other, double tolerance) {
        if (other == null) return false;

        double xDiff = Math.abs(this.getXValueAsDouble() - other.getXValueAsDouble());
        double yDiff = Math.abs(this.yVal - other.yVal);

        return xDiff <= tolerance && yDiff <= tolerance;
    }

    @Override
    public String toString() {
        return String.format("FunctionPoint{id=%d, functionId=%d, x=%.6f, y=%.6f, computedAt=%s}",
                id, functionId, xVal != null ? xVal.doubleValue() : 0.0,
                yVal, computedAt != null ? computedAt.toString() : "null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionPoint that = (FunctionPoint) o;

        if (Math.abs(this.getXValueAsDouble() - that.getXValueAsDouble()) > 1e-12) return false;
        if (Math.abs(this.yVal - that.yVal) > 1e-12) return false;

        return functionId.equals(that.functionId);
    }

    @Override
    public int hashCode() {
        int result = functionId.hashCode();
        result = 31 * result + (xVal != null ? xVal.hashCode() : 0);
        result = 31 * result + (yVal != null ? yVal.hashCode() : 0);
        return result;
    }
}