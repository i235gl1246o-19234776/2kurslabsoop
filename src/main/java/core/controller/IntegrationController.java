// src/main/java/core/controller/IntegrationController.java
package core.controller;

import core.entity.FunctionEntity;
import core.entity.TabulatedFunctionEntity;
import core.entity.UserEntity;
import core.repository.FunctionRepository;
import core.repository.TabulatedFunctionRepository;
import core.repository.UserRepository;
import core.dto.IntegrationRequestDto;
import core.dto.IntegrationResultDto;
import functions.TabulatedFunction;
import functions.MathFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import operations.ParallelIntegrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/operations")
public class IntegrationController {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    private boolean hasAccessToFunction(Long functionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Проверка доступа: пользователь не аутентифицирован.");
            return false;
        }

        String currentRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        log.debug("Проверка доступа к функции {}: текущая роль = '{}'", functionId, currentRole);

        if ("ROLE_ADMIN".equals(currentRole)) {
            log.debug("Проверка доступа: доступ разрешён для администратора.");
            return true;
        }

        String currentUsername = auth.getName();
        UserEntity currentUser = userRepository.findByName(currentUsername);
        if (currentUser == null) {
            log.warn("Проверка доступа: аутентифицированный пользователь '{}' не найден в БД.", currentUsername);
            return false;
        }

        Optional<FunctionEntity> functionOpt = functionRepository.findById(functionId);
        if (functionOpt.isEmpty()) {
            log.warn("Проверка доступа: функция с ID {} не найдена.", functionId);
            return false;
        }

        UserEntity owner = functionOpt.get().getUser();
        boolean hasAccess = owner.getId().equals(currentUser.getId());
        log.debug("Проверка доступа: пользователь '{}' имеет доступ к функции {}: {}", currentUsername, functionId, hasAccess);
        return hasAccess;
    }

    private TabulatedFunction loadTabulatedFunctionFromDB(Long functionId) {
        if (functionId == null) {
            log.error("ID функции не может быть null");
            return null;
        }

        Optional<FunctionEntity> functionOpt = functionRepository.findById(functionId);
        if (functionOpt.isEmpty()) {
            log.warn("Функция с ID {} не найдена в базе данных", functionId);
            return null;
        }

        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        if (points.isEmpty()) {
            log.warn("Функция с ID {} не содержит точек", functionId);
            return null;
        }

        // Сортируем точки по X
        points.sort((p1, p2) -> Double.compare(p1.getXVal(), p2.getXVal()));

        double[] xValues = points.stream().mapToDouble(TabulatedFunctionEntity::getXVal).toArray();
        double[] yValues = points.stream().mapToDouble(TabulatedFunctionEntity::getYVal).toArray();

        // Используем ArrayTabulatedFunctionFactory для создания TabulatedFunction из точек
        return new ArrayTabulatedFunctionFactory().create(xValues, yValues);
    }

    private TabulatedFunctionFactory getFactoryByType(String factoryType) {
        if ("linkedlist".equalsIgnoreCase(factoryType)) {
            return new LinkedListTabulatedFunctionFactory();
        } else {
            return new ArrayTabulatedFunctionFactory();
        }
    }

    @PostMapping("/integrate")
    public ResponseEntity<IntegrationResultDto> performIntegration(
            @RequestBody IntegrationRequestDto requestDto) {
        log.info("Запрос на вычисление интеграла: {}", requestDto);

        // Проверяем доступ к функции
        if (!hasAccessToFunction(requestDto.getFunctionId())) {
            log.warn("Пользователь не имеет доступа к функции с ID {}", requestDto.getFunctionId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Загружаем функцию
        TabulatedFunction function = loadTabulatedFunctionFromDB(requestDto.getFunctionId());
        if (function == null) {
            log.error("Функция с ID {} не найдена или не содержит точек", requestDto.getFunctionId());
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // Ограничиваем количество потоков от 1 до 16
            int threadCount = Math.max(1, Math.min(16, requestDto.getThreadCount()));

            // Преобразуем TabulatedFunction в MathFunction для интегрирования
            MathFunction mathFunction = (MathFunction) function;

            // Выполняем интегрирование
            long startTime = System.nanoTime();
            double result = ParallelIntegrator.integrateWithFixedPool(
                    mathFunction,
                    requestDto.getFromX(),
                    requestDto.getToX(),
                    function.getCount() * 10, // Увеличиваем количество точек для точности
                    threadCount
            ).result();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // В миллисекундах

            log.info("Интеграл вычислен успешно. Значение: {}, время: {} мс", result, duration);

            return ResponseEntity.ok(new IntegrationResultDto(result, duration));
        } catch (Exception e) {
            log.error("Ошибка при вычислении интеграла: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
}