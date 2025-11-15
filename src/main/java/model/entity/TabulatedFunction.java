/*package model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TabulatedFunction {
    private Long id;
    private Long functionId;
    private Double xVal;
    private Double yVal;

    public TabulatedFunction(Long functionId, Double xVal, Double yVal) {
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
    }
}*/
package model.entity;

public class TabulatedFunction {
    private Long id;
    private Long functionId;
    private Double xVal;
    private Double yVal;

    public TabulatedFunction() {
    }

    public TabulatedFunction(Long functionId, Double xVal, Double yVal) {
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    public TabulatedFunction(Long id, Long functionId, Double xVal, Double yVal) {
        this.id = id;
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public Double getXVal() {
        return xVal;
    }

    public void setXVal(Double xVal) {
        this.xVal = xVal;
    }

    public Double getYVal() {
        return yVal;
    }

    public void setYVal(Double yVal) {
        this.yVal = yVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TabulatedFunction that = (TabulatedFunction) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (functionId != null ? !functionId.equals(that.functionId) : that.functionId != null) return false;
        if (xVal != null ? !xVal.equals(that.xVal) : that.xVal != null) return false;
        return yVal != null ? yVal.equals(that.yVal) : that.yVal == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (functionId != null ? functionId.hashCode() : 0);
        result = 31 * result + (xVal != null ? xVal.hashCode() : 0);
        result = 31 * result + (yVal != null ? yVal.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TabulatedFunction{" +
                "id=" + id +
                ", functionId=" + functionId +
                ", xVal=" + xVal +
                ", yVal=" + yVal +
                '}';
    }
}