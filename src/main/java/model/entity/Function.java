package model.entity;

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
}