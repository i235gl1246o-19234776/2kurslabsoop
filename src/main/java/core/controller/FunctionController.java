package core.controller;

import core.dto.*;
import core.entity.*;
import core.repository.*;
import core.utils.MathFunctionRegistry;
import functions.*;
import functions.factory.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/functions")
public class FunctionController {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Autowired
    private OperationRepository operationRepository;

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

    // Проверка доступа к функции
    private boolean checkFunctionAccess(Long functionId) {
        if (isAdmin()) {
            return true;
        }

        UserEntity currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return false;
        }

        Optional<FunctionEntity> functionOpt = functionRepository.findById(functionId);
        if (functionOpt.isEmpty()) {
            return false;
        }

        return functionOpt.get().getUser().getId().equals(currentUser.getId());
    }

    // Проверка доступа к пользователю
    private boolean checkUserAccess(Long userId) {
        if (isAdmin()) {
            return true;
        }

        UserEntity currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return false;
        }

        return currentUser.getId().equals(userId);
    }

    // POST /api/functions - создание функции
    @PostMapping
    public ResponseEntity<FunctionDto> createFunction(@RequestBody FunctionDto functionDto) {
        log.info("Запрос на создание функции: {}", functionDto);

        UserEntity currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            log.warn("Попытка создания функции без аутентификации");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Проверка, что пользователь создает функцию для себя или является админом
        if (!isAdmin() && !currentUser.getId().equals(functionDto.getUserId())) {
            log.warn("Пользователь '{}' пытается создать функцию для другого пользователя", currentUser.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // Получаем пользователя-владельца
            Optional<UserEntity> targetUserOpt = userRepository.findById(functionDto.getUserId());
            if (targetUserOpt.isEmpty()) {
                log.error("Пользователь с ID {} не найден в базе данных", functionDto.getUserId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            UserEntity targetUser = targetUserOpt.get();

            // Создаем функцию
            FunctionEntity funcEntity = new FunctionEntity();
            funcEntity.setUser(targetUser);

            // Устанавливаем тип функции
            try {
                FunctionEntity.FunctionType typeEnum = FunctionEntity.FunctionType.valueOf(functionDto.getTypeFunction().toLowerCase());
                funcEntity.setTypeFunction(typeEnum);
            } catch (IllegalArgumentException e) {
                log.error("Неверный тип функции: '{}'. Доступные типы: {}",
                        functionDto.getTypeFunction(), Arrays.toString(FunctionEntity.FunctionType.values()));
                return ResponseEntity.badRequest().build();
            }

            funcEntity.setFunctionName(functionDto.getFunctionName());
            funcEntity.setFunctionExpression(functionDto.getFunctionExpression());

            // Сохраняем функцию
            FunctionEntity savedEntity = functionRepository.save(funcEntity);
            FunctionDto savedDto = convertToDto(savedEntity);

            log.info("Функция '{}' создана пользователем '{}' с ID: {}",
                    savedDto.getFunctionName(), currentUser.getName(), savedDto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
        } catch (Exception e) {
            log.error("Ошибка при создании функции: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/functions - получение всех функций пользователя
    @GetMapping
    public ResponseEntity<List<FunctionDto>> getFunctions(
            @RequestParam(required = false) Long userId) {

        log.info("Запрос на получение функций с параметром userId={}", userId);

        UserEntity currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            log.warn("Попытка доступа без аутентификации к списку функций");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<FunctionEntity> functions;

        if (userId != null) {
            if (isAdmin() || currentUser.getId().equals(userId)) {
                functions = functionRepository.findByUser_Id(userId);
            } else {
                log.warn("Пользователь '{}' пытается получить функции пользователя {}",
                        currentUser.getName(), userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            // Если userId не указан, возвращаем функции текущего пользователя
            functions = functionRepository.findByUser_Id(currentUser.getId());
        }

        List<FunctionDto> functionDtos = functions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("Возвращено {} функций", functionDtos.size());
        return ResponseEntity.ok(functionDtos);
    }

    // GET /api/functions/{id} - получение функции по ID
    @GetMapping("/{id}")
    public ResponseEntity<FunctionDto> getFunctionById(@PathVariable Long id) {
        log.info("Запрос на получение функции с ID: {}", id);

        if (!checkFunctionAccess(id)) {
            log.warn("Пользователь не имеет доступа к функции с ID {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<FunctionEntity> functionOpt = functionRepository.findById(id);
        if (functionOpt.isPresent()) {
            FunctionDto functionDto = convertToDto(functionOpt.get());
            log.info("Функция с ID {} найдена", id);
            return ResponseEntity.ok(functionDto);
        } else {
            log.warn("Функция с ID {} не найдена", id);
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/functions/{id} - обновление функции
    @PutMapping("/{id}")
    public ResponseEntity<FunctionDto> updateFunction(
            @PathVariable Long id,
            @RequestBody FunctionDto functionDtoDetails) {

        log.info("Запрос на обновление функции с ID: {}", id);

        if (!checkFunctionAccess(id)) {
            log.warn("Пользователь не имеет доступа к функции с ID {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<FunctionEntity> funcOpt = functionRepository.findById(id);
        if (funcOpt.isEmpty()) {
            log.warn("Попытка обновить несуществующую функцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        FunctionEntity func = funcOpt.get();

        // Проверяем, что пользователь не пытается передать функцию другому пользователю
        if (!isAdmin() && !func.getUser().getId().equals(functionDtoDetails.getUserId())) {
            log.warn("Пользователь '{}' пытается передать функцию {} другому пользователю",
                    getAuthenticatedUser().getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Обновляем данные функции
        try {
            FunctionEntity.FunctionType typeEnum = FunctionEntity.FunctionType.valueOf(functionDtoDetails.getTypeFunction().toLowerCase());
            func.setTypeFunction(typeEnum);
        } catch (IllegalArgumentException e) {
            log.error("Неверный тип функции: '{}'", functionDtoDetails.getTypeFunction());
            return ResponseEntity.badRequest().build();
        }

        func.setFunctionName(functionDtoDetails.getFunctionName());
        func.setFunctionExpression(functionDtoDetails.getFunctionExpression());

        // Если администратор меняет владельца
        if (isAdmin() && !func.getUser().getId().equals(functionDtoDetails.getUserId())) {
            Optional<UserEntity> newOwnerOpt = userRepository.findById(functionDtoDetails.getUserId());
            if (newOwnerOpt.isEmpty()) {
                log.error("Новый владелец с ID {} не найден", functionDtoDetails.getUserId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            func.setUser(newOwnerOpt.get());
        }

        FunctionEntity updatedEntity = functionRepository.save(func);
        FunctionDto updatedDto = convertToDto(updatedEntity);

        log.info("Функция с ID {} обновлена", id);
        return ResponseEntity.ok(updatedDto);
    }

    // DELETE /api/functions/{id} - удаление функции
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        log.info("Запрос на удаление функции с ID: {}", id);

        if (!checkFunctionAccess(id)) {
            log.warn("Пользователь не имеет доступа к функции с ID {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<FunctionEntity> funcOpt = functionRepository.findById(id);
        if (funcOpt.isEmpty()) {
            log.warn("Попытка удалить несуществующую функцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        functionRepository.delete(funcOpt.get());
        log.info("Функция с ID {} удалена", id);
        return ResponseEntity.noContent().build();
    }

    // Конвертация в DTO
    private FunctionDto convertToDto(FunctionEntity funcEntity) {
        List<Long> tabulatedIds = funcEntity.getTabulatedValues() != null ?
                funcEntity.getTabulatedValues().stream()
                        .map(TabulatedFunctionEntity::getId)
                        .collect(Collectors.toList()) : new ArrayList<>();

        List<Long> operationIds = funcEntity.getOperations() != null ?
                funcEntity.getOperations().stream()
                        .map(OperationEntity::getId)
                        .collect(Collectors.toList()) : new ArrayList<>();

        Long userId = funcEntity.getUser() != null ? funcEntity.getUser().getId() : null;

        return new FunctionDto(
                funcEntity.getId(),
                userId,
                funcEntity.getTypeFunction().name().toLowerCase(),
                funcEntity.getFunctionName(),
                funcEntity.getFunctionExpression(),
                tabulatedIds,
                operationIds
        );
    }
}