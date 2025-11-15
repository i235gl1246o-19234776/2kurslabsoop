package model.dto.request;

public class SearchFunctionRequestDTO {

    private String userName;
    private String functionName;
    private String typeFunction;
    private Double xVal;
    private Double yVal;
    private Long operationsTypeId;
    private String sortBy;
    private String sortOrder;
    private Integer page;
    private Integer size;

    // Пустой конструктор
    public SearchFunctionRequestDTO() {
    }

    // Геттеры
    public String getUserName() {
        return userName;
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

    public Long getOperationsTypeId() {
        return operationsTypeId;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }

    // Сеттеры
    public void setUserName(String userName) {
        this.userName = userName;
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

    public void setOperationsTypeId(Long operationsTypeId) {
        this.operationsTypeId = operationsTypeId;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}