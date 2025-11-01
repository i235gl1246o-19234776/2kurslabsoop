// dto/request/SearchFunctionRequestDTO.java
package model.dto.request;

import jakarta.validation.constraints.Size;

public class SearchFunctionRequestDTO {

    @Size(max = 255, message = "Имя пользователя не должно превышать 255 символов")
    private String userName;

    @Size(max = 255, message = "Название функции не должно превышать 255 символов")
    private String functionName;

    private String typeFunction; // "tabulated" или "analytic"

    private Double xVal;
    private Double yVal;

    private Long operationsTypeId;

    private String sortBy = "function_id";
    private String sortOrder = "asc";

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) {
        if ("asc".equalsIgnoreCase(sortOrder) || "desc".equalsIgnoreCase(sortOrder)) {
            this.sortOrder = sortOrder.toLowerCase();
        } else {
            this.sortOrder = "asc";
        }
    }
    public boolean isValidSortBy() {
        return switch (sortBy) {
            case "function_id", "function_name", "type_function", "user_name" -> true;
            default -> false;
        };
    }

    public SearchFunctionRequestDTO() {}

    public SearchFunctionRequestDTO(String userName, String functionName, String typeFunction, Double xVal, Double yVal) {
        this.userName = userName;
        this.functionName = functionName;
        this.typeFunction = typeFunction;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getTypeFunction() { return typeFunction; }
    public void setTypeFunction(String typeFunction) { this.typeFunction = typeFunction; }

    public Double getXVal() { return xVal; }
    public void setXVal(Double xVal) { this.xVal = xVal; }

    public Double getYVal() { return yVal; }
    public void setYVal(Double yVal) { this.yVal = yVal; }

    public Long getOperationsTypeId() {
        return operationsTypeId;
    }

    public void setOperationsTypeId(Long operationsTypeId) {
        this.operationsTypeId = operationsTypeId;
    }
}