package model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Data
@NoArgsConstructor
@Log
public class FunctionRequestDTO {

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;

    @NotBlank(message = "Тип функции обязателен")
    @Pattern(regexp = "analytic|tabular", message = "Тип функции должен быть 'analytic' или 'tabular'")
    private String typeFunction;

    @NotBlank(message = "Имя функции обязательно")
    @Size(min = 1, max = 255, message = "Имя функции должно быть от 1 до 255 символов")
    private String functionName;

    private String functionExpression;

    {
        log.fine("Создан пустой FunctionRequestDTO");
    }

    public FunctionRequestDTO(Long userId, String typeFunction, String functionName, String functionExpression) {
        this.userId = userId;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
        log.info("Создан FunctionRequestDTO: " + functionName + " (тип: " + typeFunction + ")");
    }
}