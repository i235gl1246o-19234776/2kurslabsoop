package core.dto;

import lombok.*;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionDto {

    private Long id;
    private Long userId;
    private String typeFunction;
    private String functionName;
    private String functionExpression;

    private List<Long> tabulatedPointIds;
    private List<Long> operationIds;

}
