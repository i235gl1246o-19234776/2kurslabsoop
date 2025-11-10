package model.entity;

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
}