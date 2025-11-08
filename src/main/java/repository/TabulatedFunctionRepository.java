package repository;

import model.TabulatedFunction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class TabulatedFunctionRepository {
    private static final Logger logger = Logger.getLogger(TabulatedFunctionRepository.class.getName());

    public Long createTabulatedFunction(TabulatedFunction tf) throws SQLException {
        String sql = SqlLoader.loadSql("tabulated_function/insert_tabulated_function.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, tf.getFunctionId());
            stmt.setDouble(2, tf.getXVal());
            stmt.setDouble(3, tf.getYVal());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание точки табулированной функции не удалось.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.info("Точка табулированной функции успешно создана с ID: " + id);
                    return id;
                } else {
                    throw new SQLException("Создание точки не удалось, ID не получен.");
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при создании точки табулированной функции: " + e.getMessage());
            throw e;
        }
    }

    public List<TabulatedFunction> findAllByFunctionId(Long functionId) throws SQLException {
        String sql = SqlLoader.loadSql("tabulated_function/find_all_tabulated_function.sql");
        List<TabulatedFunction> points = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                points.add(mapResultSetToTabulatedFunction(rs));
            }
            logger.info("Найдено " + points.size() + " точек для функции с ID: " + functionId);
            return points;
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске точек табулированной функции: " + e.getMessage());
            throw e;
        }
    }

    public Optional<TabulatedFunction> findByXValue(Long functionId, Double xVal) throws SQLException {
        String sql = SqlLoader.loadSql("tabulated_function/find_point_x_tabulated_function.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            stmt.setDouble(2, xVal);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                TabulatedFunction point = mapResultSetToTabulatedFunction(rs);
                logger.info("Найдена точка с x=" + xVal + " для функции с ID: " + functionId);
                return Optional.of(point);
            }
            logger.info("Точка не найдена с x=" + xVal + " для функции с ID: " + functionId);
            return Optional.empty();
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске точки по значению X: " + e.getMessage());
            throw e;
        }
    }

    public List<TabulatedFunction> findBetweenXValues(Long functionId, Double xMin, Double xMax) throws SQLException {
        String sql = SqlLoader.loadSql("tabulated_function/find_tabulated_functions_between.sql");
        List<TabulatedFunction> points = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            stmt.setDouble(2, xMin);
            stmt.setDouble(3, xMax);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                points.add(mapResultSetToTabulatedFunction(rs));
            }
            logger.info("Найдено " + points.size() + " точек между x=" + xMin + " и x=" + xMax);
            return points;
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске точек в диапазоне значений X: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateTabulatedFunction(TabulatedFunction tf) throws SQLException {
        String sql = SqlLoader.loadSql("tabulated_function/update_tabulated_functions.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, tf.getXVal());
            stmt.setDouble(2, tf.getYVal());
            stmt.setLong(3, tf.getId());
            stmt.setLong(4, tf.getFunctionId());

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Точка табулированной функции успешно обновлена: " + tf.getId());
            } else {
                logger.warning("Точка для обновления не найдена с ID: " + tf.getId());
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении точки табулированной функции: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteTabulatedFunction(Long id) throws SQLException {
        String sql = SqlLoader.loadSql("tabulated_function/delete_tabulated_function.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Точка табулированной функции успешно удалена: " + id);
            } else {
                logger.warning("Точка для удаления не найдена с ID: " + id);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении точки табулированной функции: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteAllTabulatedFunctions(Long functionId) throws SQLException {
        String sql = SqlLoader.loadSql("tabulated_function/delete_all_tabulated_function.sql");


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Все точки табулированной функции удалены для функции с ID: " + functionId);
            } else {
                logger.info("Точки для удаления не найдены для функции с ID: " + functionId);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении всех точек табулированной функции: " + e.getMessage());
            throw e;
        }
    }

    private TabulatedFunction mapResultSetToTabulatedFunction(ResultSet rs) throws SQLException {
        TabulatedFunction tf = new TabulatedFunction();
        tf.setId(rs.getLong("id"));
        tf.setFunctionId(rs.getLong("function_id"));
        tf.setXVal(rs.getDouble("x_val"));
        tf.setYVal(rs.getDouble("y_val"));
        return tf;
    }

    public Optional<TabulatedFunction> findById(Long pointId) throws SQLException {
        String sql = SqlLoader.loadSql("tabulated_function/find_by_id.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pointId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                TabulatedFunction point = mapResultSetToTabulatedFunction(rs);
                logger.info("Найдена точка с ID: " + pointId);
                return Optional.of(point);
            } else {
                logger.info("Точка с ID " + pointId + " не найдена.");
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске точки табулированной функции по ID: " + e.getMessage());
            throw e;
        }
    }
}