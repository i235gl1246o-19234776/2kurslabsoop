package model.entity;
/*
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Function {
    private Long id;
    private Long userId;
    private String typeFunction;
    private String functionName;
    private String functionExpression;

    public Function(Long userId, String typeFunction, String functionName, String functionExpression) {
        this.userId = userId;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
    }
}*/

public class Function {
    private Long id;
    private Long userId;
    private String typeFunction;
    private String functionName;
    private String functionExpression;

    public Function() {
    }

    public Function(Long userId, String typeFunction, String functionName, String functionExpression) {
        this.userId = userId;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
    }

    public Function(Long id, Long userId, String typeFunction, String functionName, String functionExpression) {
        this.id = id;
        this.userId = userId;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTypeFunction() {
        return typeFunction;
    }

    public void setTypeFunction(String typeFunction) {
        this.typeFunction = typeFunction;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Function function = (Function) o;

        if (id != null ? !id.equals(function.id) : function.id != null) return false;
        if (userId != null ? !userId.equals(function.userId) : function.userId != null) return false;
        if (typeFunction != null ? !typeFunction.equals(function.typeFunction) : function.typeFunction != null)
            return false;
        if (functionName != null ? !functionName.equals(function.functionName) : function.functionName != null)
            return false;
        return functionExpression != null ? functionExpression.equals(function.functionExpression) : function.functionExpression == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (typeFunction != null ? typeFunction.hashCode() : 0);
        result = 31 * result + (functionName != null ? functionName.hashCode() : 0);
        result = 31 * result + (functionExpression != null ? functionExpression.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Function{" +
                "id=" + id +
                ", userId=" + userId +
                ", typeFunction='" + typeFunction + '\'' +
                ", functionName='" + functionName + '\'' +
                ", functionExpression='" + functionExpression + '\'' +
                '}';
    }
}