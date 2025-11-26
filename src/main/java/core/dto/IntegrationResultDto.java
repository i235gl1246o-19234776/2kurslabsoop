package core.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationResultDto {
    private double value;
    private long duration; // в миллисекундах
    private String error;

    public IntegrationResultDto(double result, long duration) {
    }
}