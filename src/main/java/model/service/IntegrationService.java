package model.service;

import functions.*;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import model.dto.request.IntegrationRequestDTO;
import model.dto.response.IntegrationResultDTO;
import model.entity.Function;
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
    private final TabulatedFunctionRepository tabulatedFunctionRepository;
    private static final int MAX_THREADS = 16;
    private static final long MAX_STEPS = 1_000_000;
    private static final long MIN_STEPS = 10;

    public IntegrationService() {
        this.functionRepository = new FunctionRepository();
        this.tabulatedFunctionRepository = new TabulatedFunctionRepository();
    }

    public IntegrationResultDTO calculateIntegral(IntegrationRequestDTO request, Long userId) throws SQLException {
        validateRequest(request);

        // Получаем функцию с проверкой принадлежности пользователю
        Function function = functionRepository.findById(request.getFunctionId(), userId)
                .orElseThrow(() -> new SecurityException("Отказано в доступе к функции"));

        // Получаем точки функции
        List<TabulatedFunction> points = tabulatedFunctionRepository.findAllByFunctionId(request.getFunctionId());
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Функция не содержит точек для интегрирования");
        }

        // Сортируем точки по X и создаем массивы
        List<TabulatedFunction> sortedPoints = points.stream()
                .sorted(Comparator.comparingDouble(TabulatedFunction::getXval))
                .collect(Collectors.toList());

        double[] xValues = sortedPoints.stream().mapToDouble(TabulatedFunction::getXval).toArray();
        double[] yValues = sortedPoints.stream().mapToDouble(TabulatedFunction::getYval).toArray();

        // Проверяем границы интегрирования
        double minX = xValues[0];
        double maxX = xValues[xValues.length - 1];

        if (request.getA() < minX || request.getB() > maxX) {
            throw new IllegalArgumentException(String.format(
                    "Интервал интегрирования [%.6f, %.6f] выходит за пределы области определения функции [%.6f, %.6f]",
                    request.getA(), request.getB(), minX, maxX
            ));
        }

        // Создаем табулированную функцию
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        AbstractTabulatedFunction tabulatedFunction = (AbstractTabulatedFunction) factory.create(xValues, yValues);

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

    private void validateRequest(IntegrationRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Запрос не может быть пустым");
        }

        if (request.getFunctionId() == null || request.getFunctionId() <= 0) {
            throw new IllegalArgumentException("Некорректный ID функции");
        }

        if (request.getA() >= request.getB()) {
            throw new IllegalArgumentException("Левая граница интервала должна быть меньше правой");
        }

        if (request.getSteps() < MIN_STEPS) {
            throw new IllegalArgumentException("Количество шагов должно быть не менее " + MIN_STEPS);
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
        return Math.min(MAX_THREADS, Math.max(1, processors));
    }
}