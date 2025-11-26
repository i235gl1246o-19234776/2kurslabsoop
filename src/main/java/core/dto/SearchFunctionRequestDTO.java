package core.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFunctionRequestDTO {
    private String userName;
    private String functionName;
    private String typeFunction;
    private Double xVal;
    private Double yVal;
    private Integer operationsTypeId;
    private String sortBy;
    private String sortOrder;
    private Integer page;
    private Integer size;
}