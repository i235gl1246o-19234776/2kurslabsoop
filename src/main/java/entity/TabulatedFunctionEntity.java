package entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "function_functions")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
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
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof TabulatedFunctionEntity that)) return false;
        return Objects.equals(xVal, that.xVal) &&
                Objects.equals(yVal, that.yVal);
    }

    @Override
    public int hashCode(){
        return Objects.hash(xVal, yVal);
    }
}
