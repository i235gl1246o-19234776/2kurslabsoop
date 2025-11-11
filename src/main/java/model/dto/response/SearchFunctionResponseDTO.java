package model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFunctionResponseDTO {
    private List<FunctionResponseDTO> functions;
    private int total;
    private Long operationsTypeId;
    private Long functionId;
    private String functionName;
    private String typeFunction;
    private Double xVal;
    private Double yVal;
    private String userName;
    private String operationTypeName;

    public SearchFunctionResponseDTO(List<FunctionResponseDTO> functions, int total) {
        this.functions = functions;
        this.total = total;
    }
}