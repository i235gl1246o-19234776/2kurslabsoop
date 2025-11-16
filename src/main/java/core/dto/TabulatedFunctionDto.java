package core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TabulatedFunctionDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("functionId")
    private Long functionId;

    @JsonProperty("xVal")
    private Double xVal;

    @JsonProperty("yVal")
    private Double yVal;
}
