package model.dto.response;

import model.entity.UserRole;

import java.util.logging.Logger;

public class UserResponseDTO {

    private static final Logger log = Logger.getLogger(UserResponseDTO.class.getName());

    private Long id;
    private String name;
    private UserRole role;

    // Пустой конструктор
    public UserResponseDTO() {
        log.fine("Создан пустой UserResponseDTO");
    }

    // Конструктор с параметрами
    public UserResponseDTO(Long id, String name) {
        this.id = id;
        this.name = name;
        log.fine("Создан UserResponseDTO с id: " + id);
    }

    public UserResponseDTO(Long id, String name, UserRole role) {
        this.id = id;
        this.name = name;
        this.role = role;
        log.fine("Создан UserResponseDTO с id: " + id + role);
    }

    // Геттеры
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Сеттеры
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}