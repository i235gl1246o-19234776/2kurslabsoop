package core.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationDto {

    private Long id;
    private Long functionId;
    private Integer operationsTypeId;
}
