package model.servlet;

public class FunctionBlock {
    private String type; // "function" или "operation"
    private String functionId;
    private String name;
    private String technicalName;
    private String operation; // "add", "subtract", "multiply", "divide", "compose"

    // Конструкторы
    public FunctionBlock() {}

    public FunctionBlock(String type, String functionId, String name, String technicalName, String operation) {
        this.type = type;
        this.functionId = functionId;
        this.name = name;
        this.technicalName = technicalName;
        this.operation = operation;
    }

    // Геттеры и сеттеры
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFunctionId() { return functionId; }
    public void setFunctionId(String functionId) { this.functionId = functionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTechnicalName() { return technicalName; }
    public void setTechnicalName(String technicalName) { this.technicalName = technicalName; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
}