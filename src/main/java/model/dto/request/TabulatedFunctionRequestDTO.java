package model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TabulatedFunctionRequestDTO {

    @NotNull(message = "ID функции обязателен")
    @Positive(message = "ID функции должен быть положительным числом")
    private Long functionId;

    @NotNull(message = "Значение X обязательно")
    private Double xVal;

    @NotNull(message = "Значение Y обязательно")
    private Double yVal;

    // Пустой конструктор
    public TabulatedFunctionRequestDTO() {
    }

    // Конструктор с параметрами
    public TabulatedFunctionRequestDTO(Long functionId, Double xVal, Double yVal) {
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    // Геттеры
    public Long getFunctionId() {
        return functionId;
    }

    public Double getXVal() {
        return xVal;
    }

    public Double getYVal() {
        return yVal;
    }

    // Сеттеры
    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public void setXVal(Double xVal) {
        this.xVal = xVal;
    }

    public void setYVal(Double yVal) {
        this.yVal = yVal;
    }

    @Override
    public String toString() {
        return String.format("TabulatedFunctionRequestDTO{functionId=%d, x=%.2f, y=%.2f}",
                functionId, xVal, yVal);
    }
}