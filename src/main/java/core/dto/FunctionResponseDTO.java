package core.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResponseDTO {
    private Long functionId;
    private String functionName;
    private String typeFunction;
    private Double xVal;
    private Double yVal;
    private String userName;
    private Integer operationsTypeId;
}