package core.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TabulatedFunctionDto {
    // Геттеры и сеттеры
    private Long id;
    private Long functionId;
    private Double xVal;
    private Double yVal;

    // Конструкторы
    public TabulatedFunctionDto() {}

    public TabulatedFunctionDto(Long id, Long functionId, Double xVal, Double yVal) {
        this.id = id;
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
    }

}
