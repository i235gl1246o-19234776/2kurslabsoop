package model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResponseDTO {
    private Long functionId;
    private String functionName;
    private String typeFunction;
    private Double xVal;
    private Double yVal;
    private String userName;
    private Long operationsTypeId;
    private String operationTypeName;  // Из таблицы operation_types через JOIN
    private String userEmail;          // Из таблицы users через JOIN

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
        this.operationTypeName = operationTypeName;
    }
}