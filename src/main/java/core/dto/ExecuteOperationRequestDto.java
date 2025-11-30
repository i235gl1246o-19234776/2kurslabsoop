package core.dto;

import lombok.*;

@Setter
@Getter
public class ExecuteOperationRequestDto {
    private Long functionIdA;
    private Long functionIdB;
    private String operation;
    private String factoryType;

}
