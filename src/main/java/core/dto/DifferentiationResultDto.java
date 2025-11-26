package core.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DifferentiationResultDto {
    private List<TabulatedFunctionDto> points;
    private String error;
}