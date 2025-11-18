package core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "functions")
@Data
@NoArgsConstructor
public class FunctionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_function", nullable = false)
    private FunctionType typeFunction;

    @Column(name = "function_name", nullable = false)
    private String functionName;

    @Column(name = "function_expression")
    private String functionExpression;

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TabulatedFunctionEntity> tabulatedValues = new ArrayList<>();

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<OperationEntity> operations = new ArrayList<>();

    public enum FunctionType {
        tabular, analytic;

        @Override
        public String toString() {
            return name().toUpperCase();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionEntity that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}