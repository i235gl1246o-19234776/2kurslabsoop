package core.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculatePointsRequestDto {
    private Long functionId;
    private String mathFunctionName;
    private double start;
    private double end;
    private int count;
    private String factoryType = "array";
}