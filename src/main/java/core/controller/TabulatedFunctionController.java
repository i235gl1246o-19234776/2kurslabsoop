package core.controller;

import core.dto.*;
import core.entity.*;
import core.repository.*;
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
import functions.MathFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;

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
    }

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

    // Проверка доступа к точке
    private boolean checkPointAccess(Long pointId) {
        Optional<TabulatedFunctionEntity> pointOpt = tabulatedFunctionRepository.findById(pointId);
        if (pointOpt.isEmpty()) {
            return false;
        }

        return checkFunctionAccess(pointOpt.get().getFunction().getId());
    }

    // GET /api/tabulated-points/function/{functionId} - получение всех точек функции
    @GetMapping("/function/{functionId}")
    public ResponseEntity<List<TabulatedFunctionDto>> getAllPointsByFunctionId(@PathVariable Long functionId) {
        log.info("Запрос на получение всех точек для функции с ID: {}", functionId);

        if (!checkFunctionAccess(functionId)) {
            log.warn("Пользователь пытается получить точки функции {}, к которой не имеет доступа", functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        List<TabulatedFunctionDto> pointDtos = points.stream()
                .map(point -> new TabulatedFunctionDto(point.getId(), point.getFunction().getId(), point.getXVal(), point.getYVal()))
                .collect(Collectors.toList());

        log.info("Получено {} точек для функции с ID: {}", pointDtos.size(), functionId);
        return ResponseEntity.ok(pointDtos);
    }

    // GET /api/tabulated-points/function/{functionId}/x/{xValue} - получение точки по X
    @GetMapping("/function/{functionId}/x/{xValue}")
    public ResponseEntity<TabulatedFunctionDto> getPointByFunctionIdAndX(
            @PathVariable Long functionId,
            @PathVariable Double xValue) {

        log.info("Запрос на поиск точки для функции ID: {} по X: {}", functionId, xValue);

        if (!checkFunctionAccess(functionId)) {
            log.warn("Пользователь пытается получить точку функции {}, к которой не имеет доступа", functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        Optional<TabulatedFunctionEntity> pointOpt = points.stream()
                .filter(p -> Math.abs(p.getXVal() - xValue) < 1e-9)
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

    // GET /api/tabulated-points/function/{functionId}/range - получение точек в диапазоне X
    @GetMapping("/function/{functionId}/range")
    public ResponseEntity<List<TabulatedFunctionDto>> getPointsInRange(
            @PathVariable Long functionId,
            @RequestParam Double fromX,
            @RequestParam Double toX) {

        log.info("Запрос на получение точек для функции ID: {} в диапазоне от {} до {}", functionId, fromX, toX);

        if (!checkFunctionAccess(functionId)) {
            log.warn("Пользователь пытается получить точки в диапазоне функции {}, к которой не имеет доступа", functionId);
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

    // POST /api/tabulated-points - создание новой точки
    @PostMapping
    public ResponseEntity<TabulatedFunctionDto> createPoint(@RequestBody TabulatedFunctionDto pointDto) {
        log.info("Запрос на создание точки для функции ID: {}", pointDto.getFunctionId());

        if (!checkFunctionAccess(pointDto.getFunctionId())) {
            log.warn("Пользователь пытается создать точку для функции {}, к которой не имеет доступа", pointDto.getFunctionId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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

        TabulatedFunctionEntity pointEntity = new TabulatedFunctionEntity();
        pointEntity.setFunction(functionOpt.get());
        pointEntity.setXVal(pointDto.getXVal());
        pointEntity.setYVal(pointDto.getYVal());

        TabulatedFunctionEntity savedEntity = tabulatedFunctionRepository.save(pointEntity);
        TabulatedFunctionDto savedDto = convertToDto(savedEntity);

        log.info("Точка создана с ID: {} для функции ID: {}", savedDto.getId(), savedDto.getFunctionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    // PUT /api/tabulated-points/{id} - обновление точки
    @PutMapping("/{id}")
    public ResponseEntity<TabulatedFunctionDto> updatePoint(
            @PathVariable Long id,
            @RequestBody TabulatedFunctionDto pointDtoDetails) {

        log.info("Запрос на обновление точки с ID: {}", id);

        if (!checkPointAccess(id)) {
            log.warn("Пользователь не имеет прав на обновление точки {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<TabulatedFunctionEntity> pointOpt = tabulatedFunctionRepository.findById(id);
        if (pointOpt.isPresent()) {
            TabulatedFunctionEntity point = pointOpt.get();
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

    // DELETE /api/tabulated-points/{id} - удаление точки
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoint(@PathVariable Long id) {
        log.info("Запрос на удаление точки с ID: {}", id);

        if (!checkPointAccess(id)) {
            log.warn("Пользователь не имеет прав на удаление точки {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<TabulatedFunctionEntity> pointOpt = tabulatedFunctionRepository.findById(id);
        if (pointOpt.isPresent()) {
            tabulatedFunctionRepository.delete(pointOpt.get());
            log.info("Точка с ID {} удалена", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Попытка удалить несуществующую точку с ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/tabulated-points/function/{functionId} - удаление всех точек функции
    @DeleteMapping("/function/{functionId}")
    public ResponseEntity<Void> deleteAllPointsByFunctionId(@PathVariable Long functionId) {
        log.info("Запрос на удаление всех точек для функции ID: {}", functionId);

        if (!checkFunctionAccess(functionId)) {
            log.warn("Пользователь пытается удалить точки функции {}, к которой не имеет доступа", functionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        tabulatedFunctionRepository.deleteAll(points);

        log.info("Удалено {} точек для функции ID: {}", points.size(), functionId);
        return ResponseEntity.noContent().build();
    }

    // POST /api/tabulated-points/calculate - вычисление и сохранение точек
    @PostMapping("/calculate")
    public ResponseEntity<String> calculateAndSaveTabulatedPoints(@RequestBody CalculatePointsRequestDto requestDto) {
        log.info("Запрос на вычисление точек для функции ID: {}, функции: {}, интервал: [{}, {}], количество точек: {}",
                requestDto.getFunctionId(), requestDto.getMathFunctionName(),
                requestDto.getStart(), requestDto.getEnd(), requestDto.getCount());

        // Проверяем доступ к функции
        if (!checkFunctionAccess(requestDto.getFunctionId())) {
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

    private TabulatedFunctionDto convertToDto(TabulatedFunctionEntity pointEntity) {
        Long functionId = pointEntity.getFunction() != null ? pointEntity.getFunction().getId() : null;
        return new TabulatedFunctionDto(
                pointEntity.getId(),
                functionId,
                pointEntity.getXVal(),
                pointEntity.getYVal()
        );
    }

    // DTO для запроса вычисления точек
    public static class CalculatePointsRequestDto {
        private Long functionId;
        private String mathFunctionName;
        private double start;
        private double end;
        private int count;
        private String factoryType;

        // Геттеры и сеттеры
        public Long getFunctionId() { return functionId; }
        public void setFunctionId(Long functionId) { this.functionId = functionId; }
        public String getMathFunctionName() { return mathFunctionName; }
        public void setMathFunctionName(String mathFunctionName) { this.mathFunctionName = mathFunctionName; }
        public double getStart() { return start; }
        public void setStart(double start) { this.start = start; }
        public double getEnd() { return end; }
        public void setEnd(double end) { this.end = end; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public String getFactoryType() { return factoryType; }
        public void setFactoryType(String factoryType) { this.factoryType = factoryType; }
    }
}