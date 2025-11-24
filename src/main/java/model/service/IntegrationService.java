package model.service;

import functions.*;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import model.dto.request.IntegrationRequestDTO;
import model.dto.response.IntegrationResultDTO;
import model.entity.Function;
import model.entity.Point;
import model.entity.TabulatedFunction;
import repository.dao.FunctionRepository;
import repository.dao.TabulatedFunctionRepository;
import operations.ParallelIntegrator;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IntegrationService {
    private final FunctionRepository functionRepository;
    private final TabulatedFunctionRepository pointRepository;
    private static final int MAX_THREADS = 16;
    private static final long MAX_STEPS = 1_000_000;
    private static final long MIN_STEPS = 10;

    public IntegrationService() {
        this.functionRepository = new FunctionRepository();
        this.pointRepository = new TabulatedFunctionRepository();
    }

    public IntegrationService(FunctionRepository functionRepository, TabulatedFunctionRepository pointRepository) {
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    public IntegrationResultDTO calculateIntegral(IntegrationRequestDTO request, Long userId) throws SQLException {
        validateRequest(request);

        // Получаем функцию с проверкой принадлежности пользователю
        Function function = functionRepository.findById(request.getFunctionId(), userId)
                .orElseThrow(() -> new SecurityException("Отказано в доступе к функции"));

        if (!"tabular".equals(function.getTypeFunction())) {
            throw new IllegalArgumentException("Интегрирование возможно только для табулированных функций");
        }

        // Получаем табулированную функцию (объект, содержащий точки)
        Optional<TabulatedFunction> tabulatedFunctionOpt = pointRepository.findById(request.getFunctionId());
        if (tabulatedFunctionOpt.isEmpty()) {
            throw new IllegalArgumentException("Функция не содержит точек для интегрирования");
        }

        TabulatedFunction tabulatedFunctionEntity = tabulatedFunctionOpt.get();

        // Получаем точки из сущности
        List<Point> points = tabulatedFunctionEntity.getPoints(); // предполагается, что есть такой метод
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Функция не содержит точек для интегрирования");
        }

        // Находим минимум и максимум по X
        double minX = points.stream().mapToDouble(Point::getXval).min().orElse(Double.MAX_VALUE);
        double maxX = points.stream().mapToDouble(Point::getXval).max().orElse(Double.MIN_VALUE);

        if (request.getA() < minX || request.getB() > maxX) {
            throw new IllegalArgumentException(String.format(
                    "Интервал интегрирования [%.6f, %.6f] выходит за пределы области определения функции [%.6f, %.6f]",
                    request.getA(), request.getB(), minX, maxX
            ));
        }

        AbstractTabulatedFunction tabulatedFunction = createTabulatedFunction(function, points);

        int threadCount = Math.min(MAX_THREADS, Math.max(1, request.getThreadCount()));
        long steps = Math.min(MAX_STEPS, Math.max(MIN_STEPS, request.getSteps()));

        long startTime = System.nanoTime();
        ParallelIntegrator.IntegrationResult result = ParallelIntegrator.integrateWithFixedPool(
                tabulatedFunction,
                request.getA(),
                request.getB(),
                steps,
                threadCount
        );
        long endTime = System.nanoTime();

        return new IntegrationResultDTO(
                result.result(),
                (endTime - startTime) / 1_000_000.0,
                threadCount,
                steps,
                request.getA(),
                request.getB(),
                function.getFunctionName()
        );
    }

    private AbstractTabulatedFunction createTabulatedFunction(Function function, List<Point> points) {
        // Сортируем точки по возрастанию X
        List<Point> sortedPoints = points.stream()
                .sorted(Comparator.comparingDouble(Point::getXval))
                .collect(Collectors.toList());

        // Извлекаем массивы X и Y
        double[] xValues = sortedPoints.stream().mapToDouble(Point::getXval).toArray();
        double[] yValues = sortedPoints.stream().mapToDouble(Point::getYval).toArray();

        // Выбираем фабрику
        //String factoryType = function.getFactoryType();
        String factoryType = "";
        TabulatedFunctionFactory factory;

        if ("linked-list".equals(factoryType)) {
            factory = new LinkedListTabulatedFunctionFactory();
        } else {
            factory = new ArrayTabulatedFunctionFactory(); // по умолчанию
        }

        return (AbstractTabulatedFunction) factory.create(xValues, yValues);
    }

    private void validateRequest(IntegrationRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Запрос не может быть пустым");
        }

        if (request.getFunctionId() == null || request.getFunctionId() <= 0) {
            throw new IllegalArgumentException("Некорректный ID функции");
        }

        if (request.getA() > request.getB()) {
            throw new IllegalArgumentException("Левая граница интервала не может быть больше правой");
        }

        if (request.getA() == request.getB()) {
            throw new IllegalArgumentException("Границы интервала не могут совпадать");
        }

        if (request.getSteps() < MIN_STEPS) {
            throw new IllegalArgumentException("Количество шагов должно быть больше " + MIN_STEPS);
        }

        if (request.getThreadCount() < 1) {
            throw new IllegalArgumentException("Количество потоков должно быть положительным");
        }

        if (request.getThreadCount() > MAX_THREADS) {
            throw new IllegalArgumentException("Количество потоков не может превышать " + MAX_THREADS);
        }
    }

    public static int getMaxThreads() {
        return MAX_THREADS;
    }

    public static int getOptimalThreadCount() {
        int processors = Runtime.getRuntime().availableProcessors();
        return Math.min(MAX_THREADS, processors * 2);
    }
}