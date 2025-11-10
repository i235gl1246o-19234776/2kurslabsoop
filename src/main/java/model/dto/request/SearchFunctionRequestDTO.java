package model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFunctionRequestDTO {

    @Size(max = 255, message = "Имя пользователя не должно превышать 255 символов")
    private String userName;

    @Size(max = 255, message = "Название функции не должно превышать 255 символов")
    private String functionName;

    private String typeFunction; // "tabulated" или "analytic"

    private Double xVal;
    private Double yVal;

    private Long operationsTypeId;

    private String sortBy = "function_id";
    private String sortOrder = "asc";

    public void setSortOrder(String sortOrder) {
        if ("asc".equalsIgnoreCase(sortOrder) || "desc".equalsIgnoreCase(sortOrder)) {
            this.sortOrder = sortOrder.toLowerCase();
        } else {
            this.sortOrder = "asc";
        }
    }

    public boolean isValidSortBy() {
        return switch (sortBy) {
            case "function_id", "function_name", "type_function", "user_name" -> true;
            default -> false;
        };
    }

    public SearchFunctionRequestDTO(String userName, String functionName, String typeFunction, Double xVal, Double yVal) {
        this.userName = userName;
        this.functionName = functionName;
        this.typeFunction = typeFunction;
        this.xVal = xVal;
        this.yVal = yVal;
    }
}