package core.dto;
import lombok.*;
import core.controller.FunctionController;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFunctionResponseDTO {
    private List<FunctionController.FunctionResponseDTO> functions;
    private int total;
}
