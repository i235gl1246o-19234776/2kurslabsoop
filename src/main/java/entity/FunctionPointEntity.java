package entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "function_points",
        uniqueConstraints = @UniqueConstraint(columnNames = {"function_id", "x_val"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private FunctionEntity function;

    @Column(name = "x_val", precision = 20, scale = 12, nullable = false)
    private BigDecimal xVal;

    @Column(name = "y_val", nullable = false)
    private Double yVal;

    @Column(name = "computed_at", nullable = false, updatable = false)
    private LocalDateTime computedAt = LocalDateTime.now();

}
