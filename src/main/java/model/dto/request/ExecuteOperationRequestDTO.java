package model.dto.request;

public class ExecuteOperationRequestDTO {
    private Long functionIdA;
    private Long functionIdB;
    private String operation; // "add", "subtract", etc.
    private String factoryType; // "array" или "linked-list"

    // геттеры и сеттеры
    public Long getFunctionIdA() { return functionIdA; }
    public void setFunctionIdA(Long functionIdA) { this.functionIdA = functionIdA; }
    public Long getFunctionIdB() { return functionIdB; }
    public void setFunctionIdB(Long functionIdB) { this.functionIdB = functionIdB; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getFactoryType() { return factoryType; }
    public void setFactoryType(String factoryType) { this.factoryType = factoryType; }
}
