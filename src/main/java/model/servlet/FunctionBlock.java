package model.servlet;

public class FunctionBlock {
    private String type;
    private Long functionId;
    private String name;
    private String technicalName;
    private String operation;


    public String getType() {
        return type;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public String getName() {
        return name;
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public String getOperation() {
        return operation;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTechnicalName(String technicalName) {
        this.technicalName = technicalName;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
