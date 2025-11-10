package service;

import model.dto.request.SearchFunctionRequestDTO;
import model.entity.Function;
import model.dto.request.FunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import model.dto.DTOTransformService;
import model.SearchFunctionResult;
import repository.DatabaseConnection;
import repository.FunctionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class FunctionService {
    private static final Logger logger = Logger.getLogger(FunctionService.class.getName());
    private final FunctionRepository functionRepository;
    private final DTOTransformService dtoTransformService;

    public FunctionService() {
        DatabaseConnection dbc = new DatabaseConnection();
        this.functionRepository = new FunctionRepository(dbc);
        this.dtoTransformService = new DTOTransformService();
    }

    public FunctionService(FunctionRepository functionRepository, DTOTransformService dtoTransformService) {
        this.functionRepository = functionRepository;
        this.dtoTransformService = dtoTransformService;
    }

    public FunctionResponseDTO createFunction(FunctionRequestDTO functionRequest) throws SQLException {
        logger.info("Создание новой функции: " + functionRequest.getFunctionName());

        Function function = dtoTransformService.toEntity(functionRequest);
        Long functionId = functionRepository.createFunction(function);

        Optional<Function> createdFunction = functionRepository.findById(functionId, functionRequest.getUserId());
        if (createdFunction.isPresent()) {
            return dtoTransformService.toResponseDTO(createdFunction.get());
        } else {
            throw new SQLException("Не удалось получить созданную функцию с ID: " + functionId);
        }
    }

    public Optional<FunctionResponseDTO> getFunctionById(Long id, Long userId) throws SQLException {
        logger.info("Получение функции по ID: " + id + " для пользователя: " + userId);
        Optional<Function> function = functionRepository.findById(id, userId);
        return function.map(dtoTransformService::toResponseDTO);
    }

    public List<FunctionResponseDTO> getFunctionsByUserId(Long userId) throws SQLException {
        logger.info("Получение всех функций пользователя с ID: " + userId);
        List<Function> functions = functionRepository.findByUserId(userId);
        return dtoTransformService.toFunctionResponseDTOs(functions);
    }

    public List<FunctionResponseDTO> getFunctionsByName(Long userId, String namePattern) throws SQLException {
        logger.info("Поиск функций по шаблону имени: " + namePattern + " для пользователя: " + userId);
        List<Function> functions = functionRepository.findByName(userId, namePattern);
        return dtoTransformService.toFunctionResponseDTOs(functions);
    }

    public List<FunctionResponseDTO> getFunctionsByType(Long userId, String type) throws SQLException {
        logger.info("Получение функций типа: " + type + " для пользователя: " + userId);
        List<Function> functions = functionRepository.findByType(userId, type);
        return dtoTransformService.toFunctionResponseDTOs(functions);
    }

    public boolean updateFunction(Function function) throws SQLException {
        logger.info("Обновление функции с ID: " + function.getId());
        return functionRepository.updateFunction(function);
    }

    public boolean deleteFunction(Long id, Long userId) throws SQLException {
        logger.info("Удаление функции с ID: " + id + " для пользователя: " + userId);
        return functionRepository.deleteFunction(id, userId);
    }

    public List<SearchFunctionResult> searchFunctions(
            Long userId,
            String userName,
            String functionNamePattern,
            String typeFunction,
            Double xVal,
            Double yVal,
            Long operationsTypeId,
            String sortBy,
            String sortOrder) throws SQLException {

        logger.info("Поиск функций с параметрами: user=" + userName + ", function=" + functionNamePattern + ", type=" + typeFunction);

        return functionRepository.search(
                userId, userName, functionNamePattern, typeFunction,
                xVal, yVal, operationsTypeId, sortBy, sortOrder
        );
    }


    public Optional<Function> getFunctionEntityById(Long id, Long userId) throws SQLException {
        return functionRepository.findById(id, userId);
    }

    public void printPerformanceStats() throws SQLException {
        functionRepository.printPerformanceStats();
    }

    public void createPerformanceTable() throws SQLException {
        functionRepository.createPerformanceTable();
    }

    private Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("getConnection() должен быть реализован");
    }

    public int searchFunctions(SearchFunctionRequestDTO request) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM functions WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (request.getUserName() != null && !request.getUserName().isEmpty()) {
            sql.append(" AND user_name = ?");
            parameters.add(request.getUserName());
        }
        if (request.getFunctionName() != null && !request.getFunctionName().isEmpty()) {
            sql.append(" AND function_name = ?");
            parameters.add(request.getFunctionName());
        }
        if (request.getTypeFunction() != null && !request.getTypeFunction().isEmpty()) {
            sql.append(" AND type_function = ?");
            parameters.add(request.getTypeFunction());
        }
        if (request.getXVal() != null) {
            sql.append(" AND x_val = ?");
            parameters.add(request.getXVal());
        }
        if (request.getYVal() != null) {
            sql.append(" AND y_val = ?");
            parameters.add(request.getYVal());
        }
        if (request.getOperationsTypeId() != null) {
            sql.append(" AND operations_type_id = ?");
            parameters.add(request.getOperationsTypeId());
        }

        System.out.println("Executing Count SQL: " + sql); // Логирование для отладки
        System.out.println("Parameters: " + parameters); // Логирование параметров
        DatabaseConnection dbconn = new DatabaseConnection();
        dbconn.setConnectionParams("jdbc:postgresql://localhost:5432/test_10k_db", "postgres", "1234", true);
        try (Connection conn = dbconn.getConnection()){
             PreparedStatement stmt = conn.prepareStatement(sql.toString());

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }
        }
    }
}