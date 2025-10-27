package repository;

import model.FunctionOperation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FunctionOperationRepository {
    private static final Logger logger = Logger.getLogger(FunctionOperationRepository.class.getName());

    // CREATE
    public Long createFunctionOperation(FunctionOperation operation) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionOperationCreate.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, operation.getFunctionId());
            stmt.setInt(2, operation.getOperationTypeId());
            stmt.setString(3, operation.getParameters());

            if (operation.getResultValue() != null) {
                stmt.setDouble(4, operation.getResultValue());
            } else {
                stmt.setNull(4, Types.DOUBLE);
            }

            if (operation.getResultFunctionId() != null) {
                stmt.setLong(5, operation.getResultFunctionId());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                logger.info("Function operation created successfully with ID: " + id);
                return id;
            }
            throw new SQLException("Failed to create function operation");
        } catch (SQLException e) {
            logger.severe("Error creating function operation: " + e.getMessage());
            throw e;
        }
    }

    // READ - поиск по ID
    public FunctionOperation findById(Long id) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionOperationFindById.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToFunctionOperation(rs);
            }
            return null;
        } catch (SQLException e) {
            logger.severe("Error finding function operation by ID " + id + ": " + e.getMessage());
            throw e;
        }
    }

    // READ - операции функции
    public List<FunctionOperation> findByFunctionId(Long functionId) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionOperationFindByFunctionId.sql");
        List<FunctionOperation> operations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                operations.add(mapResultSetToFunctionOperation(rs));
            }
            logger.info("Found " + operations.size() + " operations for function: " + functionId);
            return operations;
        } catch (SQLException e) {
            logger.severe("Error finding operations for function " + functionId + ": " + e.getMessage());
            throw e;
        }
    }

    // READ - операции по результирующей функции
    public List<FunctionOperation> findByResultFunctionId(Long resultFunctionId) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionOperationFindByResultFunctionId.sql");
        List<FunctionOperation> operations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, resultFunctionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                operations.add(mapResultSetToFunctionOperation(rs));
            }
            return operations;
        } catch (SQLException e) {
            logger.severe("Error finding operations by result function " + resultFunctionId + ": " + e.getMessage());
            throw e;
        }
    }

    // UPDATE - обновление результата
    public boolean updateResult(FunctionOperation operation) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionOperationUpdateResult.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, operation.getId());

            if (operation.getResultValue() != null) {
                stmt.setDouble(2, operation.getResultValue());
            } else {
                stmt.setNull(2, Types.DOUBLE);
            }

            if (operation.getResultFunctionId() != null) {
                stmt.setLong(3, operation.getResultFunctionId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Function operation result updated successfully: " + operation.getId());
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Error updating function operation result: " + e.getMessage());
            throw e;
        }
    }

    // DELETE
    public boolean deleteFunctionOperation(Long id) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionOperationDelete.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Function operation deleted successfully: " + id);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Error deleting function operation: " + e.getMessage());
            throw e;
        }
    }

    private FunctionOperation mapResultSetToFunctionOperation(ResultSet rs) throws SQLException {
        FunctionOperation operation = new FunctionOperation();
        operation.setId(rs.getLong("id"));
        operation.setFunctionId(rs.getLong("function_id"));
        operation.setOperationTypeId(rs.getInt("operation_type_id"));
        operation.setParameters(rs.getString("parameters"));

        double resultValue = rs.getDouble("result_value");
        if (!rs.wasNull()) {
            operation.setResultValue(resultValue);
        }

        long resultFunctionId = rs.getLong("result_function_id");
        if (!rs.wasNull()) {
            operation.setResultFunctionId(resultFunctionId);
        }

        operation.setExecutedAt(rs.getTimestamp("executed_at").toLocalDateTime());
        return operation;
    }
}