package model.dto.response;

import model.servlet.CompositeFunctionStructure;

public class CompositeFunctionResponseDTO {
    private Long functionId;
    private String functionName;
    private String technicalName;
    private String description;
    private CompositeFunctionStructure structure;

    // Геттеры и сеттеры

    public CompositeFunctionResponseDTO(Long functionId, String functionName, String technicalName,
                                        String description, CompositeFunctionStructure structure) {
        this.functionId = functionId;
        this.functionName = functionName;
        this.technicalName = technicalName;
        this.description = description;
        this.structure = structure;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public String getDescription() {
        return description;
    }

    public CompositeFunctionStructure getStructure() {
        return structure;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStructure(CompositeFunctionStructure structure) {
        this.structure = structure;
    }
}