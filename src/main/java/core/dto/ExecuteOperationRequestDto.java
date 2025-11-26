package core.dto;

import lombok.Data;

@Data
public class ExecuteOperationRequestDto {
    private Long functionIdA;
    private Long functionIdB;
    private String operation; // "add", "subtract", etc.
    private String factoryType; // "array" или "linked-list"
}