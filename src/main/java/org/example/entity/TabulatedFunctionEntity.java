package org.example.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "function_functions")
public class TabulatedFunctionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private FunctionEntity function;

    @Column(name = "x_val", nullable = false)
    private Double xVal;

    @Column(name = "y_val", nullable = false)
    private Double yVal;

    // Пустой конструктор
    public TabulatedFunctionEntity() {}

    // Конструктор с параметрами
    public TabulatedFunctionEntity(FunctionEntity function, Double xVal, Double yVal) {
        this.function = function;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FunctionEntity getFunction() { return function; }
    public void setFunction(FunctionEntity function) { this.function = function; }

    public Double getXVal() { return xVal; }
    public void setXVal(Double xVal) { this.xVal = xVal; }

    public Double getYVal() { return yVal; }
    public void setYVal(Double yVal) { this.yVal = yVal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TabulatedFunctionEntity that)) return false;
        return Objects.equals(xVal, that.xVal) &&
                Objects.equals(yVal, that.yVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xVal, yVal);
    }

    @Override
    public String toString() {
        return "TabulatedFunctionEntity{id=" + id + ", x=" + xVal + ", y=" + yVal + "}";
    }
}