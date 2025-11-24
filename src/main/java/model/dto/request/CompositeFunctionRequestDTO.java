package model.dto.request;

public class CompositeFunctionRequestDTO {
    private String functionName;
    private String technicalName;
    private String description;
    private String functionExpression;

    public String getFunctionName() {
        return functionName;
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public String getDescription() {
        return description;
    }

    public String getFunctionExpression() {
        return functionExpression;
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

    public void setFunctionExpression(String functionExpression) {
        this.functionExpression = functionExpression;
    }

    // Геттеры и сеттеры
}