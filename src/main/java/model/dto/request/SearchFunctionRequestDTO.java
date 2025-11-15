package model.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchFunctionRequestDTO {
    private String userName;
    private String functionName;
    private String typeFunction;
    private Double xVal;
    private Double yVal;
    private Long operationsTypeId;
    private String sortBy;
    private String sortOrder;
    private Integer page;
    private Integer size;

}