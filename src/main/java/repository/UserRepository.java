package repository;

import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class UserRepository {
    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());
    public Long createUser(User user) throws SQLException {
        String sql = SqlLoader.loadSql("user/insert_user.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPasswordHash());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Создание пользователя не удалось, ни одна строка не была изменена.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.info("Пользователь успешно создан с ID: " + id);
                    return id;
                } else {
                    throw new SQLException("Создание пользователя не удалось, ID не получен.");
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при создании пользователя: " + e.getMessage());
            throw e;
        }
    }

    public Optional<User> findById(Long id) throws SQLException {
        String sql = SqlLoader.loadSql("user/find_user_id.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                logger.info("Найден пользователь по ID: " + id);
                return Optional.of(user);
            }
            logger.info("Пользователь не найден с ID: " + id);
            return Optional.empty();
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске пользователя по ID " + id + ": " + e.getMessage());
            throw e;
        }
    }

    public Optional<User> findByName(String name) throws SQLException {
        String sql = SqlLoader.loadSql("user/find_user_name.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                logger.info("Найден пользователь по имени: " + name);
                return Optional.of(user);
            }
            logger.info("Пользователь не найден с именем: " + name);
            return Optional.empty();
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске пользователя по имени " + name + ": " + e.getMessage());
            throw e;
        }
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = SqlLoader.loadSql("user/update_user.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPasswordHash());
            stmt.setLong(3, user.getId());

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Пользователь успешно обновлен: " + user.getId());
            } else {
                logger.warning("Пользователь для обновления не найден с ID: " + user.getId());
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении пользователя: " + e.getMessage());
            throw e;
        }
    }

    public boolean authenticateUser(String name, String passwordHash) throws SQLException {
        String sql = SqlLoader.loadSql("user/user_autentification.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();

            boolean authenticated = rs.next();
            logger.info("Аутентификация пользователя " + (authenticated ? "успешна" : "неудачна") + " для: " + name);
            return authenticated;
        } catch (SQLException e) {
            logger.severe("Ошибка при аутентификации пользователя: " + e.getMessage());
            throw e;
        }
    }

    public boolean userNameExists(String name) throws SQLException {
        String sql = SqlLoader.loadSql("user/user_name_is_exist.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean exists = rs.getInt("count") > 0;
                logger.info("Проверка существования имени '" + name + "': " + exists);
                return exists;
            }
            return false;
        } catch (SQLException e) {
            logger.severe("Ошибка при проверке существования имени пользователя: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteUser(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Пользователь успешно удален: " + id);
            } else {
                logger.warning("Пользователь для удаления не найден с ID: " + id);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении пользователя: " + e.getMessage());
            throw e;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setPasswordHash(rs.getString("password_hash"));
        return user;
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, name FROM users;"; // Загрузка SQL из файла
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection(); // Получение соединения
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
                user.setPasswordHash(rs.getString("password_hash"));
                users.add(user);
            }
            logger.info("Найдено " + users.size() + " пользователей");
            return users;
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске всех пользователей: " + e.getMessage());
            throw e;
        }
    }
}