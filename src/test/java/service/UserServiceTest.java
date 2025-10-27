package service;

import model.User;
import repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);

        testUser = new User("testuser", "hashedpassword123");
        testUser.setId(1L);

        anotherUser = new User("anotheruser", "hashedpassword456");
        anotherUser.setId(2L);
    }

    // CREATE tests
    @Test
    void registerUser_Success() throws SQLException {
        // Arrange
        String username = "newuser";
        String password = "password123";
        String hashedPassword = Integer.toString(password.hashCode());

        User newUser = new User(username, hashedPassword);
        newUser.setId(1L);

        when(userRepository.findByUsername(username)).thenReturn(null);
        when(userRepository.createUser(any(User.class))).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(newUser);

        // Act
        User result = userService.registerUser(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(hashedPassword, result.getPasswordHash());
        verify(userRepository).createUser(any(User.class));
    }

    @Test
    void registerUser_UsernameAlreadyExists() throws SQLException {
        // Arrange
        String username = "existinguser";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(testUser);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(username, password));

        assertEquals("User with username 'existinguser' already exists", exception.getMessage());
        verify(userRepository, never()).createUser(any(User.class));
    }

    @Test
    void registerUser_InvalidUsername() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser("", "password123"));
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser("ab", "password123"));
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser("user@name", "password123"));
    }

    @Test
    void registerUser_InvalidPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser("validuser", ""));
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser("validuser", "123"));
    }

    // AUTHENTICATION tests
    @Test
    void authenticate_Success() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String hashedPassword = Integer.toString(password.hashCode());

        testUser.setPasswordHash(hashedPassword);
        when(userRepository.findByUsername(username)).thenReturn(testUser);

        // Act
        User result = userService.authenticate(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void authenticate_UserNotFound() throws SQLException {
        // Arrange
        String username = "nonexistent";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(null);

        // Act
        User result = userService.authenticate(username, password);

        // Assert
        assertNull(result);
    }

    @Test
    void authenticate_WrongPassword() throws SQLException {
        // Arrange
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";

        testUser.setPasswordHash(Integer.toString(correctPassword.hashCode()));
        when(userRepository.findByUsername(username)).thenReturn(testUser);

        // Act
        User result = userService.authenticate(username, wrongPassword);

        // Assert
        assertNull(result);
    }

    // READ tests
    @Test
    void getUserById_Success() throws SQLException {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(testUser);

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_NotFound() throws SQLException {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(null);

        // Act
        User result = userService.getUserById(999L);

        // Assert
        assertNull(result);
    }

    @Test
    void getUserById_InvalidId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(null));
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(0L));
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(-1L));
    }

    @Test
    void getUserByUsername_Success() throws SQLException {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // Act
        User result = userService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getAllUsers_Success() throws SQLException {
        // Arrange
        List<User> users = Arrays.asList(testUser, anotherUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // UPDATE tests
    @Test
    void updateUser_Success() throws SQLException {
        // Arrange
        Long userId = 1L;
        String newUsername = "newusername";
        String newPassword = "newpassword123";
        String newHashedPassword = Integer.toString(newPassword.hashCode());

        User updatedUser = new User(newUsername, newHashedPassword);
        updatedUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(testUser);
        when(userRepository.findByUsername(newUsername)).thenReturn(null);
        when(userRepository.updateUser(any(User.class))).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(updatedUser);

        // Act
        User result = userService.updateUser(userId, newUsername, newPassword);

        // Assert
        assertNotNull(result);
        assertEquals(newUsername, result.getUsername());
        assertEquals(newHashedPassword, result.getPasswordHash());
        verify(userRepository).updateUser(any(User.class));
    }

    @Test
    void updateUser_OnlyUsername() throws SQLException {
        // Arrange
        Long userId = 1L;
        String newUsername = "newusername";
        String originalPassword = testUser.getPasswordHash();

        when(userRepository.findById(userId)).thenReturn(testUser);
        when(userRepository.findByUsername(newUsername)).thenReturn(null);
        when(userRepository.updateUser(any(User.class))).thenReturn(true);
        when(userRepository.findById(userId)).thenAnswer(invocation -> {
            User user = new User(newUsername, originalPassword);
            user.setId(userId);
            return user;
        });

        // Act
        User result = userService.updateUser(userId, newUsername, null);

        // Assert
        assertNotNull(result);
        assertEquals(newUsername, result.getUsername());
        assertEquals(originalPassword, result.getPasswordHash());
    }

    @Test
    void updateUser_OnlyPassword() throws SQLException {
        // Arrange
        Long userId = 1L;
        String newPassword = "newpassword123";
        String newHashedPassword = Integer.toString(newPassword.hashCode());
        String originalUsername = testUser.getUsername();

        when(userRepository.findById(userId)).thenReturn(testUser);
        when(userRepository.updateUser(any(User.class))).thenReturn(true);
        when(userRepository.findById(userId)).thenAnswer(invocation -> {
            User user = new User(originalUsername, newHashedPassword);
            user.setId(userId);
            return user;
        });

        // Act
        User result = userService.updateUser(userId, null, newPassword);

        // Assert
        assertNotNull(result);
        assertEquals(originalUsername, result.getUsername());
        assertEquals(newHashedPassword, result.getPasswordHash());
    }

    @Test
    void updateUser_UsernameAlreadyTaken() throws SQLException {
        // Arrange
        Long userId = 1L;
        String takenUsername = "takenusername";

        when(userRepository.findById(userId)).thenReturn(testUser);
        when(userRepository.findByUsername(takenUsername)).thenReturn(anotherUser);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(userId, takenUsername, null));

        assertEquals("Username 'takenusername' is already taken", exception.getMessage());
        verify(userRepository, never()).updateUser(any(User.class));
    }

    @Test
    void updateUser_UserNotFound() throws SQLException {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(999L, "newname", "newpass"));

        assertEquals("User update failed", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("User not found with ID: 999", exception.getCause().getMessage());
    }

    @Test
    void updateUsername_Success() throws SQLException {
        // Arrange
        Long userId = 1L;
        String newUsername = "newusername";

        when(userRepository.findById(userId)).thenReturn(testUser);
        when(userRepository.findByUsername(newUsername)).thenReturn(null);
        when(userRepository.updateUser(any(User.class))).thenReturn(true);
        when(userRepository.findById(userId)).thenAnswer(invocation -> {
            User user = new User(newUsername, testUser.getPasswordHash());
            user.setId(userId);
            return user;
        });

        // Act
        User result = userService.updateUsername(userId, newUsername);

        // Assert
        assertNotNull(result);
        assertEquals(newUsername, result.getUsername());
    }

    @Test
    void updatePassword_Success() throws SQLException {
        // Arrange
        Long userId = 1L;
        String currentPassword = "currentpass";
        String newPassword = "newpass123";

        String currentHashed = Integer.toString(currentPassword.hashCode());
        String newHashed = Integer.toString(newPassword.hashCode());

        testUser.setPasswordHash(currentHashed);

        when(userRepository.findById(userId)).thenReturn(testUser);
        when(userRepository.updateUser(any(User.class))).thenReturn(true);

        // Act
        boolean result = userService.updatePassword(userId, currentPassword, newPassword);

        // Assert
        assertTrue(result);
        verify(userRepository).updateUser(any(User.class));
    }

    @Test
    void updatePassword_WrongCurrentPassword() throws SQLException {
        // Arrange
        Long userId = 1L;
        String currentPassword = "wrongpass";
        String newPassword = "newpass123";

        testUser.setPasswordHash(Integer.toString("correctpass".hashCode()));

        when(userRepository.findById(userId)).thenReturn(testUser);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updatePassword(userId, currentPassword, newPassword));

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository, never()).updateUser(any(User.class));
    }

    @Test
    void updateUserProfile_Success() throws SQLException {
        // Arrange
        User updatedUser = new User("newusername", "newhashedpass");
        updatedUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(testUser);
        when(userRepository.findByUsername("newusername")).thenReturn(null);
        when(userRepository.updateUser(updatedUser)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(updatedUser);

        // Act
        User result = userService.updateUserProfile(updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals("newusername", result.getUsername());
    }

    // DELETE tests
    @Test
    void deleteUser_Success() throws SQLException {
        // Arrange
        Long userId = 1L;
        String confirmationPassword = "password123";
        String hashedPassword = Integer.toString(confirmationPassword.hashCode());

        testUser.setPasswordHash(hashedPassword);

        when(userRepository.findById(userId)).thenReturn(testUser);
        when(userRepository.deleteUser(userId)).thenReturn(true);

        // Act
        boolean result = userService.deleteUser(userId, confirmationPassword);

        // Assert
        assertTrue(result);
        verify(userRepository).deleteUser(userId);
    }

    @Test
    void deleteUser_WrongConfirmationPassword() throws SQLException {
        // Arrange
        Long userId = 1L;
        String wrongPassword = "wrongpass";

        testUser.setPasswordHash(Integer.toString("correctpass".hashCode()));

        when(userRepository.findById(userId)).thenReturn(testUser);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(userId, wrongPassword));

        // Проверяем, что это именно то исключение, которое мы ожидаем
        assertEquals("User deletion failed", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Confirmation password is incorrect", exception.getCause().getMessage());

        verify(userRepository, never()).deleteUser(userId);
    }

    @Test
    void forceDeleteUser_Success() throws SQLException {
        // Arrange
        Long userId = 1L;
        when(userRepository.deleteUser(userId)).thenReturn(true);

        // Act
        boolean result = userService.forceDeleteUser(userId);

        // Assert
        assertTrue(result);
        verify(userRepository).deleteUser(userId);
    }

    // UTILITY methods tests
    @Test
    void getUserCount_Success() throws SQLException {
        // Arrange
        List<User> users = Arrays.asList(testUser, anotherUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        int count = userService.getUserCount();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void getUserCount_Exception() throws SQLException {
        // Arrange
        when(userRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        // Act
        int count = userService.getUserCount();

        // Assert
        assertEquals(0, count);
    }

    @Test
    void userExists_ByUsername_True() throws SQLException {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // Act
        boolean exists = userService.userExists("testuser");

        // Assert
        assertTrue(exists);
    }

    @Test
    void userExists_ByUsername_False() throws SQLException {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        // Act
        boolean exists = userService.userExists("nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void userExists_ById_True() throws SQLException {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(testUser);

        // Act
        boolean exists = userService.userExists(1L);

        // Assert
        assertTrue(exists);
    }

    @Test
    void searchUsers_Success() throws SQLException {
        // Arrange
        List<User> allUsers = Arrays.asList(
                new User("john_doe", "hash1"),
                new User("jane_smith", "hash2"),
                new User("bob_johnson", "hash3")
        );

        when(userRepository.findAll()).thenReturn(allUsers);

        // Act
        List<User> result = userService.searchUsers("john");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getUsername().equals("john_doe")));
        assertTrue(result.stream().anyMatch(u -> u.getUsername().equals("bob_johnson")));
    }

    @Test
    void searchUsers_EmptySearchTerm() throws SQLException {
        // Arrange
        List<User> allUsers = Arrays.asList(testUser, anotherUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        // Act
        List<User> result = userService.searchUsers("");

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void searchUsers_NullSearchTerm() throws SQLException {
        // Arrange
        List<User> allUsers = Arrays.asList(testUser, anotherUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        // Act
        List<User> result = userService.searchUsers(null);

        // Assert
        assertEquals(2, result.size());
    }

    // VALIDATION tests
    @Test
    void validateUsername_VariousCases() {
        // Valid usernames
        assertDoesNotThrow(() -> userService.getUserByUsername("valid"));
        assertDoesNotThrow(() -> userService.getUserByUsername("valid_user"));
        assertDoesNotThrow(() -> userService.getUserByUsername("user123"));

        // Invalid usernames
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByUsername(""));
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByUsername("ab"));
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByUsername("user@name"));
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByUsername("user-name"));
    }

    @Test
    void validatePassword_VariousCases() {
        // Valid passwords
        assertDoesNotThrow(() -> userService.authenticate("testuser", "123456"));
        assertDoesNotThrow(() -> userService.authenticate("testuser", "longpassword"));

        // Invalid passwords
        assertThrows(IllegalArgumentException.class, () -> userService.authenticate("testuser", ""));
        assertThrows(IllegalArgumentException.class, () -> userService.authenticate("testuser", "123"));
    }
}