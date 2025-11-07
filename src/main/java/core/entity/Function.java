package core.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "functions")
public class Function {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_function_user"))
    private User user;

    @Column(name = "type_function", nullable = false)
    private String typeFunction;

    @Column(name = "function_name", nullable = false)
    private String functionName;

    @Column(name = "function_expression")
    private String functionExpression;

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TabulatedFunction> tabulatedFunctions = new ArrayList<>();

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Operation> operations = new ArrayList<>();

    public Function() {}

    public Function(String typeFunction, User user, String functionName, String functionExpression) {
        this.typeFunction = typeFunction;
        this.user = user;
        this.functionName = functionName;
        this.functionExpression = functionExpression;
    }

    public List<Operation> getOperations() { return operations; }
    public List<TabulatedFunction> getTabulatedFunctions() { return tabulatedFunctions; }
    public String getFunctionExpression() { return functionExpression; }
    public String getFunctionName() { return functionName; }
    public String getTypeFunction() { return typeFunction; }
    public User getUser() { return user; }
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setTypeFunction(String typeFunction) { this.typeFunction = typeFunction; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }
    public void setFunctionExpression(String functionExpression) { this.functionExpression = functionExpression; }
    public void setTabulatedFunctions(List<TabulatedFunction> tabulatedFunctions) { this.tabulatedFunctions = tabulatedFunctions; }
    public void setOperations(List<Operation> operations) { this.operations = operations; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Function func)) return false;
        return Objects.equals(id, func.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FunctionEntity{id=" + id + ", type=" + typeFunction + ", name='" + functionName + "'}";
    }
}
