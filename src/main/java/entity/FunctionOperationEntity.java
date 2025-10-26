package entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "function_operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionOperationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private FunctionEntity function;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operation_type_id", nullable = false)
    private OperationTypeEntity operationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode parameters;

    @Column(name = "result_value")
    private Double resultValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_function_id")
    private FunctionEntity resultFunction;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt = LocalDateTime.now();

}
