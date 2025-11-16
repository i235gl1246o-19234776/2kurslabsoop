package model.service;

import model.dto.request.SearchFunctionRequestDTO;
import model.dto.response.SearchFunctionResponseDTO;
import model.entity.Function;
import model.dto.request.FunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import model.dto.DTOTransformService;
import model.entity.SearchFunctionResult;
import repository.DatabaseConnection;
import repository.dao.FunctionRepository;

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
    public SearchFunctionResponseDTO searchFunctions(SearchFunctionRequestDTO request) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT
                f.id AS function_id,
                f.function_name,
                f.type_function,
                f.function_expression,
                u.name AS username,              -- имя пользователя из users
                o.id AS operation_id,
                o.operations_type_id,            -- ID типа операции
                t.id AS tabulated_function_id,
                t.x_val,                         -- значение X из tabulated_functions
                t.y_val                          -- значение Y из tabulated_functions
            FROM functions f
            LEFT JOIN users u ON f.user_id = u.id
            LEFT JOIN operations o ON f.id = o.function_id
            LEFT JOIN tabulated_functions t ON f.id = t.function_id
            WHERE 1=1
    """);

        List<Object> parameters = new ArrayList<>();
        List<FunctionResponseDTO> functions = new ArrayList<>();

        // Добавляем условия фильтрации
        if (request.getUserName() != null && !request.getUserName().isEmpty()) {
            sql.append(" AND u.name = ?");  // Исправлено: u.name вместо f.user_name
            parameters.add(request.getUserName());
        }
        if (request.getFunctionName() != null && !request.getFunctionName().isEmpty()) {
            sql.append(" AND f.function_name LIKE ?");
            parameters.add(request.getFunctionName() + "%");
        }
        if (request.getTypeFunction() != null && !request.getTypeFunction().isEmpty()) {
            sql.append(" AND f.type_function = ?");
            parameters.add(request.getTypeFunction());
        }
        if (request.getXVal() != null) {
            sql.append(" AND t.x_val = ?");  // Исправлено: t.x_val вместо f.x_val
            parameters.add(request.getXVal());
        }
        if (request.getYVal() != null) {
            sql.append(" AND t.y_val = ?");  // Исправлено: t.y_val вместо f.y_val
            parameters.add(request.getYVal());
        }
        if (request.getOperationsTypeId() != null) {
            sql.append(" AND o.operations_type_id = ?");  // Исправлено: o.operations_type_id вместо f.operations_type_id
            parameters.add(request.getOperationsTypeId());
        }

        // Добавляем сортировку
        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            String sortField = getSortField(request.getSortBy());
            String sortOrder = "ASC".equalsIgnoreCase(request.getSortOrder()) ? "ASC" : "DESC";
            sql.append(" ORDER BY ").append(sortField).append(" ").append(sortOrder);
        }

        // Добавляем пагинацию
        if (request.getPage() != null && request.getSize() != null) {
            int offset = (request.getPage() - 1) * request.getSize();
            sql.append(" LIMIT ? OFFSET ?");
            parameters.add(request.getSize());
            parameters.add(offset);
        }

        System.out.println("Executing Search SQL: " + sql);
        System.out.println("Parameters: " + parameters);

        DatabaseConnection dbconn = new DatabaseConnection();

        try (Connection conn = dbconn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Устанавливаем параметры
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            // Выполняем запрос и маппим результаты
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FunctionResponseDTO function = new FunctionResponseDTO();
                    function.setFunctionId(rs.getLong("function_id"));
                    function.setFunctionName(rs.getString("function_name"));
                    function.setTypeFunction(rs.getString("type_function"));
                    function.setXVal(rs.getDouble("x_val"));
                    function.setYVal(rs.getDouble("y_val"));
                    function.setUserName(rs.getString("username"));  // Исправлено: username вместо user_name
                    function.setOperationsTypeId(rs.getLong("operations_type_id"));
                    // Убрано: function.setOperationTypeName(rs.getString("operation_type_name")); — нет такой колонки

                    functions.add(function);
                }
            }

            int total = getTotalCount(request);

            return new SearchFunctionResponseDTO(functions, total);

        }
    }

    private int getTotalCount(SearchFunctionRequestDTO request) throws SQLException {
        StringBuilder countSql = new StringBuilder("""
        SELECT COUNT(*) 
        FROM functions f
        LEFT JOIN users u ON f.user_id = u.id
        LEFT JOIN operations o ON f.id = o.function_id
        LEFT JOIN tabulated_functions t ON f.id = t.function_id
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();

        if (request.getUserName() != null && !request.getUserName().isEmpty()) {
            countSql.append(" AND u.name = ?");
            params.add(request.getUserName());
        }
        if (request.getFunctionName() != null && !request.getFunctionName().isEmpty()) {
            countSql.append(" AND f.function_name LIKE ?");
            params.add(request.getFunctionName() + "%");
        }
        if (request.getTypeFunction() != null && !request.getTypeFunction().isEmpty()) {
            countSql.append(" AND f.type_function = ?");
            params.add(request.getTypeFunction());
        }
        if (request.getXVal() != null) {
            countSql.append(" AND t.x_val = ?");
            params.add(request.getXVal());
        }
        if (request.getYVal() != null) {
            countSql.append(" AND t.y_val = ?");
            params.add(request.getYVal());
        }
        if (request.getOperationsTypeId() != null) {
            countSql.append(" AND o.operations_type_id = ?");
            params.add(request.getOperationsTypeId());
        }

        DatabaseConnection dbconn = new DatabaseConnection();

        try (Connection conn = dbconn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(countSql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        return 0;
    }


    private String getSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "function_id" -> "f.id"; // Исправлено: f.id вместо f.function_id
            case "function_name" -> "f.function_name";
            case "type_function" -> "f.type_function";
            case "function_expression" -> "f.function_expression"; // Добавлено, если нужно сортировать по выражению
            case "name" -> "u.name"; // Исправлено: u.name, так как f.user_name не существует
            case "x_val" -> "t.x_val"; // Исправлено: t.x_val, так как f.x_val не существует
            case "y_val" -> "t.y_val"; // Исправлено: t.y_val, так как f.y_val не существует
            case "operations_type_id" -> "o.operations_type_id"; // Исправлено: o.operations_type_id, так как f.operations_type_id не существует
            case "operation_id" -> "o.id"; // Добавлено, если нужно сортировать по ID операции
            case "tabulated_function_id" -> "t.id"; // Добавлено, если нужно сортировать по ID табулированной точки
            default -> "f.id"; // Исправлено: f.id вместо f.function_id
        };
    }
}