package model.dto.request;

public class CompositeFunctionRequestDTO {
    private String functionName;
    private String functionExpression;

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getFunctionExpression() { return functionExpression; }
    public void setFunctionExpression(String functionExpression) { this.functionExpression = functionExpression; }
}