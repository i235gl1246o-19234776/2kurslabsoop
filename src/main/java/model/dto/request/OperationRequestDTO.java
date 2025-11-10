package model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Data
@NoArgsConstructor
@Log
public class OperationRequestDTO {

    @NotNull(message = "ID функции обязателен")
    @Positive(message = "ID функции должен быть положительным числом")
    private Long functionId;

    @NotNull(message = "Тип операции обязателен")
    @Positive(message = "Тип операции должен быть положительным числом")
    private Integer operationsTypeId;

    {
        log.fine("Создан пустой OperationRequestDTO");
    }

    public OperationRequestDTO(Long functionId, Integer operationsTypeId) {
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
        log.info("Создан OperationRequestDTO для функции: " + functionId + " (тип: " + operationsTypeId + ")");
    }
}