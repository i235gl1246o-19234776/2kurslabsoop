package model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operation {
    private Long id;
    private Long functionId;
    private Integer operationsTypeId;

    public Operation(Long functionId, Integer operationsTypeId) {
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
    }
}