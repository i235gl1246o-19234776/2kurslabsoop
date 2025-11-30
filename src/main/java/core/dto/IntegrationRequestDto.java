package core.dto;

import lombok.*;

@Setter
@Getter
public class IntegrationRequestDto {
    private Long functionId;
    private double fromX;
    private double toX;
    private int threadCount;

}