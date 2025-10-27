package repository;

import model.FunctionPoint;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FunctionPointRepository {
    private static final Logger logger = Logger.getLogger(FunctionPointRepository.class.getName());

    // CREATE
    public Long createFunctionPoint(FunctionPoint point) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionPointCreate.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, point.getFunctionId());
            stmt.setBigDecimal(2, point.getXVal());
            stmt.setDouble(3, point.getYVal());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                logger.info("Function point created/updated successfully with ID: " + id);
                return id;
            }
            throw new SQLException("Failed to create function point");
        } catch (SQLException e) {
            logger.severe("Error creating function point: " + e.getMessage());
            throw e;
        }
    }

    // READ - все точки функции
    public List<FunctionPoint> findAllByFunctionId(Long functionId) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionPointFindAllByFunctionId.sql");
        List<FunctionPoint> points = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                points.add(mapResultSetToFunctionPoint(rs, functionId));
            }
            logger.info("Found " + points.size() + " points for function: " + functionId);
            return points;
        } catch (SQLException e) {
            logger.severe("Error finding points for function " + functionId + ": " + e.getMessage());
            throw e;
        }
    }

    // READ - конкретная точка
    public FunctionPoint findOne(Long functionId, BigDecimal xVal) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionPointFindOne.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            stmt.setBigDecimal(2, xVal);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToFunctionPoint(rs, functionId);
            }
            return null;
        } catch (SQLException e) {
            logger.severe("Error finding point for function " + functionId + " at x=" + xVal + ": " + e.getMessage());
            throw e;
        }
    }

    // DELETE - все точки функции
    public boolean deleteByFunctionId(Long functionId) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionPointDeleteByFunctionId.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("All function points deleted for function: " + functionId);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Error deleting function points for function " + functionId + ": " + e.getMessage());
            throw e;
        }
    }

    // BATCH CREATE - массовое добавление точек
    public void createBatchPoints(Long functionId, List<FunctionPoint> points) throws SQLException {
        String sql = SqlLoader.loadSql("FunctionPointCreate.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (FunctionPoint point : points) {
                stmt.setLong(1, functionId);
                stmt.setBigDecimal(2, point.getXVal());
                stmt.setDouble(3, point.getYVal());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            logger.info("Batch created " + results.length + " function points for function: " + functionId);
        } catch (SQLException e) {
            logger.severe("Error batch creating function points: " + e.getMessage());
            throw e;
        }
    }

    private FunctionPoint mapResultSetToFunctionPoint(ResultSet rs, Long functionId) throws SQLException {
        FunctionPoint point = new FunctionPoint();
        point.setId(rs.getLong("id"));
        point.setFunctionId(functionId);
        point.setXVal(rs.getBigDecimal("x_val"));
        point.setYVal(rs.getDouble("y_val"));
        point.setComputedAt(rs.getTimestamp("computed_at").toLocalDateTime());
        return point;
    }
}