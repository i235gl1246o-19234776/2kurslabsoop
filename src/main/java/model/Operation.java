package model;

public class Operation {
    private Long id;
    private Long functionId;
    private Integer operationsTypeId;

    public Operation() {}

    public Operation(Long functionId, Integer operationsTypeId) {
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public Integer getOperationsTypeId() { return operationsTypeId; }
    public void setOperationsTypeId(Integer operationsTypeId) { this.operationsTypeId = operationsTypeId; }

    @Override
    public String toString() {
        return String.format("Operation{id=%d, functionId=%d, typeId=%d}",
                id, functionId, operationsTypeId);
    }
}