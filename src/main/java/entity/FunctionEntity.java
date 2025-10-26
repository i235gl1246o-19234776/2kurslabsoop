package entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "functions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String name;

    @Column(name = "data_format", nullable = false)
    private String dataFormat; // 'analytical' или 'tabulated'

    @Column(name = "function_source", nullable = false)
    private String functionSource; // 'base', 'derived', 'composite'

    private String expression;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_function_id")
    private FunctionEntity parentFunction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Обратные связи
    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FunctionPointEntity> points = new ArrayList<>();

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FunctionOperationEntity> operations = new ArrayList<>();

    @OneToMany(mappedBy = "resultFunction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FunctionOperationEntity> resultingOperations = new ArrayList<>();

}
