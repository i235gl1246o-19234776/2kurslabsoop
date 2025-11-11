package repository;

import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.dao.UserRepository;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends BaseRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        DatabaseConnection dbt = new DatabaseTestConnection();
        userRepository = new UserRepository(dbt);
    }

    @Test
    void testCreateUser_ShouldCreateNewUser() throws SQLException {
        User user = new User("testuser", "hashed_password_123");

        Long userId = userRepository.createUser(user);

        assertNotNull(userId, "ID пользователя не должен быть null");
        assertTrue(userId > 0, "ID пользователя должен быть положительным числом");

        Optional<User> foundUser = userRepository.findById(userId);
        assertTrue(foundUser.isPresent(), "Пользователь должен быть найден в базе");
        assertEquals("testuser", foundUser.get().getName(), "Имя пользователя должно совпадать");
        assertEquals("hashed_password_123", foundUser.get().getPasswordHash(), "Хэш пароля должен совпадать");
    }

    @Test
    void testFindById_ShouldReturnUser_WhenUserExists() throws SQLException {
        User user = new User("findbyid_user", "password123");
        Long userId = userRepository.createUser(user);

        Optional<User> foundUser = userRepository.findById(userId);

        assertTrue(foundUser.isPresent(), "Пользователь должен быть найден");
        assertEquals(userId, foundUser.get().getId(), "ID пользователя должен совпадать");
        assertEquals("findbyid_user", foundUser.get().getName(), "Имя пользователя должно совпадать");
    }

    @Test
    void testFindById_ShouldReturnEmpty_WhenUserNotExists() throws SQLException {
        Optional<User> foundUser = userRepository.findById(999999L);

        assertFalse(foundUser.isPresent(), "Пользователь не должен быть найден");
    }

    @Test
    void testFindByName_ShouldReturnUser_WhenUserExists() throws SQLException {
        User user = new User("findbyname_user", "password456");
        userRepository.createUser(user);

        Optional<User> foundUser = userRepository.findByName("findbyname_user");

        assertTrue(foundUser.isPresent(), "Пользователь должен быть найден по имени");
        assertEquals("findbyname_user", foundUser.get().getName(), "Имя пользователя должно совпадать");
    }

    @Test
    void testFindByName_ShouldReturnEmpty_WhenUserNotExists() throws SQLException {
        Optional<User> foundUser = userRepository.findByName("nonexistent_user");

        assertFalse(foundUser.isPresent(), "Пользователь не должен быть найден");
    }

    @Test
    void testUpdateUser_ShouldUpdateUserData() throws SQLException {
        User user = new User("updateuser", "old_password");
        Long userId = userRepository.createUser(user);
        user.setId(userId);

        user.setName("updated_user_name");
        user.setPasswordHash("new_secure_password");
        boolean updated = userRepository.updateUser(user);

        assertTrue(updated, "Обновление должно быть успешным");

        Optional<User> foundUser = userRepository.findById(userId);
        assertTrue(foundUser.isPresent(), "Пользователь должен существовать после обновления");
        assertEquals("updated_user_name", foundUser.get().getName(), "Имя должно быть обновлено");
        assertEquals("new_secure_password", foundUser.get().getPasswordHash(), "Пароль должен быть обновлен");
    }

    @Test
    void testUpdateUser_ShouldReturnFalse_WhenUserNotExists() throws SQLException {
        User nonExistentUser = new User("nonexistent", "password");
        nonExistentUser.setId(999999L);

        boolean updated = userRepository.updateUser(nonExistentUser);

        assertFalse(updated, "Обновление несуществующего пользователя должно вернуть false");
    }

    @Test
    void testAuthenticateUser_ShouldReturnTrue_WhenCredentialsCorrect() throws SQLException {
        User user = new User("authuser", "correct_password_hash");
        userRepository.createUser(user);

        boolean authenticated = userRepository.authenticateUser("authuser", "correct_password_hash");

        assertTrue(authenticated, "Аутентификация должна быть успешной при правильных credentials");
    }

    @Test
    void testAuthenticateUser_ShouldReturnFalse_WhenWrongPassword() throws SQLException {
        User user = new User("authuser", "correct_hash");
        userRepository.createUser(user);

        boolean authenticated = userRepository.authenticateUser("authuser", "wrong_hash");

        assertFalse(authenticated, "Аутентификация должна провалиться при неправильном пароле");
    }

    @Test
    void testAuthenticateUser_ShouldReturnFalse_WhenUserNotExists() throws SQLException {
        boolean authenticated = userRepository.authenticateUser("nonexistent", "any_password");

        assertFalse(authenticated, "Аутентификация должна провалиться для несуществующего пользователя");
    }

    @Test
    void testUserNameExists_ShouldReturnTrue_WhenUserNameExists() throws SQLException {
        User user = new User("existinguser", "password");
        userRepository.createUser(user);

        assertTrue(userRepository.userNameExists("existinguser"),
                "Метод должен вернуть true для существующего имени пользователя");
    }

    @Test
    void testUserNameExists_ShouldReturnFalse_WhenUserNameNotExists() throws SQLException {
        assertFalse(userRepository.userNameExists("nonexistinguser"),
                "Метод должен вернуть false для несуществующего имени пользователя");
    }

    @Test
    void testDeleteUser_ShouldDeleteUser() throws SQLException {
        User user = new User("deleteuser", "password");
        Long userId = userRepository.createUser(user);

        assertTrue(userRepository.findById(userId).isPresent(), "Пользователь должен существовать до удаления");

        boolean deleted = userRepository.deleteUser(userId);

        assertTrue(deleted, "Удаление должно быть успешным");
        assertFalse(userRepository.findById(userId).isPresent(), "Пользователь не должен существовать после удаления");
    }

    @Test
    void testDeleteUser_ShouldReturnFalse_WhenUserNotExists() throws SQLException {
        boolean deleted = userRepository.deleteUser(999999L);

        assertFalse(deleted, "Удаление несуществующего пользователя должно вернуть false");
    }

    @Test
    void testCreateUser_ShouldThrowException_WhenDuplicateUserName() throws SQLException {
        User user1 = new User("duplicate_user", "pass1");
        userRepository.createUser(user1);

        User user2 = new User("duplicate_user", "pass2");

        SQLException exception = assertThrows(SQLException.class, () -> {
            userRepository.createUser(user2);
        }, "Должно быть выброшено исключение при создании пользователя с дублирующимся именем");

        assertTrue(exception.getMessage().toLowerCase().contains("unique") ||
                        exception.getMessage().toLowerCase().contains("duplicate"),
                "Сообщение об ошибке должно указывать на нарушение уникальности");
    }
}