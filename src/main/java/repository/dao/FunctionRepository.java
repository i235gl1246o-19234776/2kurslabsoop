package repository.dao;

import model.entity.Function;
import model.entity.SearchFunctionResult;
import repository.DatabaseConnection;
import repository.SqlLoader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class FunctionRepository {
    private static final Logger logger = Logger.getLogger(FunctionRepository.class.getName());

    private final DatabaseConnection databaseConnection;

    public FunctionRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }
    public FunctionRepository() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        this.databaseConnection = databaseConnection;
    }

    private Connection getConnection() throws SQLException {
        if (databaseConnection != null) {
            return databaseConnection.getConnection();
        }
        DatabaseConnection defaultConnection = new DatabaseConnection();
        return defaultConnection.getConnection();
    }

    public void createPerformanceTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS method_performance (
                id BIGSERIAL PRIMARY KEY,
                method_name VARCHAR(100) NOT NULL,
                execution_time_ms DOUBLE PRECISION NOT NULL,
                parameters TEXT,
                success BOOLEAN NOT NULL,
                executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Таблица method_performance создана или уже существует");
        }
    }

    public Long createFunction(Function function) throws SQLException {
        long startTime = System.nanoTime();
        boolean success = false;
        String params = "userId=" + function .getUserId() + ", type=" + function.getTypeFunction() + ", name=" + function.getFunctionName();

        try {
            String sql = SqlLoader.loadSql("functions/insert_function.sql");

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setLong(1, function.getUserId());
                stmt.setString(2, function.getTypeFunction());
                stmt.setString(3, function.getFunctionName());
                stmt.setString(4, function.getFunctionExpression());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Создание функции не удалось, ни одна строка не была изменена.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        logger.info("Функция успешно создана с ID: " + id);
                        success = true;
                        return id;
                    } else {
                        throw new SQLException("Создание функции не удалось, ID не получен.");
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при создании функции: " + e.getMessage());
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;
            //savePerformanceMetric("createFunction", duration, params, success);
        }
    }

    public Optional<Function> findById(Long id, Long userId) throws SQLException {
        long startTime = System.nanoTime();
        boolean success = false;
        String params = "id=" + id + ", userId=" + userId;

        try {
            String sql = SqlLoader.loadSql("functions/find_id_functions.sql");

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, id);
                stmt.setLong(2, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Function function = mapResultSetToFunction(rs);
                    logger.info("Найдена функция по ID: " + id);
                    success = true;
                    return Optional.of(function);
                }
                logger.info("Функция не найдена с ID: " + id);
                success = true;
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функции по ID " + id + ": " + e.getMessage());
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;
            //savePerformanceMetric("findById", duration, params, success);
        }
    }

    public List<Function> findByUserId(Long userId) throws SQLException {
        long startTime = System.nanoTime();
        boolean success = false;
        String params = "userId=" + userId;

        try {
            String sql = SqlLoader.loadSql("functions/find_user_functions.sql");
            List<Function> functions = new ArrayList<>();

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    functions.add(mapResultSetToFunction(rs));
                }
                logger.info("Найдено " + functions.size() + " функций для пользователя с ID: " + userId);
                success = true;
                return functions;
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функций для пользователя с ID " + userId + ": " + e.getMessage());
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;
            //savePerformanceMetric("findByUserId", duration, params, success);
        }
    }

    public List<Function> findByName(Long userId, String namePattern) throws SQLException {
        long startTime = System.nanoTime();
        boolean success = false;
        String params = "userId=" + userId + ", namePattern=" + namePattern;

        try {
            String sql = SqlLoader.loadSql("functions/find_name_functions.sql");
            List<Function> functions = new ArrayList<>();

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, userId);
                stmt.setString(2, "%" + namePattern + "%");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    functions.add(mapResultSetToFunction(rs));
                }
                logger.info("Найдено " + functions.size() + " функций по шаблону имени: " + namePattern);
                success = true;
                return functions;
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функций по шаблону имени: " + e.getMessage());
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;
            //savePerformanceMetric("findByName", duration, params, success);
        }
    }

    public List<Function> findByType(Long userId, String type) throws SQLException {
        long startTime = System.nanoTime();
        boolean success = false;
        String params = "userId=" + userId + ", type=" + type;

        try {
            String sql = SqlLoader.loadSql("functions/find_type_functions.sql");
            List<Function> functions = new ArrayList<>();

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, userId);
                stmt.setString(2, type);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    functions.add(mapResultSetToFunction(rs));
                }
                logger.info("Найдено " + functions.size() + " функций типа: " + type);
                success = true;
                return functions;
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функций по типу: " + e.getMessage());
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;
            //savePerformanceMetric("findByType", duration, params, success);
        }
    }

    public boolean updateFunction(Function function) throws SQLException {
        long startTime = System.nanoTime();
        boolean success = false;
        String params = "id=" + function.getId() + ", userId=" + function.getUserId() + ", name=" + function.getFunctionName();

        try {
            String sql = SqlLoader.loadSql("functions/update_function.sql");

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, function.getTypeFunction());
                stmt.setString(2, function.getFunctionName());
                stmt.setString(3, function.getFunctionExpression());
                stmt.setLong(4, function.getId());
                stmt.setLong(5, function.getUserId());

                int rowsAffected = stmt.executeUpdate();
                success = rowsAffected > 0;

                if (success) {
                    logger.info("Функция успешно обновлена: " + function.getId());
                } else {
                    logger.warning("Функция для обновления не найдена с ID: " + function.getId());
                }
                return success;
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении функции: " + e.getMessage());
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;
            //savePerformanceMetric("updateFunction", duration, params, success);
        }
    }

    public boolean deleteFunction(Long id, Long userId) throws SQLException {
        long startTime = System.nanoTime();
        boolean success = false;
        String params = "id=" + id + ", userId=" + userId;

        try {
            String sql = SqlLoader.loadSql("functions/delete_function.sql");

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, id);
                stmt.setLong(2, userId);
                int rowsAffected = stmt.executeUpdate();
                success = rowsAffected > 0;

                if (success) {
                    logger.info("Функция успешно удалена: " + id);
                } else {
                    logger.warning("Функция для удаления не найдена с ID: " + id);
                }
                return success;
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении функции: " + e.getMessage());
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;
            //savePerformanceMetric("deleteFunction", duration, params, success);
        }
    }

    public List<SearchFunctionResult> search(
            Long userId,
            String userName,
            String functionNamePattern,
            String typeFunction,
            Double xVal,
            Double yVal,
            Long operationsTypeId,
            String sortBy,
            String sortOrder) throws SQLException {

        long startTime = System.nanoTime();
        boolean success = false;
        String params = String.format(
                "userId=%s, userName=%s, functionName=%s, type=%s, x=%s, y=%s, operationsTypeId=%s",
                userId, userName, functionNamePattern, typeFunction, xVal, yVal, operationsTypeId
        );

        try {
            if (!"function_id".equals(sortBy) && !"function_name".equals(sortBy) &&
                    !"type_function".equals(sortBy) && !"user_name".equals(sortBy)) {
                sortBy = "function_id";
            }
            if (!"asc".equals(sortOrder) && !"desc".equals(sortOrder)) {
                sortOrder = "asc";
            }

            StringBuilder sql = new StringBuilder("""
            SELECT 
                f.id AS function_id,
                f.user_id,
                u.name AS user_name,
                f.function_name,
                f.function_expression,
                f.type_function
            FROM functions f
            JOIN "users" u ON f.user_id = u.id
            """);
            boolean needsOperationsJoin = operationsTypeId != null;
            if (needsOperationsJoin) {
                sql.append(" LEFT JOIN operations o ON f.id = o.function_id ");
            }

            boolean needsTabulatedJoin = (xVal != null || yVal != null);
            if (needsTabulatedJoin) {
                sql.append(" LEFT JOIN tabulated_functions t ON f.id = t.function_id ");
            }

            sql.append(" WHERE 1=1 ");

            var paramsList = new ArrayList<Object>();

            if (userId != null) {
                sql.append(" AND f.user_id = ? ");
                paramsList.add(userId);
            }
            if (userName != null && !userName.trim().isEmpty()) {
                sql.append(" AND u.name ILIKE ? ");
                paramsList.add("%" + userName.trim() + "%");
            }
            if (functionNamePattern != null && !functionNamePattern.trim().isEmpty()) {
                sql.append(" AND f.function_name ILIKE ? ");
                paramsList.add("%" + functionNamePattern.trim() + "%");
            }
            if (typeFunction != null && !typeFunction.trim().isEmpty()) {
                sql.append(" AND f.type_function = ? ");
                paramsList.add(typeFunction.trim());
            }
            if (xVal != null) {
                sql.append(" AND t.x_val = ? ");
                paramsList.add(xVal);
            }
            if (yVal != null) {
                sql.append(" AND t.y_val = ? ");
                paramsList.add(yVal);
            }
            if (operationsTypeId != null) {
                sql.append(" AND o.operations_type_id = ? ");
                paramsList.add(operationsTypeId);
            }

            sql.append(" GROUP BY f.id, u.id ");
            sql.append(" ORDER BY ").append(sortBy).append(" ").append(sortOrder);

            logger.info(String.format(
                    "Выполняется поиск функций: userId=%s, userName='%s', functionName='%s', type='%s', x=%s, y=%s, sortBy=%s, sortOrder=%s",
                    userId,
                    userName,
                    functionNamePattern,
                    typeFunction,
                    xVal,
                    yVal,
                    sortBy,
                    sortOrder
            ));

            List<SearchFunctionResult> results = new ArrayList<>();

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

                for (int i = 0; i < paramsList.size(); i++) {
                    stmt.setObject(i + 1, paramsList.get(i));
                }

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    results.add(new SearchFunctionResult(
                            rs.getLong("function_id"),
                            rs.getLong("user_id"),
                            rs.getString("user_name"),
                            rs.getString("function_name"),
                            rs.getString("function_expression"),
                            rs.getString("type_function")
                    ));
                }

                logger.info("Поиск завершён: найдено " + results.size() + " функций");
                success = true;
                return results;

            }
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функций: " + e.getMessage());
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;
            //savePerformanceMetric("search", duration, params, success);
        }
    }

    public void printPerformanceStats() throws SQLException {
        String sql = """
            SELECT 
                method_name,
                COUNT(*) as call_count,
                AVG(execution_time_ms) as avg_time_ms,
                MIN(execution_time_ms) as min_time_ms,
                MAX(execution_time_ms) as max_time_ms,
                SUM(CASE WHEN success = true THEN 1 ELSE 0 END) as success_count
            FROM method_performance 
            GROUP BY method_name
            ORDER BY avg_time_ms DESC
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            logger.info("=== СТАТИСТИКА ПРОИЗВОДИТЕЛЬНОСТИ МЕТОДОВ ===");
            while (rs.next()) {
                logger.info(String.format(
                        "Метод: %s, Вызовов: %d, Среднее: %.2f мс, Min: %.2f мс, Max: %.2f мс, Успешно: %d",
                        rs.getString("method_name"),
                        rs.getInt("call_count"),
                        rs.getDouble("avg_time_ms"),
                        rs.getDouble("min_time_ms"),
                        rs.getDouble("max_time_ms"),
                        rs.getInt("success_count")
                ));
            }
        }
    }

    private Function mapResultSetToFunction(ResultSet rs) throws SQLException {
        Function function = new Function();
        function.setId(rs.getLong("id"));
        function.setUserId(rs.getLong("user_id"));
        function.setTypeFunction(rs.getString("type_function"));
        function.setFunctionName(rs.getString("function_name"));
        function.setFunctionExpression(rs.getString("function_expression"));
        return function;
    }
}