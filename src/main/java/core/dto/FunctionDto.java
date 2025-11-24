package core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    @JsonInclude(JsonInclude.Include.NON_NULL) // Добавьте аннотацию
    private List<Long> tabulatedPointIds;
    @JsonInclude(JsonInclude.Include.NON_NULL) // Добавьте аннотацию
    private List<Long> operationIds;

}
