package core.controller;

import core.entity.*;
import core.dto.*;
import core.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PutMapping("/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        log.info("Запрос на обновление роли пользователя с ID: {} на роль: {}", id, role);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка обновления роли пользователя без аутентификации пользователем с ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        if (!"ROLE_ADMIN".equals(currentRole)) {
            log.warn("Пользователь '{}' (роль: {}) не имеет прав для обновления роли другого пользователя (ID: {})", auth.getName(), currentRole, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<UserEntity> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            log.warn("Попытка обновить роль несуществующего пользователя с ID: {}", id);
            return ResponseEntity.notFound().build(); // 404 Not Found
        }

        UserEntity user = userOpt.get();
        if (!"USER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            log.warn("Попытка установить недопустимую роль '{}' пользователю с ID: {}", role, id);
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }

        user.setRole(role);

        try {
            UserEntity updatedEntity = userRepository.save(user);
            UserDto updatedDto = convertToDto(updatedEntity);
            log.info("Роль пользователя с ID {} обновлена на '{}' пользователем '{}'", id, role, auth.getName());
            return ResponseEntity.ok(updatedDto);
        } catch (Exception e) {
            log.error("Ошибка при обновлении роли пользователя с ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        List<UserEntity> users = userRepository.findAll();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto) // Конвертируем в DTO
                .collect(Collectors.toList());
        log.info("Возвращено {} пользователей", userDtos.size());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Запрос на получение пользователя с ID: {}", id);
        Optional<UserEntity> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            UserDto userDto = convertToDto(userOpt.get());
            log.info("Пользователь с ID {} найден", id);
            return ResponseEntity.ok(userDto);
        } else {
            log.warn("Пользователь с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<UserDto> getUserByName(@PathVariable String name) {
        log.info("Запрос на поиск пользователя по имени: {}", name);
        UserEntity user = userRepository.findByName(name);
        if (user != null) {
            UserDto userDto = convertToDto(user);
            log.info("Пользователь с именем '{}' найден", name);
            return ResponseEntity.ok(userDto);
        } else {
            log.warn("Пользователь с именем '{}' не найден", name);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exist/{name}")
    public ResponseEntity<Boolean> userExists(@PathVariable String name) {
        log.info("Проверка существования пользователя: {}", name);
        boolean exists = userRepository.existsByName(name);
        log.info("Пользователь '{}' существует: {}", name, exists);
        return ResponseEntity.ok(exists);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        log.info("Запрос на создание пользователя: {}", userDto.getName());

        if (userRepository.existsByName(userDto.getName())) {
            log.warn("Пользователь с именем '{}' уже существует", userDto.getName());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setName(userDto.getName());

        String rawPassword = userDto.getPassword();
        String hashedPassword = passwordEncoder.encode(rawPassword);
        userEntity.setPasswordHash(hashedPassword);

        userEntity.setRole("USER"); // Устанавливаем роль по умолчанию
        try {
            UserEntity savedEntity = userRepository.save(userEntity);
            UserDto savedDto = convertToDto(savedEntity);
            log.info("Пользователь '{}' создан с ID: {}", savedDto.getName(), savedDto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
        } catch (Exception e) {
            log.error("Ошибка при создании пользователя: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {
        log.info("Запрос на аутентификацию пользователя: {}", authRequest.getName());
        UserEntity user = userRepository.findByName(authRequest.getName());

        if (user != null && passwordEncoder.matches(authRequest.getPassword(), user.getPasswordHash())) {
            UserDto userDto = convertToDto(user);
            log.info("Аутентификация успешна для пользователя: {}", user.getName());
            return ResponseEntity.ok(userDto);
        } else {
            log.warn("Аутентификация не удалась для пользователя: {}", authRequest.getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("authenticated", false));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDtoDetails) {
        log.info("Запрос на обновление пользователя с ID: {}", id);
        Optional<UserEntity> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            user.setName(userDtoDetails.getName());

            if (userDtoDetails.getPassword() != null && !userDtoDetails.getPassword().isEmpty()) {
                String hashedPassword = passwordEncoder.encode(userDtoDetails.getPassword());
                user.setPasswordHash(hashedPassword);
                log.debug("Пароль пользователя с ID {} обновлён", id);
            }

            UserEntity updatedEntity = userRepository.save(user);
            UserDto updatedDto = convertToDto(updatedEntity);
            log.info("Пользователь с ID {} обновлён", id);
            return ResponseEntity.ok(updatedDto);
        } else {
            log.warn("Попытка обновить несуществующего пользователя с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Запрос на удаление пользователя с ID: {}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("Пользователь с ID {} удалён", id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            log.warn("Попытка удалить несуществующего пользователя с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    private UserDto convertToDto(UserEntity userEntity) {
        List<Long> functionIds = userEntity.getFunctions() != null ?
                userEntity.getFunctions().stream()
                        .map(core.entity.FunctionEntity::getId)
                        .collect(Collectors.toList()) : new ArrayList<>();


        return new UserDto(userEntity.getId(), userEntity.getName(), userEntity.getRole(), null , functionIds);
    }

    static class AuthRequest{
        private String name;
        private String password;

        public String getName(){ return name; }
        public void setName(String name){ this.name = name; }
        public String getPassword(){ return password; }
        public void setPassword(String password) { this.password = password; }
    }
}