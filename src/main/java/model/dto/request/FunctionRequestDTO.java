package model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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

    // Пустой конструктор
    public FunctionRequestDTO() {
    }

    // Конструктор с параметрами
    public FunctionRequestDTO(Long userId, String typeFunction, String functionName, String functionExpression) {
        this.userId = userId;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
    }

    // Геттеры
    public Long getUserId() {
        return userId;
    }

    public String getTypeFunction() {
        return typeFunction;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getFunctionExpression() {
        return functionExpression;
    }

    // Сеттеры
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTypeFunction(String typeFunction) {
        this.typeFunction = typeFunction;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void setFunctionExpression(String functionExpression) {
        this.functionExpression = functionExpression;
    }
}