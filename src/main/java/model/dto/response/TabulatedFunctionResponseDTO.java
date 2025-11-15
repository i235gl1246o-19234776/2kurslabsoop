package model.dto.response;

public class TabulatedFunctionResponseDTO {

    private Long id;
    private Long functionId;
    private Double xVal;
    private Double yVal;

    // Пустой конструктор
    public TabulatedFunctionResponseDTO() {
    }

    // Конструктор с параметрами
    public TabulatedFunctionResponseDTO(Long id, Long functionId, Double xVal, Double yVal) {
        this.id = id;
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    // Геттеры
    public Long getId() {
        return id;
    }

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
    public void setId(Long id) {
        this.id = id;
    }

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
        return String.format("TabulatedFunctionResponseDTO{id=%d, functionId=%d, x=%.2f, y=%.2f}",
                id, functionId, xVal, yVal);
    }
}