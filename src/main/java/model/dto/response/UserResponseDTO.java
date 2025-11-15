package model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Data
@NoArgsConstructor
@Log
public class UserResponseDTO {

    private Long id;
    private String name;

    public UserResponseDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}