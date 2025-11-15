package model.entity;
/*
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operation {
    private Long id;
    private Long functionId;
    private Integer operationsTypeId;

    public Operation(Long functionId, Integer operationsTypeId) {
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
    }
}*/
public class Operation {
    private Long id;
    private Long functionId;
    private Integer operationsTypeId;

    public Operation() {
    }

    public Operation(Long functionId, Integer operationsTypeId) {
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
    }

    public Operation(Long id, Long functionId, Integer operationsTypeId) {
        this.id = id;
        this.functionId = functionId;
        this.operationsTypeId = operationsTypeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public Integer getOperationsTypeId() {
        return operationsTypeId;
    }

    public void setOperationsTypeId(Integer operationsTypeId) {
        this.operationsTypeId = operationsTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operation operation = (Operation) o;

        if (id != null ? !id.equals(operation.id) : operation.id != null) return false;
        if (functionId != null ? !functionId.equals(operation.functionId) : operation.functionId != null) return false;
        return operationsTypeId != null ? operationsTypeId.equals(operation.operationsTypeId) : operation.operationsTypeId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (functionId != null ? functionId.hashCode() : 0);
        result = 31 * result + (operationsTypeId != null ? operationsTypeId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "id=" + id +
                ", functionId=" + functionId +
                ", operationsTypeId=" + operationsTypeId +
                '}';
    }
}