package entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "operation_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationTypeEntity {

    @Id
    @Column(nullable = false)
    private Short id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "result_type", nullable = false, length = 10)
    private String resultType;

}
