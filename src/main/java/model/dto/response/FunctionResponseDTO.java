package model.dto.response;

import java.util.Objects;
import java.util.logging.Logger;

public class FunctionResponseDTO {
    private static final Logger logger = Logger.getLogger(FunctionResponseDTO.class.getName());

    private Long id;
    private Long userId;
    private String typeFunction;
    private String functionName;
    private String functionExpression;

    public FunctionResponseDTO() {
        logger.fine("Создан пустой FunctionResponseDTO");
    }

    public FunctionResponseDTO(Long id, Long userId, String typeFunction, String functionName, String functionExpression) {
        this.id = id;
        this.userId = userId;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
        logger.info("Создан FunctionResponseDTO: " + functionName + " (id: " + id + ")");
    }

    public FunctionResponseDTO(Long functionId, String functionName, String functionExpression, String typeFunction, Long userId, String userName) {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        logger.fine("Установлен id: " + id);
        this.id = id;
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
        logger.fine("Установлен typeFunction: " + typeFunction);
        this.typeFunction = typeFunction;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        logger.fine("Установлено functionName: " + functionName);
        this.functionName = functionName;
    }

    public String getFunctionExpression() {
        return functionExpression;
    }

    public void setFunctionExpression(String functionExpression) {
        logger.fine("Установлено functionExpression: " + functionExpression);
        this.functionExpression = functionExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionResponseDTO that = (FunctionResponseDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(typeFunction, that.typeFunction) &&
                Objects.equals(functionName, that.functionName) &&
                Objects.equals(functionExpression, that.functionExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, typeFunction, functionName, functionExpression);
    }

    @Override
    public String toString() {
        return "FunctionResponseDTO{id=" + id + ", name='" + functionName + "', type='" + typeFunction + "'}";
    }
}