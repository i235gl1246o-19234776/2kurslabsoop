package core.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationRequestDto {
    private Long functionId;
    private Integer operationsTypeId;
}
