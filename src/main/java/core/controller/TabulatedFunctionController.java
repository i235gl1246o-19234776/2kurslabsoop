package core.controller;

import core.entity.*;
import core.repository.*;
import core.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

import functions.MathFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import core.utils.MathFunctionRegistry;

@Slf4j
@RestController
@RequestMapping("/api/tabulated-points")
public class TabulatedFunctionController {
    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;
    @Autowired
    private FunctionRepository functionRepository;
    @Autowired
    private UserRepository userRepository;

    // Статический реестр математических функций
    private static final Map<String, MathFunction> MATH_FUNCTION_MAP = new HashMap<>();
    static {
        MATH_FUNCTION_MAP.put("Тождественная функция", new functions.IdentityFunction());
        MATH_FUNCTION_MAP.put("Квадратичная функция", new functions.SqrFunction());
        MATH_FUNCTION_MAP.put("IdentityFunction", new functions.IdentityFunction());
        MATH_FUNCTION_MAP.put("SqrFunction", new functions.SqrFunction());
        // Добавьте другие функции по необходимости
    }

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

    @GetMapping("/function/{functionId}")
    public ResponseEntity<List<TabulatedFunctionDto>> getAllPointsByFunctionId(@PathVariable Long functionId) {
        log.info("Запрос на получение всех точек для функции с ID: {}", functionId);
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                log.warn("Попытка доступа без аутентификации к точкам функции с ID: {}", functionId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Optional<FunctionEntity> functionOpt = functionRepository.findById(functionId);
            if (functionOpt.isEmpty()) {
                log.warn("Функция с ID {} не найдена при запросе точек", functionId);
                return ResponseEntity.notFound().build();
            }
            if (!hasAccessToFunction(functionId)) {
                log.warn("Пользователь '{}' пытается получить точки функции {}, к которой не имеет доступа", auth.getName(), functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
            List<TabulatedFunctionDto> pointDtos = points.stream()
                    .map(point -> new TabulatedFunctionDto(point.getId(), point.getFunction().getId(), point.getXVal(), point.getYVal()))
                    .collect(Collectors.toList());
            log.info("Получено {} точек для функции с ID: {}", pointDtos.size(), functionId);
            return ResponseEntity.ok(pointDtos);
        } catch (Exception e) {
            log.error("Внутренняя ошибка при загрузке точек функции с ID: {}", functionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/function/{functionId}/x/{xValue}")
    public ResponseEntity<TabulatedFunctionDto> getPointByFunctionIdAndX(@PathVariable Long functionId, @PathVariable Double xValue) {
        log.info("Запрос на поиск точки для функции ID: {} по X: {}", functionId, xValue);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка доступа без аутентификации к точке функции ID: {}", functionId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<FunctionEntity> functionOpt = functionRepository.findById(functionId);
        if (functionOpt.isEmpty()) {
            log.warn("Функция с ID {} не найдена при запросе точки", functionId);
            return ResponseEntity.notFound().build();
        }
        if (!hasAccessToFunction(functionId)) {
            log.warn("Пользователь '{}' пытается получить точку функции {}, к которой не имеет доступа", auth.getName(), functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        Optional<TabulatedFunctionEntity> pointOpt = points.stream()
                .filter(p -> p.getXVal().equals(xValue))
                .findFirst();
        if (pointOpt.isPresent()) {
            TabulatedFunctionDto pointDto = convertToDto(pointOpt.get());
            log.info("Точка найдена для функции ID: {} и X: {}", functionId, xValue);
            return ResponseEntity.ok(pointDto);
        } else {
            log.warn("Точка с X={} не найдена для функции ID: {}", xValue, functionId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/function/{functionId}/range")
    public ResponseEntity<List<TabulatedFunctionDto>> getPointsInRange(@PathVariable Long functionId, @RequestParam Double fromX, @RequestParam Double toX) {
        log.info("Запрос на получение точек для функции ID: {} в диапазоне от {} до {}", functionId, fromX, toX);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка доступа без аутентификации к точкам функции ID: {} в диапазоне", functionId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<FunctionEntity> functionOpt = functionRepository.findById(functionId);
        if (functionOpt.isEmpty()) {
            log.warn("Функция с ID {} не найдена при запросе точек в диапазоне", functionId);
            return ResponseEntity.notFound().build();
        }
        if (!hasAccessToFunction(functionId)) {
            log.warn("Пользователь '{}' пытается получить точки в диапазоне функции {}, к которой не имеет доступа", auth.getName(), functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        List<TabulatedFunctionDto> rangeDtos = points.stream()
                .filter(p -> p.getXVal() >= fromX && p.getXVal() <= toX)
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("Найдено {} точек в диапазоне [{}, {}] для функции ID: {}", rangeDtos.size(), fromX, toX, functionId);
        return ResponseEntity.ok(rangeDtos);
    }

    @PostMapping
    public ResponseEntity<TabulatedFunctionDto> createPoint(@RequestBody TabulatedFunctionDto pointDto) {
        log.info("Запрос на создание точки для функции ID: {}", pointDto.getFunctionId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка создания точки без аутентификации");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (pointDto.getXVal() == null || pointDto.getYVal() == null) {
            log.error("xVal и/или yVal не могут быть null при создании точки");
            return ResponseEntity.badRequest().build();
        }
        Optional<FunctionEntity> functionOpt = functionRepository.findById(pointDto.getFunctionId());
        if (functionOpt.isEmpty()) {
            log.warn("Функция с ID {} не найдена при создании точки", pointDto.getFunctionId());
            return ResponseEntity.notFound().build();
        }
        if (!hasAccessToFunction(pointDto.getFunctionId())) {
            log.warn("Пользователь '{}' пытается создать точку для функции {}, к которой не имеет доступа", auth.getName(), pointDto.getFunctionId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        TabulatedFunctionEntity pointEntity = new TabulatedFunctionEntity();
        pointEntity.setFunction(functionOpt.get());
        pointEntity.setXVal(pointDto.getXVal());
        pointEntity.setYVal(pointDto.getYVal());
        TabulatedFunctionEntity savedEntity = tabulatedFunctionRepository.save(pointEntity);
        TabulatedFunctionDto savedDto = convertToDto(savedEntity);
        log.info("Точка создана с ID: {} для функции ID: {}", savedDto.getId(), savedDto.getFunctionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TabulatedFunctionDto> updatePoint(@PathVariable Long id, @RequestBody TabulatedFunctionDto pointDtoDetails) {
        log.info("Запрос на обновление точки с ID: {}", id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка обновления точки без аутентификации к точке ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<TabulatedFunctionEntity> pointOpt = tabulatedFunctionRepository.findById(id);
        if (pointOpt.isPresent()) {
            TabulatedFunctionEntity point = pointOpt.get();
            Long functionId = point.getFunction().getId();
            if (!hasAccessToFunction(functionId)) {
                log.warn("Пользователь '{}' не имеет прав на обновление точки {}, принадлежащей функции {}", auth.getName(), id, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            point.setXVal(pointDtoDetails.getXVal());
            point.setYVal(pointDtoDetails.getYVal());
            TabulatedFunctionEntity updatedEntity = tabulatedFunctionRepository.save(point);
            TabulatedFunctionDto updatedDto = convertToDto(updatedEntity);
            log.info("Точка с ID {} обновлена", id);
            return ResponseEntity.ok(updatedDto);
        } else {
            log.warn("Попытка обновить несуществующую точку с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoint(@PathVariable Long id) {
        log.info("Запрос на удаление точки с ID: {}", id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка удаления точки без аутентификации к точке ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<TabulatedFunctionEntity> pointOpt = tabulatedFunctionRepository.findById(id);
        if (pointOpt.isPresent()) {
            TabulatedFunctionEntity point = pointOpt.get();
            Long functionId = point.getFunction().getId();
            if (!hasAccessToFunction(functionId)) {
                log.warn("Пользователь '{}' не имеет прав на удаление точки {}, принадлежащей функции {}", auth.getName(), id, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            tabulatedFunctionRepository.delete(point);
            log.info("Точка с ID {} удалена", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Попытка удалить несуществующую точку с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/function/{functionId}")
    public ResponseEntity<Void> deleteAllPointsByFunctionId(@PathVariable Long functionId) {
        log.info("Запрос на удаление всех точек для функции ID: {}", functionId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка удаления точек без аутентификации для функции ID: {}", functionId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<FunctionEntity> functionOpt = functionRepository.findById(functionId);
        if (functionOpt.isEmpty()) {
            log.warn("Функция с ID {} не найдена при удалении точек", functionId);
            return ResponseEntity.notFound().build();
        }
        if (!hasAccessToFunction(functionId)) {
            log.warn("Пользователь '{}' пытается удалить точки функции {}, к которой не имеет доступа", auth.getName(), functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        tabulatedFunctionRepository.deleteAll(points);
        log.info("Удалено {} точек для функции ID: {}", points.size(), functionId);
        return ResponseEntity.noContent().build();
    }

    private TabulatedFunctionDto convertToDto(TabulatedFunctionEntity pointEntity) {
        Long functionId = pointEntity.getFunction() != null ? pointEntity.getFunction().getId() : null;
        return new TabulatedFunctionDto(
                pointEntity.getId(),
                functionId,
                pointEntity.getXVal(),
                pointEntity.getYVal()
        );
    }



    @PostMapping("/calculate")
    public ResponseEntity<String> calculateAndSaveTabulatedPoints(@RequestBody CalculatePointsRequestDto requestDto) {
        log.info("Запрос на вычисление точек для функции ID: {}, функции: {}, интервал: [{}, {}], количество точек: {}",
                requestDto.getFunctionId(), requestDto.getMathFunctionName(),
                requestDto.getStart(), requestDto.getEnd(), requestDto.getCount());

        // Проверяем доступ к функции
        if (!hasAccessToFunction(requestDto.getFunctionId())) {
            log.warn("Пользователь не имеет доступа к функции с ID {}", requestDto.getFunctionId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Получаем математическую функцию по имени
        MathFunction mathFunction = MATH_FUNCTION_MAP.get(requestDto.getMathFunctionName());
        if (mathFunction == null) {
            log.warn("Неизвестное имя математической функции: {}", requestDto.getMathFunctionName());
            return ResponseEntity.badRequest().body("Неизвестное имя математической функции");
        }

        // Проверяем параметры
        if (requestDto.getStart() >= requestDto.getEnd() || requestDto.getCount() <= 0) {
            log.warn("Неверные параметры интервала: start={}, end={}, count={}",
                    requestDto.getStart(), requestDto.getEnd(), requestDto.getCount());
            return ResponseEntity.badRequest().body("Неверные параметры интервала");
        }

        // Определяем фабрику
        TabulatedFunctionFactory factory;
        if ("linked-list".equals(requestDto.getFactoryType())) {
            factory = new LinkedListTabulatedFunctionFactory();
            log.debug("Используется LinkedListTabulatedFunctionFactory");
        } else {
            factory = new ArrayTabulatedFunctionFactory();
            log.debug("Используется ArrayTabulatedFunctionFactory");
        }

        try {
            // Вычисляем и сохраняем точки
            Optional<FunctionEntity> functionOpt = functionRepository.findById(requestDto.getFunctionId());
            if (functionOpt.isEmpty()) {
                log.error("Функция с ID {} не найдена", requestDto.getFunctionId());
                return ResponseEntity.notFound().build();
            }

            FunctionEntity functionEntity = functionOpt.get();
            functionEntity.setTypeFunction(FunctionEntity.FunctionType.tabular);
            functionRepository.save(functionEntity);

            // Вычисляем точки
            double[] xValues = new double[requestDto.getCount()];
            double[] yValues = new double[requestDto.getCount()];
            double step = (requestDto.getEnd() - requestDto.getStart()) / (requestDto.getCount() - 1);

            for (int i = 0; i < requestDto.getCount(); i++) {
                xValues[i] = requestDto.getStart() + i * step;
                yValues[i] = mathFunction.apply(xValues[i]);

                // Создаем и сохраняем точку
                TabulatedFunctionEntity pointEntity = new TabulatedFunctionEntity();
                pointEntity.setFunction(functionEntity);
                pointEntity.setXVal(xValues[i]);
                pointEntity.setYVal(yValues[i]);
                tabulatedFunctionRepository.save(pointEntity);
            }

            log.info("Вычислено и сохранено {} точек для функции ID: {}", requestDto.getCount(), requestDto.getFunctionId());
            return ResponseEntity.ok("Tabulated points calculated and saved successfully");
        } catch (Exception e) {
            log.error("Ошибка при вычислении и сохранении точек: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при вычислении точек");
        }
    }
}