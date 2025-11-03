package entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "operations")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class OperationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    @ToString.Exclude
    private FunctionEntity function;

    @Column(name = "operations_type_id", nullable = false)
    private Integer operationsTypeId;

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof OperationEntity operation)) return false;
        return Objects.equals(operationsTypeId, operation.operationsTypeId);
    }

    @Override
    public int hashCode(){
        return Objects.hash(operationsTypeId);
    }
}
