package core.controller;

import core.dto.IntegrationRequestDto;
import core.dto.IntegrationResultDto;
import core.entity.FunctionEntity;
import core.entity.TabulatedFunctionEntity;
import core.entity.UserEntity;
import core.repository.FunctionRepository;
import core.repository.TabulatedFunctionRepository;
import core.repository.UserRepository;
import functions.MathFunction;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
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
@RequestMapping("/api/integration")
public class IntegrationController {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

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

    // POST /api/integration - вычисление интеграла
    @PostMapping
    public ResponseEntity<IntegrationResultDto> performIntegration(
            @RequestBody IntegrationRequestDto requestDto) {

        log.info("Запрос на вычисление интеграла: {}", requestDto);

        // Проверяем доступ к функции
        if (!checkFunctionAccess(requestDto.getFunctionId())) {
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
            double result = operations.ParallelIntegrator.integrateWithFixedPool(
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

    // Загрузка функции из БД
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
}