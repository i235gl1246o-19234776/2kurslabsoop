package core.controller;

import core.dto.UserDto;
import core.entity.UserEntity;
import core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class RegisterController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).+$";

    // POST /api/register - публичная регистрация без аутентификации
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        log.info("Запрос на публичную регистрацию пользователя: {}", request.getUsername());

        // Проверка длины пароля
        if (request.getPassword().length() < MIN_PASSWORD_LENGTH) {
            log.warn("Пароль слишком короткий для пользователя: {}", request.getUsername());
            return ResponseEntity.badRequest().body(Map.of("error", "Пароль должен содержать минимум 8 символов"));
        }

        // Проверка сложности пароля
        if (!request.getPassword().matches(PASSWORD_PATTERN)) {
            log.warn("Пароль не соответствует требованиям безопасности: {}", request.getUsername());
            return ResponseEntity.badRequest().body(Map.of("error", "Пароль должен содержать цифры, заглавные и строчные буквы"));
        }

        if (userRepository.existsByName(request.getUsername())) {
            log.warn("Попытка регистрации существующего пользователя: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Пользователь с таким именем уже существует"));
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setName(request.getUsername());
        userEntity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userEntity.setRole("USER"); // Запрещаем создавать админов через регистрацию

        try {
            UserEntity savedEntity = userRepository.save(userEntity);
            UserDto savedDto = new UserDto(
                    savedEntity.getId(),
                    savedEntity.getName(),
                    savedEntity.getRole(),
                    null,  // password not included in response
                    null   // functionIds
            );
            log.info("Успешная регистрация пользователя: {}", savedDto.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
        } catch (Exception e) {
            log.error("Ошибка при регистрации пользователя: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при создании пользователя"));
        }
    }

    // DTO для регистрации
    public static class RegisterRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}