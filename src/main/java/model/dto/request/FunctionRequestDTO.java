package model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import java.util.logging.Logger;

public class FunctionRequestDTO {
    private static final Logger logger = Logger.getLogger(FunctionRequestDTO.class.getName());

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;

    @NotBlank(message = "Тип функции обязателен")
    @Pattern(regexp = "analytic|tabular", message = "Тип функции должен быть 'analytic' или 'tabular'")
    private String typeFunction;

    @NotBlank(message = "Имя функции обязательно")
    @Size(min = 1, max = 255, message = "Имя функции должно быть от 1 до 255 символов")
    private String functionName;

    private String functionExpression;

    public FunctionRequestDTO() {
        logger.fine("Создан пустой FunctionRequestDTO");
    }

    public FunctionRequestDTO(Long userId, String typeFunction, String functionName, String functionExpression) {
        this.userId = userId;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
        logger.info("Создан FunctionRequestDTO: " + functionName + " (тип: " + typeFunction + ")");
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        logger.fine("Установлен userId: " + userId);
        this.userId = userId;
    }

    public String getTypeFunction() {
        return typeFunction;
    }

    public void setTypeFunction(String typeFunction) {
        logger.fine("Установлен тип функции: " + typeFunction);
        this.typeFunction = typeFunction;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        logger.fine("Установлено имя функции: " + functionName);
        this.functionName = functionName;
    }

    public String getFunctionExpression() {
        return functionExpression;
    }

    public void setFunctionExpression(String functionExpression) {
        logger.fine("Установлено выражение функции: " + functionExpression);
        this.functionExpression = functionExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionRequestDTO that = (FunctionRequestDTO) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(typeFunction, that.typeFunction) &&
                Objects.equals(functionName, that.functionName) &&
                Objects.equals(functionExpression, that.functionExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, typeFunction, functionName, functionExpression);
    }

    @Override
    public String toString() {
        return "FunctionRequestDTO{name='" + functionName + "', type='" + typeFunction + "', userId=" + userId + "}";
    }
}