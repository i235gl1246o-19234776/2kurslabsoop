package model.service;

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
import java.util.List;
import java.util.Optional;
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


}