package model.dto.response;

public class CompositeFunctionResponseDTO {
    private Long id;
    private String functionName;

    public CompositeFunctionResponseDTO(Long id, String functionName) {
        this.id = id;
        this.functionName = functionName;
    }

    // getters
    public Long getId() { return id; }
    public String getFunctionName() { return functionName; }
}