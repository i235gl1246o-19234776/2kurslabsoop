package core.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "functions")
public class FunctionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_function", nullable = false)
    private FunctionType typeFunction;

    @Column(name = "function_name", nullable = false)
    private String functionName;

    @Column(name = "function_expression")
    private String functionExpression;

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TabulatedFunctionEntity> tabulatedValues = new ArrayList<>();

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OperationEntity> operations = new ArrayList<>();

    public enum FunctionType {
        tabular,
        analytic
    }

    // Пустой конструктор
    public FunctionEntity() {}

    // Удобный конструктор для тестов
    public FunctionEntity(UserEntity user, FunctionType typeFunction, String functionName, String functionExpression) {
        this.user = user;
        this.typeFunction = typeFunction;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public FunctionType getTypeFunction() { return typeFunction; }
    public void setTypeFunction(FunctionType typeFunction) { this.typeFunction = typeFunction; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getFunctionExpression() { return functionExpression; }
    public void setFunctionExpression(String functionExpression) { this.functionExpression = functionExpression; }

    public List<TabulatedFunctionEntity> getTabulatedValues() { return tabulatedValues; }
    public void setTabulatedValues(List<TabulatedFunctionEntity> tabulatedValues) { this.tabulatedValues = tabulatedValues; }

    public List<OperationEntity> getOperations() { return operations; }
    public void setOperations(List<OperationEntity> operations) { this.operations = operations; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionEntity that)) return false;
        return typeFunction == that.typeFunction &&
                Objects.equals(functionName, that.functionName) &&
                Objects.equals(functionExpression, that.functionExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeFunction, functionName, functionExpression);
    }

    @Override
    public String toString() {
        return "FunctionEntity{id=" + id + ", functionName='" + functionName + "', type=" + typeFunction + "}";
    }
}