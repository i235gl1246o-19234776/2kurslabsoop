package model.dto.request;

public class DifferentiateRequestDTO {
    private Long functionId;
    private String factoryType;

    public Long getFunctionId() { return functionId; }
    public void setFunctionId(Long functionId) { this.functionId = functionId; }

    public String getFactoryType() { return factoryType; }
    public void setFactoryType(String factoryType) { this.factoryType = factoryType; }
}