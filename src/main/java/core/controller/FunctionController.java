package core.controller;

import core.entity.*;
import core.repository.*;
import core.dto.*;
import functions.ArrayTabulatedFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import core.dto.MathFunctionCreationDto;
import core.utils.MathFunctionRegistry;
import functions.MathFunction;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;

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

    @GetMapping("/math-functions") // GET запрос
    public ResponseEntity<List<String>> getAvailableMathFunctionNames() {
        log.info("Запрос на получение доступных имён MathFunction");
        List<String> availableNames = MathFunctionRegistry.getAvailableFunctionNames();
        return ResponseEntity.ok(availableNames);
    }

    @PostMapping("/from-math")
    public ResponseEntity<FunctionDto> createFunctionFromMath(@RequestBody MathFunctionCreationDto dto) {
        log.info("Запрос на создание табулированной функции из MathFunction: {}", dto);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка создания функции из MathFunction без аутентификации");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentUsername = auth.getName();
        UserEntity currentUser = userRepository.findByName(currentUsername);
        if (currentUser == null) {
            log.error("Ошибка: аутентифицированный пользователь '{}' не найден в БД", currentUsername);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (!currentUser.getId().equals(dto.getUserId())) {
            log.warn("Пользователь '{}' пытается создать функцию для другого пользователя (ожидаемый ID: {}, полученный в DTO: {})", currentUsername, currentUser.getId(), dto.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // 1. Получаем MathFunction по имени
            MathFunction mathFunction = MathFunctionRegistry.getFunctionByName(dto.getMathFunctionName());
            if (mathFunction == null) {
                throw new IllegalArgumentException("Функция с именем '" + dto.getMathFunctionName() + "' не найдена.");
            }

            if (MathFunctionRegistry.isTabulatedFunction(mathFunction)) {
                throw new IllegalArgumentException("Невозможно создать табулированную функцию из другой табулированной функции.");
            }

            if (dto.getXFrom() >= dto.getXTo()) {
                throw new IllegalArgumentException("xFrom должен быть строго меньше xTo.");
            }
            if (dto.getCount() < 2) {
                throw new IllegalArgumentException("Количество точек должно быть не менее 2.");
            }

            // --- ВЫБОР ФАБРИКИ НА ОСНОВЕ ПОЛЯ В DTO ---
            TabulatedFunctionFactory factory;
            String factoryType = dto.getFactoryType();
            if ("linkedlist".equalsIgnoreCase(factoryType)) {
                factory = new LinkedListTabulatedFunctionFactory();
            } else { // По умолчанию или "array"
                factory = new ArrayTabulatedFunctionFactory();
            }

            // 4. Используем выбранную фабрику для создания TabulatedFunction
            TabulatedFunction tabulatedFunction = factory.create(mathFunction, dto.getXFrom(), dto.getXTo(), dto.getCount());

            // 5. Создаём FunctionEntity (обёртка для табулированной функции)
            FunctionEntity functionEntity = new FunctionEntity();
            functionEntity.setFunctionName("FuncFromMath_" + dto.getMathFunctionName() + "_" + System.currentTimeMillis());
            functionEntity.setTypeFunction(FunctionEntity.FunctionType.tabular);
            functionEntity.setFunctionExpression(null);
            functionEntity.setUser(currentUser);

            // 6. Сохраняем FunctionEntity
            FunctionEntity savedFunctionEntity = functionRepository.save(functionEntity);

            // 7. Создаём точки для функции
            for (int i = 0; i < tabulatedFunction.getCount(); i++) {
                double x = tabulatedFunction.getX(i);
                double y = tabulatedFunction.getY(i);

                TabulatedFunctionEntity pointEntity = new TabulatedFunctionEntity();
                pointEntity.setFunction(savedFunctionEntity);
                pointEntity.setXVal(x);
                pointEntity.setYVal(y);

                tabulatedFunctionRepository.save(pointEntity);
            }

            // 8. Конвертируем FunctionEntity в FunctionDto и возвращаем
            FunctionDto savedDto = convertToDto(savedFunctionEntity);
            log.info("Табулированная функция из MathFunction создана и сохранена с ID: {}", savedDto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);

        } catch (IllegalArgumentException e) {
            log.error("Ошибка при создании функции из MathFunction: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при создании функции из MathFunction", e);
            throw e;
        }
    }

// ... (остальные методы)

    private boolean hasAccessToUser(Long targetUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Проверка доступа: пользователь не аутентифицирован.");
            return false;
        }
        String currentRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        log.debug("Проверка доступа: текущая роль = '{}'", currentRole);
        if ("ROLE_ADMIN".equals(currentRole)) {
            log.debug("Проверка доступа: доступ разрешён для администратора.");
            return true;
        }
        String currentUsername = auth.getName();
        UserEntity currentUser = userRepository.findByName(currentUsername);
        boolean hasAccess = currentUser != null && currentUser.getId().equals(targetUserId);
        log.debug("Проверка доступа: пользователь '{}' имеет доступ к ID {}: {}", currentUsername, targetUserId, hasAccess);
        return hasAccess;
    }

    @GetMapping
    public ResponseEntity<List<FunctionDto>> getFunctions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type) {
        log.info("Запрос на получение функций с параметрами : userId={}, name={}, type={}", userId, name, type);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка доступа без аутентификации к списку функций");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String currentRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        List<FunctionEntity> functions;
        if (userId != null) {
            if ("ROLE_ADMIN".equals(currentRole)) {
                functions = functionRepository.findByUser_Id(userId);
                log.debug("Админ запрашивает функции пользователя ID: {}", userId);
            } else {
                String currentUsername = auth.getName();
                UserEntity currentUser = userRepository.findByName(currentUsername);
                if (currentUser != null && currentUser.getId().equals(userId)) {
                    functions = functionRepository.findByUser_Id(userId);
                    log.debug("Пользователь '{}' запрашивает свои функции (ID: {})", currentUsername, userId);
                } else {
                    log.warn("Пользователь '{}' пытается получить функции пользователя {} (не его)", currentUsername, userId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        } else {
            if ("ROLE_ADMIN".equals(currentRole)) {
                functions = functionRepository.findAll();
                log.debug("Админ запрашивает все функции");
            } else {
                String currentUsername = auth.getName();
                UserEntity currentUser = userRepository.findByName(currentUsername);
                if (currentUser != null) {
                    functions = functionRepository.findByUser_Id(currentUser.getId());
                    log.debug("Пользователь '{}' запрашивает свои функции", currentUsername);
                } else {
                    log.error("Ошибка: аутентифицированный пользователь '{}' не найден в БД", currentUsername);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
        }
        if (name != null) {
            functions = functions.stream()
                    .filter(f -> f.getFunctionName().equals(name))
                    .collect(Collectors.toList());
        }
        if (type != null) {
            FunctionEntity.FunctionType typeEnum;
            try {
                typeEnum = FunctionEntity.FunctionType.valueOf(type.toLowerCase());
            } catch (IllegalArgumentException e) {
                log.warn("Неверный тип функции при поиске: {}", type);
                return ResponseEntity.badRequest().build();
            }
            functions = functions.stream()
                    .filter(f -> f.getTypeFunction() == typeEnum)
                    .collect(Collectors.toList());
        }
        List<FunctionDto> functionDtos = functions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("Возвращено {} функций", functionDtos.size());
        return ResponseEntity.ok(functionDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FunctionDto> getFunctionById(@PathVariable Long id) {
        log.info("Запрос на получение функции с ID: {} (проверка аутентификации и авторизации)", id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка доступа без аутентификации к функции ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<FunctionEntity> funcOpt = functionRepository.findById(id);
        if (!funcOpt.isPresent()) {
            log.warn("Функция с ID {} не найдена", id);
            return ResponseEntity.notFound().build();
        }
        FunctionEntity func = funcOpt.get();
        Long ownerId = func.getUser().getId();
        if (!hasAccessToUser(ownerId)) {
            log.warn("Пользователь '{}' не имеет доступа к функции {} принадлежащей пользователю {}", auth.getName(), id, ownerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        FunctionDto functionDto = convertToDto(func);
        log.info("Функция с ID {} найдена и доступ разрешен", id);
        return ResponseEntity.ok(functionDto);
    }

    @PostMapping
    public ResponseEntity<FunctionDto> createFunction(@RequestBody FunctionDto functionDto) {
        log.info("Запрос на создание функции: {}", functionDto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка создания функции без аутентификации");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String currentUsername = auth.getName();
        UserEntity currentUser = userRepository.findByName(currentUsername);
        if (currentUser == null) {
            log.error("Ошибка: аутентифицированный пользователь '{}' не найден в БД", currentUsername);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            if (!currentUser.getId().equals(functionDto.getUserId())) {
                log.warn("Пользователь '{}' пытается создать функцию для другого пользователя (ожидаемый ID: {}, полученный в DTO: {})", currentUsername, currentUser.getId(), functionDto.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        try {
            UserEntity targetUser = userRepository.findById(functionDto.getUserId())
                    .orElseThrow(() -> {
                        log.error("Пользователь с ID {} не найден в базе данных", functionDto.getUserId());
                        return new RuntimeException("Пользователь не найден");
                    });
            FunctionEntity funcEntity = new FunctionEntity();
            funcEntity.setUser(targetUser);
            String typeString = functionDto.getTypeFunction();
            log.info("Получен тип функции: {}", typeString);
            try {
                FunctionEntity.FunctionType typeEnum = FunctionEntity.FunctionType.valueOf(typeString.toLowerCase());
                funcEntity.setTypeFunction(typeEnum);
                log.info("Установлен тип функции: {}", typeEnum);
            } catch (IllegalArgumentException e) {
                log.error("Неверный тип функции: '{}'. Доступные типы: {}", typeString, Arrays.toString(FunctionEntity.FunctionType.values()));
                return ResponseEntity.badRequest().build();
            }
            funcEntity.setFunctionName(functionDto.getFunctionName());
            funcEntity.setFunctionExpression(functionDto.getFunctionExpression());
            log.info("Сохранение функции в базу...");
            FunctionEntity savedEntity = functionRepository.save(funcEntity);
            FunctionDto savedDto = convertToDto(savedEntity);
            log.info("Функция '{}' создана пользователем '{}' с ID: {}", savedDto.getFunctionName(), currentUsername, savedDto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
        } catch (RuntimeException e) {
            log.error("Ошибка при создании функции: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FunctionDto> updateFunction(@PathVariable Long id, @RequestBody FunctionDto functionDtoDetails) {
        log.info("Запрос на обновление функции с ID: {}", id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка обновления функции без аутентификации к функции ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<FunctionEntity> funcOpt = functionRepository.findById(id);
        if (!funcOpt.isPresent()) {
            log.warn("Попытка обновить несуществующую функцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        FunctionEntity func = funcOpt.get();

        if (!hasAccessToUser(func.getUser().getId())) {
            log.warn("Пользователь '{}' не имеет прав на обновление функции {} принадлежащей пользователю {}", auth.getName(), id, func.getUser().getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            if (!func.getUser().getId().equals(functionDtoDetails.getUserId())) {
                log.warn("Пользователь '{}' пытается передать функцию {} другому пользователю (ID: {})", auth.getName(), id, functionDtoDetails.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        func.setTypeFunction(FunctionEntity.FunctionType.valueOf(functionDtoDetails.getTypeFunction().toLowerCase()));
        func.setFunctionName(functionDtoDetails.getFunctionName());
        func.setFunctionExpression(functionDtoDetails.getFunctionExpression());

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            if (!func.getUser().getId().equals(functionDtoDetails.getUserId())) {
                UserEntity newOwner = userRepository.findById(functionDtoDetails.getUserId())
                        .orElseThrow(() -> new RuntimeException("Новый владелец не найден при обновлении функции"));
                func.setUser(newOwner);
                log.info("Владелец функции {} изменен на пользователя ID {}", id, newOwner.getId());
            }
        }

        FunctionEntity updatedEntity = functionRepository.save(func);
        FunctionDto updatedDto = convertToDto(updatedEntity);
        log.info("Функция с ID {} обновлена", id);
        return ResponseEntity.ok(updatedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        log.info("Запрос на удаление функции с ID: {} (проверка аутентификации и авторизации)", id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка удаления функции без аутентификации к функции ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<FunctionEntity> funcOpt = functionRepository.findById(id);
        if (!funcOpt.isPresent()) {
            log.warn("Попытка удалить несуществующую функцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        FunctionEntity func = funcOpt.get();
        Long ownerId = func.getUser().getId();
        if (!hasAccessToUser(ownerId)) {
            log.warn("Пользователь '{}' не имеет прав на удаление функции {} принадлежащей пользователю {}", auth.getName(), id, ownerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        functionRepository.delete(func);
        log.info("Функция с ID {} удалена пользователем {}", id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FunctionDto>> searchFunctions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String functionName,
            @RequestParam(required = false) String typeFunction) {
        log.info("Запрос на расширенный поиск функций с параметрами : userId={}, functionName={}, typeFunction={}", userId, functionName, typeFunction);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка поиска функций без аутентификации");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String currentRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        List<FunctionEntity> functions;
        if (userId != null) {
            if ("ROLE_ADMIN".equals(currentRole)) {
                functions = functionRepository.findByUser_Id(userId);
                log.debug("Админ ищет функции пользователя ID: {}", userId);
            } else {
                String currentUsername = auth.getName();
                UserEntity currentUser = userRepository.findByName(currentUsername);
                if (currentUser != null && currentUser.getId().equals(userId)) {
                    functions = functionRepository.findByUser_Id(userId);
                    log.debug("Пользователь '{}' ищет в своих функциях (ID: {})", currentUsername, userId);
                } else {
                    log.warn("Пользователь '{}' пытается искать функции пользователя {} (не его)", currentUsername, userId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        } else {
            if ("ROLE_ADMIN".equals(currentRole)) {
                functions = functionRepository.findAll();
                log.debug("Админ ищет во всех функциях");
            } else {
                String currentUsername = auth.getName();
                UserEntity currentUser = userRepository.findByName(currentUsername);
                if (currentUser != null) {
                    functions = functionRepository.findByUser_Id(currentUser.getId());
                    log.debug("Пользователь '{}' ищет в своих функциях", currentUsername);
                } else {
                    log.error("Ошибка: аутентифицированный пользователь '{}' не найден в БД", currentUsername);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
        }
        if (functionName != null) {
            functions = functions.stream()
                    .filter(f -> f.getFunctionName().toLowerCase().contains(functionName.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (typeFunction != null) {
            try {
                FunctionEntity.FunctionType typeEnum = FunctionEntity.FunctionType.valueOf(typeFunction.toLowerCase());
                functions = functions.stream()
                        .filter(f -> f.getTypeFunction() == typeEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                log.warn("Неверный тип функции при поиске: {}", typeFunction);
                return ResponseEntity.badRequest().build();
            }
        }
        List<FunctionDto> functionDtos = functions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("Возвращено {} функций", functionDtos.size());
        return ResponseEntity.ok(functionDtos);
    }

    private FunctionDto convertToDto(FunctionEntity funcEntity) {
        List<Long> tabulatedIds = funcEntity.getTabulatedValues() != null ?
                funcEntity.getTabulatedValues().stream()
                        .map(core.entity.TabulatedFunctionEntity::getId)
                        .collect(Collectors.toList()) : new ArrayList<>();
        List<Long> operationIds = funcEntity.getOperations() != null ?
                funcEntity.getOperations().stream()
                        .map(core.entity.OperationEntity::getId)
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