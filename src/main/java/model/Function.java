package model;

import java.time.LocalDateTime;

public class Function {
    private Long id;
    private Long userId;
    private String name;
    private String dataFormat;
    private String functionSource;
    private String expression;
    private Long parentFunctionId;
    private LocalDateTime createdAt;

    public Function() {}

    public Function(Long userId, String name, String dataFormat,
                    String functionSource, String expression) {
        this.userId = userId;
        this.name = name;
        this.dataFormat = dataFormat;
        this.functionSource = functionSource;
        this.expression = expression;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDataFormat() { return dataFormat; }
    public void setDataFormat(String dataFormat) { this.dataFormat = dataFormat; }
    public String getFunctionSource() { return functionSource; }
    public void setFunctionSource(String functionSource) { this.functionSource = functionSource; }
    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
    public Long getParentFunctionId() { return parentFunctionId; }
    public void setParentFunctionId(Long parentFunctionId) { this.parentFunctionId = parentFunctionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("Function{id=%d, name='%s', format='%s', source='%s'}",
                id, name, dataFormat, functionSource);
    }
}