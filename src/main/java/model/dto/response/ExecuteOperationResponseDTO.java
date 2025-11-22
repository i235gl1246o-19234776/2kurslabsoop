package model.dto.response;

import model.dto.PointDTO;

import java.util.List;

public class ExecuteOperationResponseDTO {
    private Long functionIdA;
    private Long functionIdB;
    private String operation;
    private List<PointDTO> points;

    public List<PointDTO> getPoints() {
        return points;
    }

    public Long getFunctionIdA() {
        return functionIdA;
    }

    public String getOperation() {
        return operation;
    }

    public Long getFunctionIdB() {
        return functionIdB;
    }

    public void setFunctionIdA(Long functionIdA) {
        this.functionIdA = functionIdA;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setPoints(List<PointDTO> points) {
        this.points = points;
    }

    public void setFunctionIdB(Long functionIdB) {
        this.functionIdB = functionIdB;
    }
}
