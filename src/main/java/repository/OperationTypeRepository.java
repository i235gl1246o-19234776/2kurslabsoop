package repository;

import model.OperationType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class OperationTypeRepository {
    private static final Logger logger = Logger.getLogger(OperationTypeRepository.class.getName());

    // READ: Получение всех типов операций
    public List<OperationType> findAll() throws SQLException {
        String sql = SqlLoader.loadSql("OperationTypeFindAll.sql");
        List<OperationType> operationTypes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                operationTypes.add(mapResultSetToOperationType(rs));
            }
            logger.info("Found " + operationTypes.size() + " operation types");
            return operationTypes;
        } catch (SQLException e) {
            logger.severe("Error finding all operation types: " + e.getMessage());
            throw e;
        }
    }

    // READ: Поиск по ID
    public OperationType findById(Integer id) throws SQLException {
        String sql = SqlLoader.loadSql("OperationTypeFindById.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToOperationType(rs);
            }
            return null;
        } catch (SQLException e) {
            logger.severe("Error finding operation type by ID " + id + ": " + e.getMessage());
            throw e;
        }
    }

    // Инициализация предопределенных типов операций (использует встроенные SQL)
    public void initializePredefinedTypes() throws SQLException {
        logger.info("Initializing predefined operation types...");

        String initSql = """
            INSERT INTO operation_types (id, name, result_type) VALUES
                (1, 'AndThen', 'function'),
                (2, 'NewtonMethod', 'num'),
                (3, 'RungeMethod', 'num'),
                (4, 'Derivative', 'function'),
                (5, 'DefiniteIntegral', 'num'),
                (6, 'Evaluate', 'num')
            ON CONFLICT (id) DO NOTHING;
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(initSql)) {

            stmt.executeUpdate();
            logger.info("Predefined operation types initialized");
        } catch (SQLException e) {
            logger.severe("Error initializing operation types: " + e.getMessage());
            throw e;
        }
    }

    private OperationType mapResultSetToOperationType(ResultSet rs) throws SQLException {
        OperationType operationType = new OperationType();
        operationType.setId(rs.getInt("id"));
        operationType.setName(rs.getString("name"));
        operationType.setResultType(rs.getString("result_type"));
        return operationType;
    }
}