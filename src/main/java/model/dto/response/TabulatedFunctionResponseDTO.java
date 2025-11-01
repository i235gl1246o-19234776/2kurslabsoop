package model.dto.response;

import java.util.Objects;
import java.util.logging.Logger;

public class TabulatedFunctionResponseDTO {
    private static final Logger logger = Logger.getLogger(TabulatedFunctionResponseDTO.class.getName());

    private Long id;
    private Long functionId;
    private Double xVal;
    private Double yVal;

    public TabulatedFunctionResponseDTO() {
        logger.fine("Создан пустой TabulatedFunctionResponseDTO");
    }

    public TabulatedFunctionResponseDTO(Long id, Long functionId, Double xVal, Double yVal) {
        this.id = id;
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
        logger.info("Создан TabulatedFunctionResponseDTO: id=" + id + " (functionId: " + functionId + ")");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        logger.fine("Установлен id: " + id);
        this.id = id;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        logger.fine("Установлен functionId: " + functionId);
        this.functionId = functionId;
    }

    public Double getXVal() {
        return xVal;
    }

    public void setXVal(Double xVal) {
        logger.fine("Установлено xVal: " + xVal);
        this.xVal = xVal;
    }

    public Double getYVal() {
        return yVal;
    }

    public void setYVal(Double yVal) {
        logger.fine("Установлено yVal: " + yVal);
        this.yVal = yVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TabulatedFunctionResponseDTO that = (TabulatedFunctionResponseDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(functionId, that.functionId) &&
                Objects.equals(xVal, that.xVal) &&
                Objects.equals(yVal, that.yVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, functionId, xVal, yVal);
    }

    @Override
    public String toString() {
        return String.format("TabulatedFunctionResponseDTO{id=%d, functionId=%d, x=%.2f, y=%.2f}",
                id, functionId, xVal, yVal);
    }
}