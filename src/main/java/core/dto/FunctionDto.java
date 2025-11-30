package core.dto;

import lombok.*;
import java.util.List;
@Setter
@Getter
public class FunctionDto {
    // Геттеры и сеттеры
    private Long id;
    private Long userId;
    private String typeFunction;
    private String functionName;
    private String functionExpression;
    private List<Long> tabulatedValueIds;
    private List<Long> operationIds;

    // Конструкторы
    public FunctionDto() {}

    public FunctionDto(Long id, Long userId, String typeFunction, String functionName,
                       String functionExpression, List<Long> tabulatedValueIds, List<Long> operationIds) {
        this.id = id;
        this.userId = userId;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
        this.tabulatedValueIds = tabulatedValueIds;
        this.operationIds = operationIds;
    }

}
