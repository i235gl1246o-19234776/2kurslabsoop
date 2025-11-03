package org.example.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "operations")
public class OperationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private FunctionEntity function;

    @Column(name = "operations_type_id", nullable = false)
    private Integer operationsTypeId;

    // Пустой конструктор
    public OperationEntity() {}

    // Конструктор с параметрами
    public OperationEntity(FunctionEntity function, Integer operationsTypeId) {
        this.function = function;
        this.operationsTypeId = operationsTypeId;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FunctionEntity getFunction() { return function; }
    public void setFunction(FunctionEntity function) { this.function = function; }

    public Integer getOperationsTypeId() { return operationsTypeId; }
    public void setOperationsTypeId(Integer operationsTypeId) { this.operationsTypeId = operationsTypeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperationEntity that)) return false;
        return Objects.equals(operationsTypeId, that.operationsTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationsTypeId);
    }

    @Override
    public String toString() {
        return "OperationEntity{id=" + id + ", operationsTypeId=" + operationsTypeId + "}";
    }
}