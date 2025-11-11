package model.entity;

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
}