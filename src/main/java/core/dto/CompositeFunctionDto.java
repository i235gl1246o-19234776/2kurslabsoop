package core.dto;

import lombok.*;

@Setter
@Getter
public class CompositeFunctionDto {
    private Long userId;
    private String baseFunctionName;
    private String outerFunctionName;
    private String customName;
}