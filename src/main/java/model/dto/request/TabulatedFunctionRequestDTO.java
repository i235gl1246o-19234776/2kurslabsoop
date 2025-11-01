package model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Objects;
import java.util.logging.Logger;

public class TabulatedFunctionRequestDTO {
    private static final Logger logger = Logger.getLogger(TabulatedFunctionRequestDTO.class.getName());

    @NotNull(message = "ID функции обязателен")
    @Positive(message = "ID функции должен быть положительным числом")
    private Long functionId;

    @NotNull(message = "Значение X обязательно")
    private Double xVal;

    @NotNull(message = "Значение Y обязательно")
    private Double yVal;

    public TabulatedFunctionRequestDTO() {
        logger.fine("Создан пустой TabulatedFunctionRequestDTO");
    }

    public TabulatedFunctionRequestDTO(Long functionId, Double xVal, Double yVal) {
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
        logger.info("Создан TabulatedFunctionRequestDTO для функции: " + functionId + " (x=" + xVal + ", y=" + yVal + ")");
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
        TabulatedFunctionRequestDTO that = (TabulatedFunctionRequestDTO) o;
        return Objects.equals(functionId, that.functionId) &&
                Objects.equals(xVal, that.xVal) &&
                Objects.equals(yVal, that.yVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionId, xVal, yVal);
    }

    @Override
    public String toString() {
        return String.format("TabulatedFunctionRequestDTO{functionId=%d, x=%.2f, y=%.2f}",
                functionId, xVal, yVal);
    }
}