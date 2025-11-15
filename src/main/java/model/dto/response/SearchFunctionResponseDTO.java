package model.dto.response;

import java.util.List;

public class SearchFunctionResponseDTO {

    private List<FunctionResponseDTO> functions;
    private int total;
    private Long operationsTypeId;
    private Long functionId;
    private String functionName;
    private String typeFunction;
    private Double xVal;
    private Double yVal;
    private String userName;
    private String operationTypeName;

    // Пустой конструктор
    public SearchFunctionResponseDTO() {
    }

    // Конструктор с параметрами (все поля)
    public SearchFunctionResponseDTO(List<FunctionResponseDTO> functions, int total, Long operationsTypeId,
                                     Long functionId, String functionName, String typeFunction,
                                     Double xVal, Double yVal, String userName, String operationTypeName) {
        this.functions = functions;
        this.total = total;
        this.operationsTypeId = operationsTypeId;
        this.functionId = functionId;
        this.functionName = functionName;
        this.typeFunction = typeFunction;
        this.xVal = xVal;
        this.yVal = yVal;
        this.userName = userName;
        this.operationTypeName = operationTypeName;
    }

    // Конструкторы для удобства
    public SearchFunctionResponseDTO(List<FunctionResponseDTO> functions, int total) {
        this.functions = functions;
        this.total = total;
    }

    // Геттеры
    public List<FunctionResponseDTO> getFunctions() {
        return functions;
    }

    public int getTotal() {
        return total;
    }

    public Long getOperationsTypeId() {
        return operationsTypeId;
    }

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

    public String getOperationTypeName() {
        return operationTypeName;
    }

    // Сеттеры
    public void setFunctions(List<FunctionResponseDTO> functions) {
        this.functions = functions;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setOperationsTypeId(Long operationsTypeId) {
        this.operationsTypeId = operationsTypeId;
    }

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

    public void setOperationTypeName(String operationTypeName) {
        this.operationTypeName = operationTypeName;
    }
}