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
import java.util.Map;
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

    public SearchFunctionResponseDTO searchFunctions(SearchFunctionRequestDTO request) throws SQLException {
        SearchQueryBuilder queryBuilder = new SearchQueryBuilder(request);
        String sql = queryBuilder.buildSearchSql();
        List<Object> parameters = queryBuilder.getParameters();

        System.out.println("Executing Search SQL: " + sql);
        System.out.println("Parameters: " + parameters);

        List<FunctionResponseDTO> functions = new ArrayList<>();
        DatabaseConnection dbconn = new DatabaseConnection();

        try (Connection conn = dbconn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, parameters);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    functions.add(mapResultSetToFunction(rs));
                }
            }
        }

        int total = getTotalCount(request);
        return new SearchFunctionResponseDTO(functions, total);
    }

    private int getTotalCount(SearchFunctionRequestDTO request) throws SQLException {
        SearchQueryBuilder queryBuilder = new SearchQueryBuilder(request);
        String countSql = queryBuilder.buildCountSql();
        List<Object> parameters = queryBuilder.getParameters();

        DatabaseConnection dbconn = new DatabaseConnection();

        try (Connection conn = dbconn.getConnection();
             PreparedStatement stmt = conn.prepareStatement(countSql)) {

            setParameters(stmt, parameters);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // Вспомогательные классы и методы
    private static class SearchQueryBuilder {
        private final SearchFunctionRequestDTO request;
        private final List<Object> parameters;
        private final StringBuilder whereClause;

        public SearchQueryBuilder(SearchFunctionRequestDTO request) {
            this.request = request;
            this.parameters = new ArrayList<>();
            this.whereClause = new StringBuilder();
            buildWhereClause();
        }

        private void buildWhereClause() {
            Map<String, Condition> conditions = Map.of(
                    "userName", new Condition("u.name", request.getUserName(), ConditionType.EQUAL),
                    "functionName", new Condition("f.function_name", request.getFunctionName(), ConditionType.LIKE_START),
                    "typeFunction", new Condition("f.type_function", request.getTypeFunction(), ConditionType.EQUAL),
                    "xVal", new Condition("t.x_val", request.getXVal(), ConditionType.EQUAL),
                    "yVal", new Condition("t.y_val", request.getYVal(), ConditionType.EQUAL),
                    "operationsTypeId", new Condition("o.operations_type_id", request.getOperationsTypeId(), ConditionType.EQUAL)
            );

            conditions.forEach((field, condition) -> {
                if (condition.shouldInclude()) {
                    whereClause.append(" AND ").append(condition.buildClause());
                    parameters.add(condition.getParameterValue());
                }
            });
        }

        public String buildSearchSql() {
            StringBuilder sql = new StringBuilder("""
            SELECT
                f.id AS function_id,
                f.function_name,
                f.type_function,
                f.function_expression,
                u.name AS username,              
                o.id AS operation_id,
                o.operations_type_id,            
                t.id AS tabulated_function_id,
                t.x_val,                         
                t.y_val                          
            FROM functions f
            LEFT JOIN users u ON f.user_id = u.id
            LEFT JOIN operations o ON f.id = o.function_id
            LEFT JOIN tabulated_functions t ON f.id = t.function_id
            WHERE 1=1
        """);

            sql.append(whereClause);

            if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
                String sortField = getSortField(request.getSortBy());
                String sortOrder = "ASC".equalsIgnoreCase(request.getSortOrder()) ? "ASC" : "DESC";
                sql.append(" ORDER BY ").append(sortField).append(" ").append(sortOrder);
            }

            if (request.getPage() != null && request.getSize() != null) {
                int offset = (request.getPage() - 1) * request.getSize();
                sql.append(" LIMIT ? OFFSET ?");
                parameters.add(request.getSize());
                parameters.add(offset);
            }

            return sql.toString();
        }

        public String buildCountSql() {
            return """
            SELECT COUNT(*) 
            FROM functions f
            LEFT JOIN users u ON f.user_id = u.id
            LEFT JOIN operations o ON f.id = o.function_id
            LEFT JOIN tabulated_functions t ON f.id = t.function_id
            WHERE 1=1
        """ + whereClause;
        }

        public List<Object> getParameters() {
            return parameters;
        }
    }

    private static class Condition {
        private final String column;
        private final Object value;
        private final ConditionType type;

        public Condition(String column, Object value, ConditionType type) {
            this.column = column;
            this.value = value;
            this.type = type;
        }

        public boolean shouldInclude() {
            if (value == null) return false;
            if (value instanceof String stringValue) {
                return !stringValue.isEmpty();
            }
            return true;
        }

        public String buildClause() {
            return switch (type) {
                case EQUAL -> column + " = ?";
                case LIKE_START -> column + " LIKE ?";
            };
        }

        public Object getParameterValue() {
            return switch (type) {
                case EQUAL -> value;
                case LIKE_START -> value + "%";
            };
        }
    }

    private enum ConditionType {
        EQUAL, LIKE_START
    }

    // Вспомогательные методы
    private void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            stmt.setObject(i + 1, parameters.get(i));
        }
    }

    private FunctionResponseDTO mapResultSetToFunction(ResultSet rs) throws SQLException {
        FunctionResponseDTO function = new FunctionResponseDTO();
        function.setFunctionId(rs.getLong("function_id"));
        function.setFunctionName(rs.getString("function_name"));
        function.setTypeFunction(rs.getString("type_function"));
        function.setXVal(rs.getDouble("x_val"));
        function.setYVal(rs.getDouble("y_val"));
        function.setUserName(rs.getString("username"));
        function.setOperationsTypeId(rs.getLong("operations_type_id"));
        return function;
    }


    private static String getSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "function_id" -> "f.id";
            case "function_name" -> "f.function_name";
            case "type_function" -> "f.type_function";
            case "function_expression" -> "f.function_expression";
            case "name" -> "u.name";
            case "x_val" -> "t.x_val";
            case "y_val" -> "t.y_val";
            case "operations_type_id" -> "o.operations_type_id";
            case "operation_id" -> "o.id";
            case "tabulated_function_id" -> "t.id";
            default -> "f.id";
        };
    }
}