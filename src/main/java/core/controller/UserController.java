package core.controller;

import core.dto.UserDto;
import core.entity.UserEntity;
import core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).+$";

    // Получение аутентифицированного пользователя
    private UserEntity getAuthenticatedUser() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        return (UserEntity) request.getAttribute("authenticatedUser");
    }

    // Проверка прав администратора
    private boolean isAdmin() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        String role = (String) request.getAttribute("userRole");
        return "ADMIN".equals(role);
    }

    // Проверка прав владельца или администратора
    private boolean isOwnerOrAdmin(Long targetUserId) {
        UserEntity currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return false;
        }
        return currentUser.getId().equals(targetUserId) || isAdmin();
    }

    // GET /api/users/name/{username} - аутентификация через Basic Auth
    @GetMapping("/name/{username}")
    public ResponseEntity<?> authenticateUser(@PathVariable String username, HttpServletRequest request) {
        log.info("Запрос на аутентификацию для пользователя: {}", username);

        // Проверка заголовка Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            log.warn("Отсутствует заголовок авторизации для пользователя: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"Restricted\"")
                    .body(Map.of("error", "Authorization header required"));
        }

        // Декодирование креденшиалов
        try {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);

            if (values.length < 2) {
                log.warn("Неверный формат учетных данных для: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"Restricted\"")
                        .body(Map.of("error", "Invalid credentials format"));
            }

            String inputUsername = values[0].trim();
            String inputPassword = values[1].trim();

            // Проверка соответствия имени в URL и заголовке
            if (!inputUsername.equals(username)) {
                log.warn("Несоответствие имени в URL ({}) и в заголовке ({})", username, inputUsername);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username in URL does not match Authorization header"));
            }

            if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                log.warn("Пустое имя или пароль для аутентификации: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"Restricted\"")
                        .body(Map.of("error", "Username and password required"));
            }

            // Аутентификация
            UserEntity userEntity = userRepository.findByName(inputUsername);
            Optional<UserEntity> userOpt = Optional.ofNullable(userEntity);
            if (userOpt.isPresent() && passwordEncoder.matches(inputPassword, userOpt.get().getPasswordHash())) {
                UserDto userDto = convertToDto(userOpt.get());
                log.info("Успешная аутентификация: {}", inputUsername);
                return ResponseEntity.ok(userDto);
            }
        } catch (IllegalArgumentException e) {
            log.error("Ошибка декодирования Base64: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"Restricted\"")
                    .body(Map.of("error", "Invalid Base64 encoding"));
        }

        log.warn("Неудачная аутентификация для: {}", username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", "Basic realm=\"Restricted\"")
                .body(Map.of("error", "Invalid username or password"));
    }

    // GET /api/users - получение всех пользователей (только для админа)
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        if (!isAdmin()) {
            log.warn("Попытка получения списка пользователей без прав администратора");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<UserEntity> users = userRepository.findAll();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("Возвращено {} пользователей", userDtos.size());
        return ResponseEntity.ok(userDtos);
    }

    // GET /api/users/{id} - получение пользователя по ID (только для админа)
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Запрос на получение пользователя с ID: {}", id);
        if (!isAdmin()) {
            log.warn("Попытка получения пользователя по ID без прав администратора");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
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

    // GET /api/users/exist/{name} - проверка существования пользователя (только для админа)
    @GetMapping("/exist/{name}")
    public ResponseEntity<Boolean> userExists(@PathVariable String name) {
        log.info("Проверка существования пользователя: {}", name);
        if (!isAdmin()) {
            log.warn("Попытка проверки существования пользователя без прав администратора");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean exists = userRepository.existsByName(name);
        log.info("Пользователь '{}' существует: {}", name, exists);
        return ResponseEntity.ok(exists);
    }

    // POST /api/users - создание пользователя (только для админа)
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        log.info("Запрос на создание пользователя: {}", userDto.getName());
        if (!isAdmin()) {
            log.warn("Попытка создания пользователя без прав администратора");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Запрещаем создание администраторов через API
        if ("ADMIN".equalsIgnoreCase(userDto.getRole())) {
            log.warn("Попытка создания администратора через API");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new UserDto(null, null, null, "Создание администраторов через API запрещено", null));
        }

        if (userRepository.existsByName(userDto.getName())) {
            log.warn("Пользователь с именем '{}' уже существует", userDto.getName());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setName(userDto.getName());
        userEntity.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
        userEntity.setRole("USER"); // По умолчанию всегда USER
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

    // PUT /api/users/{id} - обновление пользователя
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDtoDetails) {
        log.info("Запрос на обновление пользователя с ID: {}", id);
        if (!isOwnerOrAdmin(id)) {
            log.warn("Пользователь '{}' не имеет прав на обновление пользователя с ID: {}",
                    getAuthenticatedUser().getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<UserEntity> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            user.setName(userDtoDetails.getName());
            if (userDtoDetails.getPassword() != null && !userDtoDetails.getPassword().isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(userDtoDetails.getPassword()));
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

    // DELETE /api/users/{id} - удаление пользователя
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Запрос на удаление пользователя с ID: {}", id);
        UserEntity currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            log.warn("Попытка удаления пользователя без аутентификации (ID: {})", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!currentUser.getId().equals(id) && !isAdmin()) {
            log.warn("Пользователь '{}' не имеет прав на удаление пользователя с ID: {}",
                    currentUser.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("Пользователь с ID {} удалён", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Попытка удалить несуществующего пользователя с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/users/{id}/role - обновление роли пользователя (только для админа)
    @PutMapping("/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        log.info("Запрос на обновление роли пользователя с ID: {} на роль: {}", id, role);
        if (!isAdmin()) {
            log.warn("Попытка обновления роли пользователя без прав администратора");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<UserEntity> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            log.warn("Попытка обновить роль несуществующего пользователя с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        UserEntity user = userOpt.get();
        if (!"USER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            log.warn("Попытка установить недопустимую роль '{}' пользователю с ID: {}", role, id);
            return ResponseEntity.badRequest().build();
        }
        user.setRole(role);
        try {
            UserEntity updatedEntity = userRepository.save(user);
            UserDto updatedDto = convertToDto(updatedEntity);
            log.info("Роль пользователя с ID {} обновлена на '{}'", id, role);
            return ResponseEntity.ok(updatedDto);
        } catch (Exception e) {
            log.error("Ошибка при обновлении роли пользователя с ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private UserDto convertToDto(UserEntity userEntity) {
        List<Long> functionIds = userEntity.getFunctions() != null ?
                userEntity.getFunctions().stream()
                        .map(core.entity.FunctionEntity::getId)
                        .collect(Collectors.toList()) : new ArrayList<>();
        return new UserDto(userEntity.getId(), userEntity.getName(), userEntity.getRole(), null, functionIds);
    }
}