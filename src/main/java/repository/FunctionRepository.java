package repository;

import model.Function;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FunctionRepository {
    private static final Logger logger = Logger.getLogger(FunctionRepository.class.getName());

    // CREATE
    public Long createFunction(Function function) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionCreate.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, function.getUserId());
            stmt.setString(2, function.getName());
            stmt.setString(3, function.getDataFormat());
            stmt.setString(4, function.getFunctionSource());
            stmt.setString(5, function.getExpression());

            if (function.getParentFunctionId() != null) {
                stmt.setLong(6, function.getParentFunctionId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                logger.info("Function created successfully with ID: " + id);
                return id;
            }
            throw new SQLException("Failed to create function");
        } catch (SQLException e) {
            logger.severe("Error creating function: " + e.getMessage());
            throw e;
        }
    }

    // READ - поиск по ID
    public Function findById(Long id) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionFindById.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToFunction(rs);
            }
            return null;
        } catch (SQLException e) {
            logger.severe("Error finding function by ID " + id + ": " + e.getMessage());
            throw e;
        }
    }

    // READ - функции пользователя
    public List<Function> findByUserId(Long userId) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionFindByUserId.sql");
        List<Function> functions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToFunction(rs));
            }
            logger.info("Found " + functions.size() + " functions for user: " + userId);
            return functions;
        } catch (SQLException e) {
            logger.severe("Error finding functions for user " + userId + ": " + e.getMessage());
            throw e;
        }
    }

    // UPDATE
    public boolean updateFunction(Function function) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionUpdate.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, function.getId());
            stmt.setString(2, function.getName());
            stmt.setString(3, function.getDataFormat());
            stmt.setString(4, function.getFunctionSource());
            stmt.setString(5, function.getExpression());

            if (function.getParentFunctionId() != null) {
                stmt.setLong(6, function.getParentFunctionId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Function updated successfully: " + function.getId());
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Error updating function: " + e.getMessage());
            throw e;
        }
    }

    // DELETE
    public boolean deleteFunction(Long id) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionDelete.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Function deleted successfully: " + id);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Error deleting function: " + e.getMessage());
            throw e;
        }
    }

    private Function mapResultSetToFunction(ResultSet rs) throws SQLException {
        Function function = new Function();
        function.setId(rs.getLong("id"));
        function.setUserId(rs.getLong("user_id"));
        function.setName(rs.getString("name"));
        function.setDataFormat(rs.getString("data_format"));
        function.setFunctionSource(rs.getString("function_source"));
        function.setExpression(rs.getString("expression"));

        long parentId = rs.getLong("parent_function_id");
        if (!rs.wasNull()) {
            function.setParentFunctionId(parentId);
        }

        function.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return function;
    }
}