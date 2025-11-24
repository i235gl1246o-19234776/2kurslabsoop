// src/main/java/core/dto/IntegrationResultDto.java
package core.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationResultDto {
    private double value;
    private long duration; // в миллисекундах
}