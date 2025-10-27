package service;

import model.User;
import repository.UserRepository;
import java.util.List;
import java.util.logging.Logger;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // CREATE - регистрация пользователя
    public User registerUser(String username, String password) {
        validateUsername(username);
        validatePassword(password);

        try {
            // Проверяем, не существует ли уже пользователь с таким именем
            User existingUser = userRepository.findByUsername(username);
            if (existingUser != null) {
                throw new IllegalArgumentException("User with username '" + username + "' already exists");
            }

            // Хэшируем пароль (в реальном приложении используйте BCrypt)
            String passwordHash = hashPassword(password);

            User user = new User(username, passwordHash);
            Long userId = userRepository.createUser(user);

            User createdUser = userRepository.findById(userId);
            logger.info("User registered successfully: " + username);
            return createdUser;

        } catch (Exception e) {
            logger.severe("Error registering user: " + e.getMessage());
            throw new RuntimeException("Registration failed", e);
        }
    }

    // READ - аутентификация пользователя
    public User authenticate(String username, String password) {
        validateUsername(username);
        validatePassword(password);

        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                logger.warning("Authentication failed: user not found - " + username);
                return null;
            }

            // Проверяем пароль (в реальном приложении используйте BCrypt)
            String inputPasswordHash = hashPassword(password);
            if (user.getPasswordHash().equals(inputPasswordHash)) {
                logger.info("User authenticated successfully: " + username);
                return user;
            } else {
                logger.warning("Authentication failed: invalid password for user - " + username);
                return null;
            }

        } catch (Exception e) {
            logger.severe("Error during authentication: " + e.getMessage());
            throw new RuntimeException("Authentication failed", e);
        }
    }

    // READ - получение пользователя по ID
    public User getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        try {
            User user = userRepository.findById(id);
            if (user == null) {
                logger.warning("User not found with ID: " + id);
            }
            return user;
        } catch (Exception e) {
            logger.severe("Error getting user by ID: " + e.getMessage());
            throw new RuntimeException("Failed to get user", e);
        }
    }

    // READ - получение пользователя по имени
    public User getUserByUsername(String username) {
        validateUsername(username);

        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            logger.severe("Error getting user by username: " + e.getMessage());
            throw new RuntimeException("Failed to get user", e);
        }
    }

    // READ - все пользователи
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            logger.severe("Error getting all users: " + e.getMessage());
            throw new RuntimeException("Failed to get users", e);
        }
    }

    // UPDATE - обновление данных пользователя (НОВЫЙ МЕТОД)
    public User updateUser(Long userId, String newUsername, String newPassword) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        try {
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            boolean updated = false;

            // Обновление имени пользователя, если предоставлено
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                validateUsername(newUsername);

                // Проверяем, не занято ли новое имя другим пользователем
                User existingUser = userRepository.findByUsername(newUsername);
                if (existingUser != null && !existingUser.getId().equals(userId)) {
                    throw new IllegalArgumentException("Username '" + newUsername + "' is already taken");
                }

                user.setUsername(newUsername);
                updated = true;
            }

            // Обновление пароля, если предоставлен
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                validatePassword(newPassword);
                String newPasswordHash = hashPassword(newPassword);
                user.setPasswordHash(newPasswordHash);
                updated = true;
            }

            if (updated) {
                boolean success = userRepository.updateUser(user);
                if (success) {
                    User updatedUser = userRepository.findById(userId);
                    logger.info("User updated successfully: " + userId);
                    return updatedUser;
                } else {
                    throw new RuntimeException("Failed to update user in database");
                }
            } else {
                logger.info("No changes provided for user update: " + userId);
                return user;
            }

        } catch (Exception e) {
            logger.severe("Error updating user: " + e.getMessage());
            throw new RuntimeException("User update failed", e);
        }
    }

    // UPDATE - обновление только имени пользователя
    public User updateUsername(Long userId, String newUsername) {
        return updateUser(userId, newUsername, null);
    }

    // UPDATE - обновление пароля с подтверждением
    public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        validatePassword(newPassword);

        try {
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            // Проверяем текущий пароль
            String currentPasswordHash = hashPassword(currentPassword);
            if (!user.getPasswordHash().equals(currentPasswordHash)) {
                throw new IllegalArgumentException("Current password is incorrect");
            }

            String newPasswordHash = hashPassword(newPassword);
            user.setPasswordHash(newPasswordHash);
            boolean success = userRepository.updateUser(user);

            if (success) {
                logger.info("Password updated successfully for user: " + userId);
            }
            return success;

        } catch (Exception e) {
            logger.severe("Error updating password: " + e.getMessage());
            throw new RuntimeException("Password update failed", e);
        }
    }

    // UPDATE - обновление профиля (альтернативный метод)
    public User updateUserProfile(User updatedUser) {
        if (updatedUser == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        validateUser(updatedUser);

        try {
            // Проверяем существование пользователя
            User existingUser = userRepository.findById(updatedUser.getId());
            if (existingUser == null) {
                throw new IllegalArgumentException("User not found with ID: " + updatedUser.getId());
            }

            // Проверяем уникальность имени пользователя
            User userWithSameUsername = userRepository.findByUsername(updatedUser.getUsername());
            if (userWithSameUsername != null && !userWithSameUsername.getId().equals(updatedUser.getId())) {
                throw new IllegalArgumentException("Username '" + updatedUser.getUsername() + "' is already taken");
            }

            boolean success = userRepository.updateUser(updatedUser);
            if (success) {
                User resultUser = userRepository.findById(updatedUser.getId());
                logger.info("User profile updated successfully: " + updatedUser.getId());
                return resultUser;
            } else {
                throw new RuntimeException("Failed to update user profile");
            }

        } catch (Exception e) {
            logger.severe("Error updating user profile: " + e.getMessage());
            throw new RuntimeException("Profile update failed", e);
        }
    }

    // DELETE - удаление пользователя
    public boolean deleteUser(Long userId, String confirmationPassword) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        try {
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            // Подтверждение паролем для удаления
            String inputPasswordHash = hashPassword(confirmationPassword);
            if (!user.getPasswordHash().equals(inputPasswordHash)) {
                throw new IllegalArgumentException("Confirmation password is incorrect");
            }

            boolean success = userRepository.deleteUser(userId);

            if (success) {
                logger.info("User deleted successfully: " + userId);
            }
            return success;

        } catch (Exception e) {
            logger.severe("Error deleting user: " + e.getMessage());
            throw new RuntimeException("User deletion failed", e);
        }
    }

    // DELETE - принудительное удаление (без подтверждения пароля)
    public boolean forceDeleteUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        try {
            boolean success = userRepository.deleteUser(userId);
            if (success) {
                logger.warning("User force deleted: " + userId);
            }
            return success;
        } catch (Exception e) {
            logger.severe("Error force deleting user: " + e.getMessage());
            throw new RuntimeException("Force user deletion failed", e);
        }
    }

    // Валидация
    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        validateUsername(user.getUsername());
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers and underscores");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }

    // Упрощенное хэширование пароля (в реальном приложении используйте BCrypt)
    private String hashPassword(String password) {
        // Временная реализация - замените на BCrypt
        return Integer.toString(password.hashCode());
    }

    // Статистика
    public int getUserCount() {
        try {
            return userRepository.findAll().size();
        } catch (Exception e) {
            logger.severe("Error getting user count: " + e.getMessage());
            return 0;
        }
    }

    // Проверка существования пользователя
    public boolean userExists(String username) {
        try {
            return userRepository.findByUsername(username) != null;
        } catch (Exception e) {
            logger.severe("Error checking user existence: " + e.getMessage());
            return false;
        }
    }

    public boolean userExists(Long userId) {
        try {
            return userRepository.findById(userId) != null;
        } catch (Exception e) {
            logger.severe("Error checking user existence: " + e.getMessage());
            return false;
        }
    }

    // Поиск пользователей по шаблону
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }

        try {
            List<User> allUsers = userRepository.findAll();
            return allUsers.stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
        } catch (Exception e) {
            logger.severe("Error searching users: " + e.getMessage());
            throw new RuntimeException("User search failed", e);
        }
    }
}