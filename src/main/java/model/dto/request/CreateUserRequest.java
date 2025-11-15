package model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    private String name;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
    private String password;

    // Пустой конструктор
    public CreateUserRequest() {
    }

    // Конструктор с параметрами
    public CreateUserRequest(String name, String password) {
        this.name = name;
        this.password = password;
    }

    // Геттеры
    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    // Сеттеры
    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserRequestDTO{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}