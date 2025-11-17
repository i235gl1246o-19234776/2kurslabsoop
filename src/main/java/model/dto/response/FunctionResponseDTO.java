package model.dto.response;

public class FunctionResponseDTO {

    private Long functionId;
    private String functionName;
    private String typeFunction;
    private Double xVal;
    private Double yVal;
    private String userName;
    private Long operationsTypeId;

    // Пустой конструктор
    public FunctionResponseDTO() {
    }

    // Конструктор с параметрами (все поля)
    public FunctionResponseDTO(Long functionId, String functionName, String typeFunction,
                               Double xVal, Double yVal, String userName, Long operationsTypeId,
                               String operationTypeName, String userEmail) {
        this.functionId = functionId;
        this.functionName = functionName;
        this.typeFunction = typeFunction;
        this.xVal = xVal;
        this.yVal = yVal;
        this.userName = userName;
        this.operationsTypeId = operationsTypeId;
    }

    // Дополнительные конструкторы для удобства
    public FunctionResponseDTO(Long functionId, String functionName, String typeFunction,
                               Double xVal, Double yVal, String userName, Long operationsTypeId) {
        this.functionId = functionId;
        this.functionName = functionName;
        this.typeFunction = typeFunction;
        this.xVal = xVal;
        this.yVal = yVal;
        this.userName = userName;
        this.operationsTypeId = operationsTypeId;
    }

    public FunctionResponseDTO(Long functionId, String functionName, String typeFunction,
                               Double xVal, Double yVal, String userName,
                               Long operationsTypeId, String operationTypeName) {
        this.functionId = functionId;
        this.functionName = functionName;
        this.typeFunction = typeFunction;
        this.xVal = xVal;
        this.yVal = yVal;
        this.userName = userName;
        this.operationsTypeId = operationsTypeId;
    }

    // Геттеры
    public Long getFunctionId() {
        return functionId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getTypeFunction() {
        return typeFunction;
    }

    public Double getXVal() {
        return xVal;
    }

    public Double getYVal() {
        return yVal;
    }

    public String getUserName() {
        return userName;
    }

    public Long getOperationsTypeId() {
        return operationsTypeId;
    }

    // Сеттеры
    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void setTypeFunction(String typeFunction) {
        this.typeFunction = typeFunction;
    }

    public void setXVal(Double xVal) {
        this.xVal = xVal;
    }

    public void setYVal(Double yVal) {
        this.yVal = yVal;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setOperationsTypeId(Long operationsTypeId) {
        this.operationsTypeId = operationsTypeId;
    }
}