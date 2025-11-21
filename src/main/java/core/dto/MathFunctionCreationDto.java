// src/main/java/core/dto/MathFunctionCreationDto.java
package core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MathFunctionCreationDto {

    @JsonProperty("mathFunctionName")
    private String mathFunctionName;

    @JsonProperty("xFrom")
    private double xFrom;

    @JsonProperty("xTo")
    private double xTo;

    @JsonProperty("count")
    private int count;

    @JsonProperty("userId")
    private Long userId;

    // --- НОВОЕ поле для выбора фабрики ---
    @JsonProperty("factoryType") // Может быть "array" или "linkedlist"
    private String factoryType = "array"; // Значение по умолчанию
}