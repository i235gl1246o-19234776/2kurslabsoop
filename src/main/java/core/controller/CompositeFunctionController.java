package core.controller;

import core.dto.CompositeFunctionDto;
import core.dto.FunctionDto;
import core.entity.FunctionEntity;
import core.entity.UserEntity;
import core.repository.FunctionRepository;
import core.repository.UserRepository;
import functions.CompositeFunction;
import functions.MathFunction;
import core.utils.MathFunctionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/functions")
public class CompositeFunctionController {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

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

    // POST /api/functions/composite - создание сложной функции
    @PostMapping("/composite")
    public ResponseEntity<FunctionDto> createCompositeFunction(
            @RequestBody CompositeFunctionDto compositeDto) {

        log.info("Запрос на создание сложной функции: {}", compositeDto);

        UserEntity currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            log.warn("Попытка создания сложной функции без аутентификации");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Проверяем доступ к пользователю
        if (!isAdmin() && !currentUser.getId().equals(compositeDto.getUserId())) {
            log.warn("Пользователь '{}' пытается создать сложную функцию для другого пользователя", currentUser.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // Проверяем существование пользователя
            Optional<UserEntity> userOpt = userRepository.findById(compositeDto.getUserId());
            if (userOpt.isEmpty()) {
                log.error("Пользователь с ID {} не найден", compositeDto.getUserId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            UserEntity user = userOpt.get();

            // Получаем базовую и внешнюю функции
            MathFunction baseFunction = MathFunctionRegistry.getFunctionByName(compositeDto.getBaseFunctionName());
            MathFunction outerFunction = MathFunctionRegistry.getFunctionByName(compositeDto.getOuterFunctionName());

            // Создаем сложную функцию
            CompositeFunction compositeFunction = new CompositeFunction(outerFunction, baseFunction);

            // Создаем сущность функции
            FunctionEntity functionEntity = new FunctionEntity();
            functionEntity.setUser(user);
            functionEntity.setTypeFunction(FunctionEntity.FunctionType.analytic);
            functionEntity.setFunctionName(compositeDto.getCustomName() != null ?
                    compositeDto.getCustomName() :
                    "Сложная функция " + System.currentTimeMillis());
            functionEntity.setFunctionExpression(
                    compositeDto.getOuterFunctionName() + " ∘ " + compositeDto.getBaseFunctionName()
            );

            // Сохраняем в базу данных
            FunctionEntity savedEntity = functionRepository.save(functionEntity);

            // Создаем DTO для ответа
            FunctionDto resultDto = new FunctionDto();
            resultDto.setId(savedEntity.getId());
            resultDto.setUserId(user.getId());
            resultDto.setTypeFunction("analytic");
            resultDto.setFunctionName(functionEntity.getFunctionName());
            resultDto.setFunctionExpression(functionEntity.getFunctionExpression());

            log.info("Сложная функция создана успешно с ID: {}", resultDto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(resultDto);
        } catch (Exception e) {
            log.error("Ошибка при создании сложной функции: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new FunctionDto(null, null, null, "Ошибка: " + e.getMessage(), null, null, null));
        }
    }
}