package model;

import java.time.LocalDateTime;

public class FunctionOperation {
    private Long id;
    private Long functionId;
    private Integer operationTypeId;
    private String parameters; // JSON строка с параметрами
    private Double resultValue;
    private Long resultFunctionId;
    private LocalDateTime executedAt;

    private OperationType operationType;
    private Function originalFunction;
    private Function resultFunction;

    public FunctionOperation() {}

    public FunctionOperation(Long functionId, Integer operationTypeId, String parameters) {
        this.functionId = functionId;
        this.operationTypeId = operationTypeId;
        this.parameters = parameters;
    }

    public FunctionOperation(Long functionId, OperationType operationType, String parameters) {
        this.functionId = functionId;
        this.operationTypeId = operationType.getId();
        this.operationType = operationType;
        this.parameters = parameters;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }

    public Integer getOperationTypeId() { return operationTypeId; }
    public void setOperationTypeId(Integer operationTypeId) {
        this.operationTypeId = operationTypeId;
    }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public Double getResultValue() { return resultValue; }
    public void setResultValue(Double resultValue) { this.resultValue = resultValue; }

    public Long getResultFunctionId() { return resultFunctionId; }
    public void setResultFunctionId(Long resultFunctionId) {
        this.resultFunctionId = resultFunctionId;
    }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    public OperationType getOperationType() { return operationType; }
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
        if (operationType != null) {
            this.operationTypeId = operationType.getId();
        }
    }

    public Function getOriginalFunction() { return originalFunction; }
    public void setOriginalFunction(Function originalFunction) {
        this.originalFunction = originalFunction;
        if (originalFunction != null) {
            this.functionId = originalFunction.getId();
        }
    }

    public Function getResultFunction() { return resultFunction; }
    public void setResultFunction(Function resultFunction) {
        this.resultFunction = resultFunction;
        if (resultFunction != null) {
            this.resultFunctionId = resultFunction.getId();
        }
    }

    public boolean hasNumericResult() {
        return resultValue != null;
    }

    public boolean hasFunctionResult() {
        return resultFunctionId != null;
    }

    public boolean isSuccessful() {
        return resultValue != null || resultFunctionId != null;
    }

    public void setToleranceParameter(double tolerance) {
        this.parameters = String.format("{\"tolerance\": %.6f}", tolerance);
    }

    public void setIntervalParameters(double a, double b) {
        this.parameters = String.format("{\"a\": %.6f, \"b\": %.6f}", a, b);
    }

    public void setOrderParameter(int order) {
        this.parameters = String.format("{\"order\": %d}", order);
    }

    @Override
    public String toString() {
        String resultInfo;
        if (resultValue != null) {
            resultInfo = String.format("numeric result: %.6f", resultValue);
        } else if (resultFunctionId != null) {
            resultInfo = String.format("function result ID: %d", resultFunctionId);
        } else {
            resultInfo = "no result";
        }

        return String.format(
                "FunctionOperation{id=%d, functionId=%d, operationTypeId=%d, %s, executedAt=%s}",
                id, functionId, operationTypeId, resultInfo,
                executedAt != null ? executedAt.toString() : "null"
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionOperation that = (FunctionOperation) o;

        if (!id.equals(that.id)) return false;
        if (!functionId.equals(that.functionId)) return false;
        if (!operationTypeId.equals(that.operationTypeId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + functionId.hashCode();
        result = 31 * result + operationTypeId.hashCode();
        return result;
    }
}