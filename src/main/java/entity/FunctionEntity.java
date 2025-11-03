package entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Entity
@Table(name = "functions")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FunctionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
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

    public enum FunctionType{
        TABULAR, ANALYTIC
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof FunctionEntity function)) return false;
        return typeFunction == function.typeFunction &&
                Objects.equals(functionName, function.functionName) &&
                Objects.equals(functionExpression, function.functionExpression);
    }


    @Override
    public int hashCode(){
        return Objects.hash(typeFunction, functionName, functionExpression);
    }
}
