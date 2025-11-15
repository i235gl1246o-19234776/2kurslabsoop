package model.dto.response;

import java.util.logging.Logger;

public class OperationResponseDTO {

    private static final Logger log = Logger.getLogger(OperationResponseDTO.class.getName());

    private Long id;
    private Long functionId;
    private Integer operationsTypeId;

    // Пустой конструктор
    public OperationResponseDTO() {
        log.fine("Создан пустой OperationResponseDTO");
    }

    // Конструктор с параметрами
    public OperationResponseDTO(Long id, Long functionId, Integer operationsTypeId) {
        this.id = id;
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
        log.info("Создан OperationResponseDTO: id=" + id + " (functionId: " + functionId + ")");
    }

    // Геттеры
    public Long getId() {
        return id;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public Integer getOperationsTypeId() {
        return operationsTypeId;
    }

    // Сеттеры
    public void setId(Long id) {
        this.id = id;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public void setOperationsTypeId(Integer operationsTypeId) {
        this.operationsTypeId = operationsTypeId;
    }
}