package core.dto;

import lombok.*;

@Setter
@Getter
public class IntegrationResultDto {
    private double value;
    private long duration;

    public IntegrationResultDto() {}

    public IntegrationResultDto(double value, long duration) {
        this.value = value;
        this.duration = duration;
    }
}