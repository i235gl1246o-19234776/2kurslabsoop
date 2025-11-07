package core.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "operations")
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false, foreignKey = @ForeignKey(name = "fk_operations_function"))
    private Function function;

    @Column(name = "operations_type_id", nullable = false)
    private Integer operationsTypeId;

    public Operation() {}

    public Operation(Function function, Integer operationsTypeId) {
        this.function = function;
        this.operationsTypeId = operationsTypeId;
    }

    public Long getId() { return id; }
    public Function getFunction() { return function; }
    public Integer getOperationsTypeId() { return operationsTypeId; }

    public void setId(Long id) { this.id = id; }
    public void setFunction(Function function) { this.function = function; }
    public void setOperationsTypeId(Integer operationsTypeId) { this.operationsTypeId = operationsTypeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operation op)) return false;
        return Objects.equals(id, op.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OperationEntity{id=" + id + ", type=" + operationsTypeId + "}";
    }
}