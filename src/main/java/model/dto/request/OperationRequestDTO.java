package model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.logging.Logger;

public class OperationRequestDTO {

    private static final Logger log = Logger.getLogger(OperationRequestDTO.class.getName());

    @NotNull(message = "ID функции обязателен")
    @Positive(message = "ID функции должен быть положительным числом")
    private Long functionId;

    @NotNull(message = "Тип операции обязателен")
    @Positive(message = "Тип операции должен быть положительным числом")
    private Integer operationsTypeId;

    // Пустой конструктор
    public OperationRequestDTO() {
        log.fine("Создан пустой OperationRequestDTO");
    }

    // Конструктор с параметрами
    public OperationRequestDTO(Long functionId, Integer operationsTypeId) {
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
        log.info("Создан OperationRequestDTO для функции: " + functionId + " (тип: " + operationsTypeId + ")");
    }

    // Геттеры
    public Long getFunctionId() {
        return functionId;
    }

    public Integer getOperationsTypeId() {
        return operationsTypeId;
    }

    // Сеттеры
    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public void setOperationsTypeId(Integer operationsTypeId) {
        this.operationsTypeId = operationsTypeId;
    }
}