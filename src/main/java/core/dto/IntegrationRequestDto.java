// src/main/java/core/dto/IntegrationRequestDto.java
package core.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationRequestDto {
    private Long functionId;
    private double fromX;
    private double toX;
    private int threadCount;
    private String factoryType;
}