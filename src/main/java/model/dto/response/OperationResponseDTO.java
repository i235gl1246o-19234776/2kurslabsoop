package model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Data
@NoArgsConstructor
@Log
public class OperationResponseDTO {

    private Long id;
    private Long functionId;
    private Integer operationsTypeId;

    {
        log.fine("Создан пустой OperationResponseDTO");
    }

    public OperationResponseDTO(Long id, Long functionId, Integer operationsTypeId) {
        this.id = id;
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
        log.info("Создан OperationResponseDTO: id=" + id + " (functionId: " + functionId + ")");
    }
}