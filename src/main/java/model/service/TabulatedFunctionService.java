package model.service;

import functions.MathFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import model.entity.TabulatedFunction;
import model.dto.request.TabulatedFunctionRequestDTO;
import model.dto.response.TabulatedFunctionResponseDTO;
import model.dto.DTOTransformService;
import repository.DatabaseConnection;
import repository.dao.TabulatedFunctionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class TabulatedFunctionService {
    private static final Logger logger = Logger.getLogger(TabulatedFunctionService.class.getName());
    private final TabulatedFunctionRepository tabulatedFunctionRepository;
    private final DTOTransformService dtoTransformService;

    public TabulatedFunctionService() {
        this.tabulatedFunctionRepository = new TabulatedFunctionRepository();
        this.dtoTransformService = new DTOTransformService();
    }

    public TabulatedFunctionService(TabulatedFunctionRepository tabulatedFunctionRepository, DTOTransformService dtoTransformService) {
        this.tabulatedFunctionRepository = tabulatedFunctionRepository;
        this.dtoTransformService = dtoTransformService;
    }

    public TabulatedFunctionResponseDTO createTabulatedFunction(TabulatedFunctionRequestDTO tabulatedFunctionRequest) throws SQLException {
        logger.info("Создание новой точки табулированной функции для функции: " + tabulatedFunctionRequest.getFunctionId());

        TabulatedFunction tabulatedFunction = dtoTransformService.toEntity(tabulatedFunctionRequest);
        Long pointId = tabulatedFunctionRepository.createTabulatedFunction(tabulatedFunction);

        Optional<TabulatedFunction> createdPoint = tabulatedFunctionRepository.findById(pointId);
        if (createdPoint.isPresent()) {
            return dtoTransformService.toResponseDTO(createdPoint.get());
        } else {
            throw new SQLException("Не удалось получить созданную точку с ID: " + pointId);
        }
    }

    public List<TabulatedFunctionResponseDTO> getTabulatedFunctionsByFunctionId(Long functionId) throws SQLException {
        logger.info("Получение всех точек табулированной функции для функции: " + functionId);
        List<TabulatedFunction> points = tabulatedFunctionRepository.findAllByFunctionId(functionId);
        return dtoTransformService.toResponseDTOs(points);
    }

    public Optional<TabulatedFunctionResponseDTO> getTabulatedFunctionByXValue(Long functionId, Double xVal) throws SQLException {
        logger.info("Поиск точки по значению X: " + xVal + " для функции: " + functionId);
        Optional<TabulatedFunction> point = tabulatedFunctionRepository.findByXValue(functionId, xVal);
        return point.map(dtoTransformService::toResponseDTO);
    }

    public List<TabulatedFunctionResponseDTO> getTabulatedFunctionsBetweenXValues(Long functionId, Double xMin, Double xMax) throws SQLException {
        logger.info("Поиск точек в диапазоне X: " + xMin + " - " + xMax + " для функции: " + functionId);
        List<TabulatedFunction> points = tabulatedFunctionRepository.findBetweenXValues(functionId, xMin, xMax);
        return dtoTransformService.toResponseDTOs(points);
    }

    public boolean updateTabulatedFunction(TabulatedFunction tabulatedFunction) throws SQLException {
        logger.info("Обновление точки табулированной функции с ID: " + tabulatedFunction.getId());
        return tabulatedFunctionRepository.updateTabulatedFunction(tabulatedFunction);
    }

    public boolean deleteTabulatedFunction(Long id) throws SQLException {
        logger.info("Удаление точки табулированной функции с ID: " + id);
        return tabulatedFunctionRepository.deleteTabulatedFunction(id);
    }

    public boolean deleteAllTabulatedFunctions(Long functionId) throws SQLException {
        logger.info("Удаление всех точек табулированной функции для функции: " + functionId);
        return tabulatedFunctionRepository.deleteAllTabulatedFunctions(functionId);
    }

    public Optional<TabulatedFunctionResponseDTO> getTabulatedFunctionById(Long pointId) throws SQLException {
        logger.info("Получение точки табулированной функции по ID: " + pointId);
        Optional<TabulatedFunction> point = tabulatedFunctionRepository.findById(pointId);
        return point.map(dtoTransformService::toResponseDTO);
    }

    public Optional<TabulatedFunction> getTabulatedFunctionEntityById(Long pointId) throws SQLException {
        return tabulatedFunctionRepository.findById(pointId);
    }

    public List<TabulatedFunction> getTabulatedFunctionEntitiesByFunctionId(Long functionId) throws SQLException {
        return tabulatedFunctionRepository.findAllByFunctionId(functionId);
    }

    public boolean isFunctionOwnedByUser(Long functionId, Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM functions WHERE id = ? AND user_id = ?";

        DatabaseConnection conn = new DatabaseConnection();
        try (Connection connection = conn.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            stmt.setLong(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }
    public Optional<Long> getFunctionIdByPointId(Long pointId) throws SQLException {
        String sql = "SELECT function_id FROM tabulated_functions WHERE id = ?";
        DatabaseConnection conn = new DatabaseConnection();
        try (Connection connection = conn.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, pointId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("function_id"));
                }
            }
        }

        return Optional.empty();
    }
    public void calculateAndSaveTabulatedPoints(Long functionId, MathFunction mathFunction, double start, double end, int count, TabulatedFunctionFactory factory) throws SQLException {
        logger.info("Вычисление точек для functionId: " + functionId + ", функции: " + mathFunction.getClass().getSimpleName() + ", интервал: [" + start + ", " + end + "], count: " + count);

        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        if (start >= end) {
            throw new IllegalArgumentException("Start must be less than End");
        }

        // 1. Вычисляем точки
        double[] xValues = new double[count];
        double[] yValues = new double[count];

        double step = (end - start) / (count); // Шаг разбиения

        for (int i = 0; i < count; i++) {
            xValues[i] = start + i * step;
            yValues[i] = mathFunction.apply(xValues[i]);
        }
        functions.TabulatedFunction tabulatedFunc = factory.create(xValues, yValues);

        // 3. Сохраняем точки в БД (используя Entity model.entity.TabulatedFunction)
        // tabulatedFunc.getCount(), tabulatedFunc.getX(i), tabulatedFunc.getY(i) - методы из functions.TabulatedFunction
        for (int i = 0; i < tabulatedFunc.getCount(); i++) {
            TabulatedFunction entity = new TabulatedFunction(); // Это model.entity.TabulatedFunction
            entity.setFunctionId(functionId);
            entity.setXVal(tabulatedFunc.getX(i)); // getX(i) из functions.TabulatedFunction
            entity.setYVal(tabulatedFunc.getY(i)); // getY(i) из functions.TabulatedFunction

            // Сохраняем точку через репозиторий
            // Убедитесь, что save в TabulatedFunctionRepository принимает model.entity.TabulatedFunction
            tabulatedFunctionRepository.createTabulatedFunction(entity);
        }

        logger.info("Вычислено и сохранено " + count + " точек для functionId: " + functionId);
    }
    public functions.TabulatedFunction loadTabulatedFunction(Long functionId) throws SQLException {
        List<model.entity.TabulatedFunction> dbPoints = tabulatedFunctionRepository.findAllByFunctionId(functionId);

        if (dbPoints.isEmpty()) {
            return null;
        }

        // Сортируем по xVal (и по id, если есть, для стабильности)
        dbPoints.sort(Comparator.comparingDouble(model.entity.TabulatedFunction::getXVal)
                .thenComparingLong(model.entity.TabulatedFunction::getId)); // если есть getId()

        // Фильтруем дубликаты по xVal — оставляем ПЕРВУЮ точку для каждого x
        Map<Double, TabulatedFunction> uniquePoints = new LinkedHashMap<>();
        for (model.entity.TabulatedFunction point : dbPoints) {
            uniquePoints.putIfAbsent(point.getXVal(), point);
        }

        List<model.entity.TabulatedFunction> filteredPoints = new ArrayList<>(uniquePoints.values());

        double[] x = filteredPoints.stream().mapToDouble(model.entity.TabulatedFunction::getXVal).toArray();
        double[] y = filteredPoints.stream().mapToDouble(model.entity.TabulatedFunction::getYVal).toArray();

        return new ArrayTabulatedFunctionFactory().create(x, y);
    }
}
