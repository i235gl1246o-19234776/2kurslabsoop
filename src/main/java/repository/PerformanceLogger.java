package repository;

import java.sql.*;
import java.util.logging.Logger;

public class PerformanceLogger {
    private static final Logger logger = Logger.getLogger(PerformanceLogger.class.getName());

    public void logPerformance(String testName, String queryDescription, long executionTimeMs, int resultCount) {
        String sql = """
            INSERT INTO performance_log (test_name, query_description, execution_time_ms, result_count)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, testName);
            stmt.setString(2, queryDescription);
            stmt.setLong(3, executionTimeMs);
            stmt.setInt(4, resultCount);

            stmt.executeUpdate();
            logger.info("✅ Производительность сохранена: " + testName + " (" + executionTimeMs + " мс)");

        } catch (SQLException e) {
            logger.severe("❌ Ошибка логирования производительности: " + e.getMessage());
        }
    }
}