package model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Data
@NoArgsConstructor
@Log
public class TabulatedFunctionResponseDTO {

    private Long id;
    private Long functionId;
    private Double xVal;
    private Double yVal;

    {
        log.fine("Создан пустой TabulatedFunctionResponseDTO");
    }

    public TabulatedFunctionResponseDTO(Long id, Long functionId, Double xVal, Double yVal) {
        this.id = id;
        this.functionId = functionId;
        this.xVal = xVal;
        this.yVal = yVal;
        log.info("Создан TabulatedFunctionResponseDTO: id=" + id + " (functionId: " + functionId + ")");
    }

    @Override
    public String toString() {
        return String.format("TabulatedFunctionResponseDTO{id=%d, functionId=%d, x=%.2f, y=%.2f}",
                id, functionId, xVal, yVal);
    }
}