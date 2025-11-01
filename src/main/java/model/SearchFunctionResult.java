package model;

public class SearchFunctionResult {
    private Long functionId;
    private Long userId;
    private String userName;
    private String functionName;
    private String functionExpression;
    private String typeFunction;

    public SearchFunctionResult(Long functionId, Long userId, String userName,
                                String functionName, String functionExpression, String typeFunction) {
        this.functionId = functionId;
        this.userId = userId;
        this.userName = userName;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
        this.typeFunction = typeFunction;
    }

    public Long getFunctionId() { return functionId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getFunctionName() { return functionName; }
    public String getFunctionExpression() { return functionExpression; }
    public String getTypeFunction() { return typeFunction; }
}