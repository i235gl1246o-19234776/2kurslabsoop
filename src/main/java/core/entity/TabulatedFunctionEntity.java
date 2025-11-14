package core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "tabulated_functions")
@Data
@NoArgsConstructor
public class TabulatedFunctionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    @ToString.Exclude
    private FunctionEntity function;

    @Column(name = "x_val", nullable = false)
    private Double xVal;

    @Column(name = "y_val", nullable = false)
    private Double yVal;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TabulatedFunctionEntity that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}