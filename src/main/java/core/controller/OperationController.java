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
import functions.TabulatedFunction;
import functions.Point;
import functions.factory.TabulatedFunctionFactory;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import operations.TabulatedDifferentialOperator;
import exception.InconsistentFunctionsException;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/operations")
public class OperationController {
    @Autowired
    private OperationRepository operationRepository;
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

    // --- Метод для бинарных операций (сложение, вычитание и т.д.) ---
    @PostMapping("/execute")
    public ResponseEntity<List<TabulatedFunctionDto>> performBinaryOperation(
            @RequestBody ExecuteOperationRequestDto requestDto) {
        log.info("Запрос на выполнение бинарной операции '{}' с DTO: {}", requestDto.getOperation(), requestDto);
        Long functionIdA = requestDto.getFunctionIdA();
        Long functionIdB = requestDto.getFunctionIdB();
        String factoryType = requestDto.getFactoryType();

        // Проверяем, что оба операнда существуют
        if (functionIdA == null || functionIdB == null) {
            log.error("Оба операнда должны быть указаны для бинарной операции");
            return ResponseEntity.badRequest().body(null);
        }

        // 1. Проверить доступ к обеим функциям-операндам
        if (!hasAccessToFunction(functionIdA) || !hasAccessToFunction(functionIdB)) {
            log.warn("Пользователь не имеет доступа к одной из функций-операндов: {} или {}", functionIdA, functionIdB);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        // 2. Загрузить функции-операнды из БД
        TabulatedFunction operand1 = loadTabulatedFunctionFromDB(functionIdA);
        if (operand1 == null) {
            log.error("Операнд 1 с ID {} не найден или не содержит точек.", functionIdA);
            return ResponseEntity.badRequest().body(null);
        }

        TabulatedFunction operand2 = loadTabulatedFunctionFromDB(functionIdB);
        if (operand2 == null) {
            log.error("Операнд 2 с ID {} не найден или не содержит точек.", functionIdB);
            return ResponseEntity.badRequest().body(null);
        }

        // 3. Выбрать фабрику для результата
        TabulatedFunctionFactory factory = getFactoryByType(factoryType);

        // 4. Выполнить операцию
        TabulatedFunction resultFunction;
        try {
            switch (requestDto.getOperation().toLowerCase()) {
                case "add":
                    resultFunction = doAddition(operand1, operand2, factory);
                    break;
                case "subtract":
                    resultFunction = doSubtraction(operand1, operand2, factory);
                    break;
                case "multiply":
                    resultFunction = doMultiplication(operand1, operand2, factory);
                    break;
                case "divide":
                    resultFunction = doDivision(operand1, operand2, factory);
                    break;
                default:
                    log.error("Неизвестная бинарная операция: {}", requestDto.getOperation());
                    return ResponseEntity.badRequest().body(null);
            }
        } catch (IllegalArgumentException | ArithmeticException | InconsistentFunctionsException e) {
            log.error("Ошибка при выполнении бинарной операции '{}': {}", requestDto.getOperation(), e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }

        // 5. Конвертировать результат в DTO и вернуть
        List<TabulatedFunctionDto> resultDtos = convertTabulatedFunctionToDtoList(resultFunction);
        log.info("Бинарная операция '{}' выполнена успешно. Результат содержит {} точек.", requestDto.getOperation(), resultDtos.size());
        return ResponseEntity.ok(resultDtos);
    }

    // --- Метод для унарных операций (дифференцирование) ---
    @PostMapping("/differentiate")
    public ResponseEntity<List<TabulatedFunctionDto>> performDifferentiation(
            @RequestBody DifferentiateRequestDto requestDto) {
        log.info("Запрос на выполнение операции 'differentiate' с DTO: {}", requestDto);
        Long functionId = requestDto.getFunctionId();
        String factoryType = requestDto.getFactoryType();

        // Проверяем, что функция указана
        if (functionId == null) {
            log.error("Функция не указана для операции дифференцирования");
            return ResponseEntity.badRequest().body(null);
        }

        // 1. Проверить доступ к функции
        if (!hasAccessToFunction(functionId)) {
            log.warn("Пользователь не имеет доступа к функции: {}", functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        // 2. Загрузить функцию из БД
        TabulatedFunction function = loadTabulatedFunctionFromDB(functionId);
        if (function == null) {
            log.error("Функция с ID {} не найдена или не содержит точек.", functionId);
            return ResponseEntity.badRequest().body(null);
        }

        // 3. Выбрать фабрику для результата
        TabulatedFunctionFactory factory = getFactoryByType(factoryType);

        // 4. Выполнить дифференцирование
        TabulatedFunction resultFunction;
        try {
            // Создаем оператор дифференцирования
            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator(factory);
            // Выполняем дифференцирование
            resultFunction = differentialOperator.derive(function);
        } catch (Exception e) {
            log.error("Ошибка при выполнении дифференцирования: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }

        // 5. Конвертировать результат в DTO и вернуть
        List<TabulatedFunctionDto> resultDtos = convertTabulatedFunctionToDtoList(resultFunction);
        log.info("Дифференцирование выполнено успешно. Результат содержит {} точек.", resultDtos.size());
        return ResponseEntity.ok(resultDtos);
    }

    // --- Вспомогательные методы ---
    // Выбор фабрики по типу
    private TabulatedFunctionFactory getFactoryByType(String factoryType) {
        if ("linkedlist".equalsIgnoreCase(factoryType)) {
            log.debug("Используется LinkedListTabulatedFunctionFactory");
            return new LinkedListTabulatedFunctionFactory();
        } else {
            log.debug("Используется ArrayTabulatedFunctionFactory");
            return new ArrayTabulatedFunctionFactory();
        }
    }

    // Загрузка функции из БД с проверкой на null
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

    // Конвертация TabulatedFunction в список DTO
    private List<TabulatedFunctionDto> convertTabulatedFunctionToDtoList(TabulatedFunction function) {
        Point[] resultPoints = asPoints(function);
        for (Point p : resultPoints) {
            if (Double.isNaN(p.x) || Double.isNaN(p.y) ||
                    !Double.isFinite(p.x) || !Double.isFinite(p.y)) {
                throw new IllegalArgumentException(
                        "Результат операции содержит недопустимые значения (NaN/Infinity)");
            }
        }
        return Arrays.stream(resultPoints)
                .map(point -> new TabulatedFunctionDto(null, null, point.x, point.y))
                .collect(Collectors.toList());
    }

    // --- Статические методы операций ---
    private static Point[] asPoints(TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("TabulatedFunction не может быть null");
        }
        int count = function.getCount();
        Point[] points = new Point[count];
        for (int i = 0; i < count; i++) {
            points[i] = new Point(function.getX(i), function.getY(i));
        }
        return points;
    }

    private TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, java.util.function.BinaryOperator<Double> operation, TabulatedFunctionFactory factory) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("TabulatedFunction не может быть null");
        }
        int countA = a.getCount();
        int countB = b.getCount();
        if (countA != countB) {
            throw new InconsistentFunctionsException("Размеры не совпадают: " + countA + " и " + countB);
        }
        Point[] pointsA = asPoints(a);
        Point[] pointsB = asPoints(b);
        double[] xValues = new double[countA];
        double[] yValues = new double[countA];
        for (int i = 0; i < countA; i++) {
            double xA = pointsA[i].x;
            double xB = pointsB[i].x;
            if (Math.abs(xA - xB) > 1e-9) {
                throw new InconsistentFunctionsException("X-координаты не совпадают: " + xA + " и " + xB + " на индексе " + i);
            }
            xValues[i] = xA;
            try {
                yValues[i] = operation.apply(pointsA[i].y, pointsB[i].y);
            } catch (ArithmeticException e) {
                throw e;
            }
        }
        return factory.create(xValues, yValues);
    }

    private TabulatedFunction doAddition(TabulatedFunction a, TabulatedFunction b, TabulatedFunctionFactory factory) {
        return doOperation(a, b, Double::sum, factory);
    }

    private TabulatedFunction doSubtraction(TabulatedFunction a, TabulatedFunction b, TabulatedFunctionFactory factory) {
        return doOperation(a, b, (u, v) -> u - v, factory);
    }

    private TabulatedFunction doMultiplication(TabulatedFunction a, TabulatedFunction b, TabulatedFunctionFactory factory) {
        return doOperation(a, b, (u, v) -> u * v, factory);
    }

    private TabulatedFunction doDivision(TabulatedFunction a, TabulatedFunction b, TabulatedFunctionFactory factory) {
        return doOperation(a, b, (u, v) -> {
            if (Math.abs(v) < 1e-10) {
                throw new ArithmeticException("Деление на ноль или значение, близкое к нулю");
            }
            return u / v;
        }, factory);
    }

    // --- CRUD операции для сущности OperationEntity ---
    @PostMapping
    public ResponseEntity<OperationDto> createOperation(@RequestBody OperationDto operationDto) {
        log.info("Запрос на создание операции для функции ID: {}", operationDto.getFunctionId());

        // Проверяем доступ к функции
        if (!hasAccessToFunction(operationDto.getFunctionId())) {
            log.warn("Пользователь не имеет доступа к функции с ID {}", operationDto.getFunctionId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // Создаем сущность операции
            OperationEntity operationEntity = new OperationEntity();
            operationEntity.setOperationsTypeId(operationDto.getOperationsTypeId());

            // Находим функцию
            Optional<FunctionEntity> functionOpt = functionRepository.findById(operationDto.getFunctionId());
            if (functionOpt.isEmpty()) {
                log.error("Функция с ID {} не найдена", operationDto.getFunctionId());
                return ResponseEntity.notFound().build();
            }

            operationEntity.setFunction(functionOpt.get());

            // Сохраняем операцию
            OperationEntity savedEntity = operationRepository.save(operationEntity);
            OperationDto savedDto = convertToDto(savedEntity);

            log.info("Операция создана с ID: {} для функции ID: {}", savedDto.getId(), savedDto.getFunctionId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
        } catch (Exception e) {
            log.error("Ошибка при создании операции: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperationDto> getOperationById(@PathVariable Long id, @RequestParam Long functionId) {
        log.info("Запрос на получение операции с ID: {} для функции ID: {}", id, functionId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Попытка доступа без аутентификации к операции ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<OperationEntity> opOpt = operationRepository.findById(id);
        if (opOpt.isPresent()) {
            OperationEntity op = opOpt.get();
            if (!op.getFunction().getId().equals(functionId)) {
                log.warn("Операция с ID {} не принадлежит функции ID {}", id, functionId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            if (!hasAccessToFunction(functionId)) {
                log.warn("Пользователь '{}' не имеет доступа к операции {}, принадлежащей функции {}", auth.getName(), id, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            OperationDto operationDto = convertToDto(op);
            log.info("Операция с ID {} найдена", id);
            return ResponseEntity.ok(operationDto);
        } else {
            log.warn("Операция с ID {} не найдена", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperationDto> updateOperation(@PathVariable Long id, @RequestBody OperationDto operationDtoDetails) {
        log.info("Запрос на обновление операции с ID: {}", id);

        Optional<OperationEntity> operationOpt = operationRepository.findById(id);
        if (operationOpt.isEmpty()) {
            log.warn("Попытка обновить несуществующую операцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        OperationEntity operation = operationOpt.get();
        Long functionId = operation.getFunction().getId();

        // Проверяем доступ к функции
        if (!hasAccessToFunction(functionId)) {
            log.warn("Пользователь не имеет доступа к функции с ID {}", functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        operation.setOperationsTypeId(operationDtoDetails.getOperationsTypeId());

        // Если нужно изменить функцию, проверяем доступ к новой функции
        if (!functionId.equals(operationDtoDetails.getFunctionId())) {
            if (!hasAccessToFunction(operationDtoDetails.getFunctionId())) {
                log.warn("Пользователь не имеет доступа к новой функции с ID {}", operationDtoDetails.getFunctionId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Optional<FunctionEntity> newFunctionOpt = functionRepository.findById(operationDtoDetails.getFunctionId());
            if (newFunctionOpt.isEmpty()) {
                log.error("Новая функция с ID {} не найдена", operationDtoDetails.getFunctionId());
                return ResponseEntity.notFound().build();
            }
            operation.setFunction(newFunctionOpt.get());
        }

        OperationEntity updatedEntity = operationRepository.save(operation);
        OperationDto updatedDto = convertToDto(updatedEntity);

        log.info("Операция с ID {} обновлена", id);
        return ResponseEntity.ok(updatedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id, @RequestParam Long functionId) {
        log.info("Запрос на удаление операции с ID: {} для функции ID: {}", id, functionId);

        Optional<OperationEntity> operationOpt = operationRepository.findById(id);
        if (operationOpt.isEmpty()) {
            log.warn("Попытка удалить несуществующую операцию с ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        OperationEntity operation = operationOpt.get();
        if (!operation.getFunction().getId().equals(functionId)) {
            log.warn("Операция с ID {} не принадлежит функции ID {}", id, functionId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Проверяем доступ к функции
        if (!hasAccessToFunction(functionId)) {
            log.warn("Пользователь не имеет доступа к функции с ID {}", functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        operationRepository.delete(operation);
        log.info("Операция с ID {} удалена", id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/function")
    public ResponseEntity<Void> deleteAllOperationsByFunctionId(@RequestParam Long functionId) {
        log.info("Запрос на удаление всех операций для функции ID: {}", functionId);

        // Проверяем доступ к функции
        if (!hasAccessToFunction(functionId)) {
            log.warn("Пользователь не имеет доступа к функции с ID {}", functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<OperationEntity> operations = operationRepository.findAll().stream()
                .filter(op -> op.getFunction().getId().equals(functionId))
                .collect(Collectors.toList());

        operationRepository.deleteAll(operations);
        log.info("Удалено {} операций для функции ID: {}", operations.size(), functionId);
        return ResponseEntity.noContent().build();
    }

    // --- Вспомогательные методы ---
    private OperationDto convertToDto(OperationEntity opEntity) {
        Long functionId = opEntity.getFunction() != null ? opEntity.getFunction().getId() : null;
        return new OperationDto(
                opEntity.getId(),
                functionId,
                opEntity.getOperationsTypeId()
        );
    }
}