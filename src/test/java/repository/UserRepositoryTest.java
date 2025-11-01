package repository;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends BaseRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
    }

    @Test
    void testCreateUser_ShouldCreateNewUser() throws SQLException {
        // Подготовка
        User user = new User("testuser", "hashed_password_123");

        // Действие
        Long userId = userRepository.createUser(user);

        // Проверка
        assertNotNull(userId, "ID пользователя не должен быть null");
        assertTrue(userId > 0, "ID пользователя должен быть положительным числом");

        Optional<User> foundUser = userRepository.findById(userId);
        assertTrue(foundUser.isPresent(), "Пользователь должен быть найден в базе");
        assertEquals("testuser", foundUser.get().getName(), "Имя пользователя должно совпадать");
        assertEquals("hashed_password_123", foundUser.get().getPasswordHash(), "Хэш пароля должен совпадать");
    }

    @Test
    void testFindById_ShouldReturnUser_WhenUserExists() throws SQLException {
        // Подготовка
        User user = new User("findbyid_user", "password123");
        Long userId = userRepository.createUser(user);

        // Действие
        Optional<User> foundUser = userRepository.findById(userId);

        // Проверка
        assertTrue(foundUser.isPresent(), "Пользователь должен быть найден");
        assertEquals(userId, foundUser.get().getId(), "ID пользователя должен совпадать");
        assertEquals("findbyid_user", foundUser.get().getName(), "Имя пользователя должно совпадать");
    }

    @Test
    void testFindById_ShouldReturnEmpty_WhenUserNotExists() throws SQLException {
        // Действие
        Optional<User> foundUser = userRepository.findById(999999L);

        // Проверка
        assertFalse(foundUser.isPresent(), "Пользователь не должен быть найден");
    }

    @Test
    void testFindByName_ShouldReturnUser_WhenUserExists() throws SQLException {
        // Подготовка
        User user = new User("findbyname_user", "password456");
        userRepository.createUser(user);

        // Действие
        Optional<User> foundUser = userRepository.findByName("findbyname_user");

        // Проверка
        assertTrue(foundUser.isPresent(), "Пользователь должен быть найден по имени");
        assertEquals("findbyname_user", foundUser.get().getName(), "Имя пользователя должно совпадать");
    }

    @Test
    void testFindByName_ShouldReturnEmpty_WhenUserNotExists() throws SQLException {
        // Действие
        Optional<User> foundUser = userRepository.findByName("nonexistent_user");

        // Проверка
        assertFalse(foundUser.isPresent(), "Пользователь не должен быть найден");
    }

    @Test
    void testUpdateUser_ShouldUpdateUserData() throws SQLException {
        // Подготовка
        User user = new User("updateuser", "old_password");
        Long userId = userRepository.createUser(user);
        user.setId(userId);

        // Действие
        user.setName("updated_user_name");
        user.setPasswordHash("new_secure_password");
        boolean updated = userRepository.updateUser(user);

        // Проверка
        assertTrue(updated, "Обновление должно быть успешным");

        Optional<User> foundUser = userRepository.findById(userId);
        assertTrue(foundUser.isPresent(), "Пользователь должен существовать после обновления");
        assertEquals("updated_user_name", foundUser.get().getName(), "Имя должно быть обновлено");
        assertEquals("new_secure_password", foundUser.get().getPasswordHash(), "Пароль должен быть обновлен");
    }

    @Test
    void testUpdateUser_ShouldReturnFalse_WhenUserNotExists() throws SQLException {
        // Подготовка
        User nonExistentUser = new User("nonexistent", "password");
        nonExistentUser.setId(999999L);

        // Действие
        boolean updated = userRepository.updateUser(nonExistentUser);

        // Проверка
        assertFalse(updated, "Обновление несуществующего пользователя должно вернуть false");
    }

    @Test
    void testAuthenticateUser_ShouldReturnTrue_WhenCredentialsCorrect() throws SQLException {
        // Подготовка
        User user = new User("authuser", "correct_password_hash");
        userRepository.createUser(user);

        // Действие
        boolean authenticated = userRepository.authenticateUser("authuser", "correct_password_hash");

        // Проверка
        assertTrue(authenticated, "Аутентификация должна быть успешной при правильных credentials");
    }

    @Test
    void testAuthenticateUser_ShouldReturnFalse_WhenWrongPassword() throws SQLException {
        // Подготовка
        User user = new User("authuser", "correct_hash");
        userRepository.createUser(user);

        // Действие
        boolean authenticated = userRepository.authenticateUser("authuser", "wrong_hash");

        // Проверка
        assertFalse(authenticated, "Аутентификация должна провалиться при неправильном пароле");
    }

    @Test
    void testAuthenticateUser_ShouldReturnFalse_WhenUserNotExists() throws SQLException {
        // Действие
        boolean authenticated = userRepository.authenticateUser("nonexistent", "any_password");

        // Проверка
        assertFalse(authenticated, "Аутентификация должна провалиться для несуществующего пользователя");
    }

    @Test
    void testUserNameExists_ShouldReturnTrue_WhenUserNameExists() throws SQLException {
        // Подготовка
        User user = new User("existinguser", "password");
        userRepository.createUser(user);

        // Действие & Проверка
        assertTrue(userRepository.userNameExists("existinguser"),
                "Метод должен вернуть true для существующего имени пользователя");
    }

    @Test
    void testUserNameExists_ShouldReturnFalse_WhenUserNameNotExists() throws SQLException {
        // Действие & Проверка
        assertFalse(userRepository.userNameExists("nonexistinguser"),
                "Метод должен вернуть false для несуществующего имени пользователя");
    }

    @Test
    void testDeleteUser_ShouldDeleteUser() throws SQLException {
        // Подготовка
        User user = new User("deleteuser", "password");
        Long userId = userRepository.createUser(user);

        // Проверка что пользователь создан
        assertTrue(userRepository.findById(userId).isPresent(), "Пользователь должен существовать до удаления");

        // Действие
        boolean deleted = userRepository.deleteUser(userId);

        // Проверка
        assertTrue(deleted, "Удаление должно быть успешным");
        assertFalse(userRepository.findById(userId).isPresent(), "Пользователь не должен существовать после удаления");
    }

    @Test
    void testDeleteUser_ShouldReturnFalse_WhenUserNotExists() throws SQLException {
        // Действие
        boolean deleted = userRepository.deleteUser(999999L);

        // Проверка
        assertFalse(deleted, "Удаление несуществующего пользователя должно вернуть false");
    }

    @Test
    void testCreateUser_ShouldThrowException_WhenDuplicateUserName() throws SQLException {
        // Подготовка
        User user1 = new User("duplicate_user", "pass1");
        userRepository.createUser(user1);

        User user2 = new User("duplicate_user", "pass2");

        // Действие & Проверка
        SQLException exception = assertThrows(SQLException.class, () -> {
            userRepository.createUser(user2);
        }, "Должно быть выброшено исключение при создании пользователя с дублирующимся именем");

        assertTrue(exception.getMessage().toLowerCase().contains("unique") ||
                        exception.getMessage().toLowerCase().contains("duplicate"),
                "Сообщение об ошибке должно указывать на нарушение уникальности");
    }
}