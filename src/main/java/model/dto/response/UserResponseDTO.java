package model.dto.response;

import java.util.Objects;
import java.util.logging.Logger;

public class UserResponseDTO {
    private static final Logger logger = Logger.getLogger(UserResponseDTO.class.getName());

    private Long id;
    private String name;

    public UserResponseDTO() {
        logger.fine("Создан пустой UserResponseDTO");
    }

    public UserResponseDTO(Long id, String name) {
        this.id = id;
        this.name = name;
        logger.info("Создан UserResponseDTO для пользователя: " + name + " (id: " + id + ")");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        logger.fine("Установлен id: " + id);
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        logger.fine("Установлено имя: " + name);
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponseDTO that = (UserResponseDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "UserResponseDTO{id=" + id + ", name='" + name + "'}";
    }
}