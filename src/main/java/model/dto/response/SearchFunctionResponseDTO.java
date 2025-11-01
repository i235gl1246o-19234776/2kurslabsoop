package model.dto.response;

import java.util.List;

public class SearchFunctionResponseDTO {
    private List<FunctionResponseDTO> functions;
    private int total;
    private Long operationsTypeId;

    public SearchFunctionResponseDTO() {}

    public SearchFunctionResponseDTO(List<FunctionResponseDTO> functions, int total) {
        this.functions = functions;
        this.total = total;
    }

    public List<FunctionResponseDTO> getFunctions() { return functions; }
    public void setFunctions(List<FunctionResponseDTO> functions) { this.functions = functions; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public Long getOperationsTypeId() { return operationsTypeId; }
    public void setOperationsTypeId(Long operationsTypeId) { this.operationsTypeId = operationsTypeId; }
}