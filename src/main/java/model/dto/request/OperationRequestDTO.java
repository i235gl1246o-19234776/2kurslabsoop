package model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Objects;
import java.util.logging.Logger;

public class OperationRequestDTO {
    private static final Logger logger = Logger.getLogger(OperationRequestDTO.class.getName());

    @NotNull(message = "ID функции обязателен")
    @Positive(message = "ID функции должен быть положительным числом")
    private Long functionId;

    @NotNull(message = "Тип операции обязателен")
    @Positive(message = "Тип операции должен быть положительным числом")
    private Integer operationsTypeId;

    public OperationRequestDTO() {
        logger.fine("Создан пустой OperationRequestDTO");
    }

    public OperationRequestDTO(Long functionId, Integer operationsTypeId) {
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
        logger.info("Создан OperationRequestDTO для функции: " + functionId + " (тип: " + operationsTypeId + ")");
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        logger.fine("Установлен functionId: " + functionId);
        this.functionId = functionId;
    }

    public Integer getOperationsTypeId() {
        return operationsTypeId;
    }

    public void setOperationsTypeId(Integer operationsTypeId) {
        logger.fine("Установлен operationsTypeId: " + operationsTypeId);
        this.operationsTypeId = operationsTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationRequestDTO that = (OperationRequestDTO) o;
        return Objects.equals(functionId, that.functionId) &&
                Objects.equals(operationsTypeId, that.operationsTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionId, operationsTypeId);
    }

    @Override
    public String toString() {
        return "OperationRequestDTO{functionId=" + functionId + ", operationsTypeId=" + operationsTypeId + "}";
    }
}
