package model.dto.response;

import java.util.Objects;
import java.util.logging.Logger;

public class OperationResponseDTO {
    private static final Logger logger = Logger.getLogger(OperationResponseDTO.class.getName());

    private Long id;
    private Long functionId;
    private Integer operationsTypeId;

    public OperationResponseDTO() {
        logger.fine("Создан пустой OperationResponseDTO");
    }

    public OperationResponseDTO(Long id, Long functionId, Integer operationsTypeId) {
        this.id = id;
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
        logger.info("Создан OperationResponseDTO: id=" + id + " (functionId: " + functionId + ")");
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
        OperationResponseDTO that = (OperationResponseDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(functionId, that.functionId) &&
                Objects.equals(operationsTypeId, that.operationsTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, functionId, operationsTypeId);
    }

    @Override
    public String toString() {
        return "OperationResponseDTO{id=" + id + ", functionId=" + functionId + ", operationsTypeId=" + operationsTypeId + "}";
    }
}