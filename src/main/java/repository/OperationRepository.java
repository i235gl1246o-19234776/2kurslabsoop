package repository;

import model.Operation;
import java.sql.*;
import java.util.Optional;
import java.util.logging.Logger;

public class OperationRepository {
    private static final Logger logger = Logger.getLogger(OperationRepository.class.getName());

    public Long createOperation(Operation operation) throws SQLException {
        String sql = SqlLoader.loadSql("operations/insert_operations.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, operation.getFunctionId());
            stmt.setInt(2, operation.getOperationsTypeId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание операции не удалось, ни одна строка не была изменена.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.info("Операция успешно создана с ID: " + id);
                    return id;
                } else {
                    throw new SQLException("Создание операции не удалось, ID не получен.");
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при создании операции: " + e.getMessage());
            throw e;
        }
    }

    public Optional<Operation> findById(Long id, Long functionId) throws SQLException {
        String sql = SqlLoader.loadSql("operations/find_operation.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.setLong(2, functionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Operation operation = mapResultSetToOperation(rs);
                logger.info("Найдена операция по ID: " + id);
                return Optional.of(operation);
            }
            logger.info("Операция не найдена с ID: " + id);
            return Optional.empty();
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске операции по ID " + id + ": " + e.getMessage());
            throw e;
        }
    }

    public boolean updateOperation(Operation operation) throws SQLException {
        String sql = SqlLoader.loadSql("operations/update_operations.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, operation.getOperationsTypeId());
            stmt.setLong(2, operation.getId());
            stmt.setLong(3, operation.getFunctionId());

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Операция успешно обновлена: " + operation.getId());
            } else {
                logger.warning("Операция для обновления не найдена с ID: " + operation.getId());
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении операции: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteOperation(Long id, Long functionId) throws SQLException {
        String sql = "DELETE FROM operations WHERE id = ? AND function_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.setLong(2, functionId);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Операция успешно удалена: " + id);
            } else {
                logger.warning("Операция для удаления не найдена с ID: " + id);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении операции: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteAllOperations(Long functionId) throws SQLException {
        String sql = "DELETE FROM operations WHERE function_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Все операции удалены для функции с ID: " + functionId);
            } else {
                logger.info("Операции для удаления не найдены для функции с ID: " + functionId);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении всех операций: " + e.getMessage());
            throw e;
        }
    }

    private Operation mapResultSetToOperation(ResultSet rs) throws SQLException {
        Operation operation = new Operation();
        operation.setId(rs.getLong("id"));
        operation.setFunctionId(rs.getLong("function_id"));
        operation.setOperationsTypeId(rs.getInt("operations_type_id"));
        return operation;
    }
}