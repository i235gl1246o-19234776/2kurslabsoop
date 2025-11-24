// src/main/java/core/dto/CompositeFunctionDto.java
package core.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeFunctionDto {
    private String baseFunctionName;
    private String outerFunctionName;
    private String customName;
    private Long userId;
}