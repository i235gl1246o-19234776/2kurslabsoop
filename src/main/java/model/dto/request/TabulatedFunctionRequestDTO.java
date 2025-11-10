package model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Data
@NoArgsConstructor
@Log
public class TabulatedFunctionRequestDTO {

    @NotNull(message = "ID функции обязателен")
    @Positive(message = "ID функции должен быть положительным числом")
    private Long functionId;

    @NotNull(message = "Значение X обязательно")
    private Double xVal;

    @NotNull(message = "Значение Y обязательно")
    private Double yVal;

    {
        log.fine("Создан пустой TabulatedFunctionRequestDTO");
    }

    public TabulatedFunctionRequestDTO(Long functionId, Double xVal, Double yVal) {
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
        log.info("Создан TabulatedFunctionRequestDTO для функции: " + functionId + " (x=" + xVal + ", y=" + yVal + ")");
    }

    @Override
    public String toString() {
        return String.format("TabulatedFunctionRequestDTO{functionId=%d, x=%.2f, y=%.2f}",
                functionId, xVal, yVal);
    }
}