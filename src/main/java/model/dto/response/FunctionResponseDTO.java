package model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Data
@NoArgsConstructor
@Log
public class FunctionResponseDTO {

    private Long id;
    private Long userId;
    private String typeFunction;
    private String functionName;
    private String functionExpression;

    {
        log.fine("Создан пустой FunctionResponseDTO");
    }

    public FunctionResponseDTO(Long id, Long userId, String typeFunction, String functionName, String functionExpression) {
        this.id = id;
        this.userId = userId;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
        log.info("Создан FunctionResponseDTO: " + functionName + " (id: " + id + ")");
    }

    public FunctionResponseDTO(Long functionId, String functionName, String functionExpression, String typeFunction, Long userId, String userName) {
    }

    @Override
    public String toString() {
        return "FunctionResponseDTO{id=" + id + ", name='" + functionName + "', type='" + typeFunction + "'}";
    }
}