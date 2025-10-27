package repository;

import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UserRepository {
    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());

    public Long createUser(User user) throws SQLException {
        String sql = SqlLoader.loadSql("UserCreate.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                logger.info("User created successfully with ID: " + id);
                return id;
            }
            throw new SQLException("Failed to create user");
        } catch (SQLException e) {
            logger.severe("Error creating user: " + e.getMessage());
            throw e;
        }
    }

    public User findById(Long id) throws SQLException {
        String sql = SqlLoader.loadSql("UserFindById.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            logger.severe("Error finding user by ID " + id + ": " + e.getMessage());
            throw e;
        }
    }

    // READ - поиск по имени
    public User findByUsername(String username) throws SQLException {
        String sql = SqlLoader.loadSql("UserFindByUsername.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            logger.severe("Error finding user by username " + username + ": " + e.getMessage());
            throw e;
        }
    }

    // READ - все пользователи
    public List<User> findAll() throws SQLException {
        String sql = SqlLoader.loadSql("UserFindAll.sql");
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            logger.info("Found " + users.size() + " users");
            return users;
        } catch (SQLException e) {
            logger.severe("Error finding all users: " + e.getMessage());
            throw e;
        }
    }

    // UPDATE - обновление пароля
    public boolean updatePassword(Long userId, String newPasswordHash) throws SQLException {
        String sql = SqlLoader.loadSql("UserUpdatePassword.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(2, userId);
            stmt.setString(1, newPasswordHash);

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("User password updated successfully: " + userId);
            } else {
                logger.warning("No user found to update with ID: " + userId);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Error updating user password: " + e.getMessage());
            throw e;
        }
    }

    // DELETE
    public boolean deleteUser(Long id) throws SQLException {
        String sql = SqlLoader.loadSql("UserDelete.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("User deleted successfully: " + id);
            } else {
                logger.warning("No user found to delete with ID: " + id);
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Error deleting user: " + e.getMessage());
            throw e;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
    public boolean updateUser(User user) throws SQLException {
        String sql = SqlLoader.loadSql("UserUpdate.sql");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setLong(3, user.getId());

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("User updated successfully: " + user.getId());
            } else {
                logger.warning("No user found to update with ID: " + user.getId());
            }
            return success;
        } catch (SQLException e) {
            logger.severe("Error updating user: " + e.getMessage());
            throw e;
        }
    }

}