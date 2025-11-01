package model;

public class Function {
    private Long id;
    private Long userId;
    private String typeFunction;
    private String functionName;
    private String functionExpression;

    public Function() {}

    public Function(Long userId, String typeFunction, String functionName, String functionExpression) {
        this.userId = userId;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTypeFunction() { return typeFunction; }
    public void setTypeFunction(String typeFunction) { this.typeFunction = typeFunction; }
    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }
    public String getFunctionExpression() { return functionExpression; }
    public void setFunctionExpression(String functionExpression) { this.functionExpression = functionExpression; }

    @Override
    public String toString() {
        return String.format("Function{id=%d, userId=%d, type='%s', name='%s'}",
                id, userId, typeFunction, functionName);
    }
}