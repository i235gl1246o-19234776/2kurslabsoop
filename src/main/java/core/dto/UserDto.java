package core.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserDto {
    // Геттеры и сеттеры
    private Long id;
    private String name;
    private String role;
    private String password;
    private List<Long> functionIds;

    // Конструкторы
    public UserDto() {}

    public UserDto(Long id, String name, String role, String password, List<Long> functionIds) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.password = password;
        this.functionIds = functionIds;
    }

}
