package core.controller;

import core.dto.*;
import core.entity.*;
import core.repository.*;
import exception.InconsistentFunctionsException;
import functions.*;
import functions.factory.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import operations.TabulatedDifferentialOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
public class OperationController {

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

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

    // POST /api/operations/execute - выполнение бинарной операции
    @PostMapping("/operations/execute")
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
        if (!checkFunctionAccess(functionIdA) || !checkFunctionAccess(functionIdB)) {
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

    // POST /api/differentiation - дифференцирование функции
    @PostMapping("/differentiation")
    public ResponseEntity<List<TabulatedFunctionDto>> performDifferentiation(
            @RequestBody DifferentiationRequestDto requestDto) {

        log.info("Запрос на выполнение операции 'differentiate' с DTO: {}", requestDto);

        Long functionId = requestDto.getFunctionId();

        // Проверяем, что функция указана
        if (functionId == null) {
            log.error("Функция не указана для операции дифференцирования");
            return ResponseEntity.badRequest().body(null);
        }

        // 1. Проверить доступ к функции
        if (!checkFunctionAccess(functionId)) {
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
        TabulatedFunctionFactory factory = getFactoryByType(requestDto.getFactoryType());

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

    // POST /api/operations/differentiate - альтернативный эндпоинт для дифференцирования (для совместимости)
    @PostMapping("/operations/differentiate")
    public ResponseEntity<List<TabulatedFunctionDto>> performDifferentiationOperations(
            @RequestBody DifferentiationRequestDto requestDto) {
        return performDifferentiation(requestDto);
    }

    // --- Вспомогательные методы ---

    // Выбор фабрики по типу
    private TabulatedFunctionFactory getFactoryByType(String factoryType) {
        if (factoryType == null || factoryType.isEmpty()) {
            factoryType = "array";
        }

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

    // Статические методы операций
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

    private TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b,
                                          java.util.function.BinaryOperator<Double> operation,
                                          TabulatedFunctionFactory factory) {
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
}