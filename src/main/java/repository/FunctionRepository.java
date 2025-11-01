package repository;

import model.Function;
import model.SearchFunctionResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class FunctionRepository {
    private static final Logger logger = Logger.getLogger(FunctionRepository.class.getName());

    public Long createFunction(Function function) throws SQLException {
        String sql = SqlLoader.loadSql("functions/insert_function.sql");

        try (Connection conn = DatabaseConnection.getConnection();
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
                    return id;
                } else {
                    throw new SQLException("Создание функции не удалось, ID не получен.");
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при создании функции: " + e.getMessage());
            throw e;
        }
    }

    public Optional<Function> findById(Long id, Long userId) throws SQLException {
        String sql = SqlLoader.loadSql("functions/find_id_functions.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Function function = mapResultSetToFunction(rs);
                logger.info("Найдена функция по ID: " + id);
                return Optional.of(function);
            }
            logger.info("Функция не найдена с ID: " + id);
            return Optional.empty();
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функции по ID " + id + ": " + e.getMessage());
            throw e;
        }
    }

    public List<Function> findByUserId(Long userId) throws SQLException {
        String sql = SqlLoader.loadSql("functions/find_user_functions.sql");
        List<Function> functions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToFunction(rs));
            }
            logger.info("Найдено " + functions.size() + " функций для пользователя с ID: " + userId);
            return functions;
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функций для пользователя с ID " + userId + ": " + e.getMessage());
            throw e;
        }
    }

    public List<Function> findByName(Long userId, String namePattern) throws SQLException {
        String sql = SqlLoader.loadSql("functions/find_name_functions.sql");
        List<Function> functions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, "%" + namePattern + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToFunction(rs));
            }
            logger.info("Найдено " + functions.size() + " функций по шаблону имени: " + namePattern);
            return functions;
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функций по шаблону имени: " + e.getMessage());
            throw e;
        }
    }

    public List<Function> findByType(Long userId, String type) throws SQLException {
        String sql = SqlLoader.loadSql("functions/find_type_functions.sql");
        List<Function> functions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToFunction(rs));
            }
            logger.info("Найдено " + functions.size() + " функций типа: " + type);
            return functions;
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функций по типу: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateFunction(Function function) throws SQLException {
        String sql = SqlLoader.loadSql("functions/update_function.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, function.getTypeFunction());
            stmt.setString(2, function.getFunctionName());
            stmt.setString(3, function.getFunctionExpression());
            stmt.setLong(4, function.getId());
            stmt.setLong(5, function.getUserId());

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Функция успешно обновлена: " + function.getId());
            } else {
                logger.warning("Функция для обновления не найдена с ID: " + function.getId());
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении функции: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteFunction(Long id, Long userId) throws SQLException {
        String sql = "DELETE FROM functions WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Функция успешно удалена: " + id);
            } else {
                logger.warning("Функция для удаления не найдена с ID: " + id);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении функции: " + e.getMessage());
            throw e;
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

        // Валидация сортировки
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
            sql.append(" LEFT JOIN tabulated_function t ON f.id = t.function_id ");
        }

        sql.append(" WHERE 1=1 ");

        var params = new ArrayList<Object>();

        if (userId != null) {
            sql.append(" AND f.user_id = ? ");
            params.add(userId);
        }
        if (userName != null && !userName.trim().isEmpty()) {
            sql.append(" AND u.name ILIKE ? ");
            params.add("%" + userName.trim() + "%");
        }
        if (functionNamePattern != null && !functionNamePattern.trim().isEmpty()) {
            sql.append(" AND f.function_name ILIKE ? ");
            params.add("%" + functionNamePattern.trim() + "%");
        }
        if (typeFunction != null && !typeFunction.trim().isEmpty()) {
            sql.append(" AND f.type_function = ? ");
            params.add(typeFunction.trim());
        }
        if (xVal != null) {
            sql.append(" AND t.x_val = ? ");
            params.add(xVal);
        }
        if (yVal != null) {
            sql.append(" AND t.y_val = ? ");
            params.add(yVal);
        }
        if (operationsTypeId != null) {
            sql.append(" AND o.operations_type_id = ? ");
            params.add(operationsTypeId);
        }

        sql.append(" GROUP BY f.id, u.id ");
        sql.append(" ORDER BY ").append(sortBy).append(" ").append(sortOrder);

        // === ЛОГИРОВАНИЕ ===
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
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
            return results;

        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функций: " + e.getMessage());
            throw e;
        }
    }
}