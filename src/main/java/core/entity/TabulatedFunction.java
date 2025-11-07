package core.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tabulated_functions")
public class TabulatedFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tabulated_functions_function"))
    private Function function;

    @Column(name = "x_val", nullable = false)
    private Double xVal;

    @Column(name = "y_val", nullable = false)
    private Double yVal;

    public TabulatedFunction() {}

    public TabulatedFunction(Function function, Double xVal, Double yVal) {
        this.function = function;
        this.xVal = xVal;
        this.yVal = yVal;
    }

    public Long getId() { return id; }
    public Function getFunction() { return function; }
    public Double getXVal() { return xVal; }
    public Double getYVal() { return yVal; }

    public void setId(Long id) { this.id = id; }
    public void setFunction(Function function) { this.function = function; }
    public void setXVal(Double xVal) { this.xVal = xVal; }
    public void setYVal(Double yVal) { this.yVal = yVal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TabulatedFunction tf)) return false;
        return Objects.equals(id, tf.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TabulatedFunctionEntity{id=" + id + ", x=" + xVal + ", y=" + yVal + "}";
    }
}