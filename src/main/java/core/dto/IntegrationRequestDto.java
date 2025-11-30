package core.dto;

import lombok.*;

@Setter
@Getter
public class IntegrationRequestDto {
    private Long functionId;
    private double a;      // Изменили fromX на a
    private double b;      // Изменили toX на b
    private int steps;     // Добавили новое поле
    private int threads;   // Изменили threadCount на threads
}