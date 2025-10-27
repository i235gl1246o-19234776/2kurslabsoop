package repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class SqlLoader {
    private static final Logger logger = Logger.getLogger(SqlLoader.class.getName());

    // Загрузка SQL по имени файла (например, "FunctionCreate.sql")
    public static String loadSql(String filename) {
        String path = "scripts/" + filename; // ← теперь путь: scripts/имя_файла.sql

        try (InputStream inputStream = SqlLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("SQL file not found: " + path);
            }
            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            logger.fine("Loaded SQL from: " + path);
            return sql.trim();
        } catch (IOException e) {
            logger.severe("Error loading SQL file: " + path + " - " + e.getMessage());
            throw new RuntimeException("Failed to load SQL file: " + path, e);
        }
    }
}