package model.entity;
/*
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFunctionResult {
    private Long functionId;
    private Long userId;
    private String userName;
    private String functionName;
    private String functionExpression;
    private String typeFunction;
}*/

public class SearchFunctionResult {
    private Long functionId;
    private Long userId;
    private String userName;
    private String functionName;
    private String functionExpression;
    private String typeFunction;

    public SearchFunctionResult() {
    }

    public SearchFunctionResult(Long functionId, Long userId, String userName, String functionName, String functionExpression, String typeFunction) {
        this.functionId = functionId;
        this.userId = userId;
        this.userName = userName;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
        this.typeFunction = typeFunction;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionExpression() {
        return functionExpression;
    }

    public void setFunctionExpression(String functionExpression) {
        this.functionExpression = functionExpression;
    }

    public String getTypeFunction() {
        return typeFunction;
    }

    public void setTypeFunction(String typeFunction) {
        this.typeFunction = typeFunction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchFunctionResult that = (SearchFunctionResult) o;

        if (functionId != null ? !functionId.equals(that.functionId) : that.functionId != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (functionName != null ? !functionName.equals(that.functionName) : that.functionName != null) return false;
        if (functionExpression != null ? !functionExpression.equals(that.functionExpression) : that.functionExpression != null)
            return false;
        return typeFunction != null ? typeFunction.equals(that.typeFunction) : that.typeFunction == null;
    }

    @Override
    public int hashCode() {
        int result = functionId != null ? functionId.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (functionName != null ? functionName.hashCode() : 0);
        result = 31 * result + (functionExpression != null ? functionExpression.hashCode() : 0);
        result = 31 * result + (typeFunction != null ? typeFunction.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SearchFunctionResult{" +
                "functionId=" + functionId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", functionName='" + functionName + '\'' +
                ", functionExpression='" + functionExpression + '\'' +
                ", typeFunction='" + typeFunction + '\'' +
                '}';
    }
}