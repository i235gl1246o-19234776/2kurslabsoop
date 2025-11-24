// src/main/java/core/dto/OperationRequestDto.java
package core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OperationRequestDto {

    @JsonProperty("operand1Id")
    private Long operand1Id;

    @JsonProperty("operand2Id")
    private Long operand2Id;

    @JsonProperty("factoryType")
    private String factoryType = "array"; // Значение по умолчанию

    // Конструктор по умолчанию
    public OperationRequestDto() {}

    // Полный конструктор
    public OperationRequestDto(Long operand1Id, Long operand2Id, String factoryType) {
        this.operand1Id = operand1Id;
        this.operand2Id = operand2Id;
        this.factoryType = factoryType != null ? factoryType : "array";
    }

    // Getters и Setters
    public Long getOperand1Id() {
        return operand1Id;
    }
    public void setOperand1Id(Long operand1Id) {
        this.operand1Id = operand1Id;
    }

    public Long getOperand2Id() {
        return operand2Id;
    }
    public void setOperand2Id(Long operand2Id) {
        this.operand2Id = operand2Id;
    }

    public String getFactoryType() {
        return factoryType;
    }
    public void setFactoryType(String factoryType) {
        this.factoryType = factoryType;
    }

    @Override
    public String toString() {
        return "OperationRequestDto{" +
                "operand1Id=" + operand1Id +
                ", operand2Id=" + operand2Id +
                ", factoryType='" + factoryType + '\'' +
                '}';
    }
}