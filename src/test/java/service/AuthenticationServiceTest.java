package service;

import model.entity.User;
import repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(userRepository);
    }

    @Test
    void hashPassword_ShouldReturnHashedPassword() {

        String plainPassword = "testPassword123";

        String hashedPassword = authenticationService.hashPassword(plainPassword);

        assertNotNull(hashedPassword);
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
    }

    @Test
    void hashPassword_DifferentPasswords_ShouldProduceDifferentHashes() {

        String password1 = "password1";
        String password2 = "password2";

        String hash1 = authenticationService.hashPassword(password1);
        String hash2 = authenticationService.hashPassword(password2);

        assertNotEquals(hash1, hash2);
    }

    @Test
    void authenticateUser_ValidCredentials_ShouldReturnTrue() throws SQLException {
        String username = "testUser";
        String password = "correctPassword";
        String hashedPassword = authenticationService.hashPassword(password);

        User user = new User();
        user.setName(username);
        user.setPasswordHash(hashedPassword);

        when(userRepository.findByName(username)).thenReturn(Optional.of(user));

        boolean result = authenticationService.authenticateUser(username, password);

        assertTrue(result);
        verify(userRepository, times(1)).findByName(username);
    }

    @Test
    void authenticateUser_InvalidPassword_ShouldReturnFalse() throws SQLException {

        String username = "testUser";
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashedPassword = authenticationService.hashPassword(correctPassword);

        User user = new User();
        user.setName(username);
        user.setPasswordHash(hashedPassword);

        when(userRepository.findByName(username)).thenReturn(Optional.of(user));

        boolean result = authenticationService.authenticateUser(username, wrongPassword);

        assertFalse(result);
        verify(userRepository, times(1)).findByName(username);
    }

    @Test
    void authenticateUser_UserNotFound_ShouldReturnFalse() throws SQLException {
        String username = "nonExistentUser";
        String password = "anyPassword";

        when(userRepository.findByName(username)).thenReturn(Optional.empty());

        boolean result = authenticationService.authenticateUser(username, password);

        assertFalse(result);
        verify(userRepository, times(1)).findByName(username);
    }

    @Test
    void authenticateUser_RepositoryThrowsException_ShouldPropagateException() throws SQLException {
        String username = "testUser";
        String password = "testPassword";

        when(userRepository.findByName(username)).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () ->
                authenticationService.authenticateUser(username, password)
        );
    }

    @Test
    void registerUser_ValidData_ShouldCreateUser() throws SQLException {
        String username = "newUser";
        String password = "newPassword";

        authenticationService.registerUser(username, password);

        verify(userRepository, times(1)).createUser(any(User.class));
    }

    @Test
    void registerUser_ShouldHashPasswordBeforeStoring() throws SQLException {
        String username = "newUser";
        String plainPassword = "newPassword";

        authenticationService.registerUser(username, plainPassword);

        verify(userRepository).createUser(argThat(user -> {
            assertNotNull(user.getPasswordHash());
            assertNotEquals(plainPassword, user.getPasswordHash());
            assertTrue(user.getPasswordHash().startsWith("$2a$"));
            assertEquals(username, user.getName());
            return true;
        }));
    }

    @Test
    void registerUser_RepositoryThrowsException_ShouldPropagateException() throws SQLException {
        String username = "newUser";
        String password = "newPassword";

        doThrow(new SQLException("Database error")).when(userRepository).createUser(any(User.class));

        assertThrows(SQLException.class, () ->
                authenticationService.registerUser(username, password)
        );
    }

    @Test
    void authenticateUser_WithNullPassword_ShouldReturnFalse() throws SQLException {
        String username = "testUser";
        String hashedPassword = authenticationService.hashPassword("somePassword");

        User user = new User();
        user.setName(username);
        user.setPasswordHash(hashedPassword);

        when(userRepository.findByName(username)).thenReturn(Optional.of(user));

        boolean result = authenticationService.authenticateUser(username, null);

        assertFalse(result);
    }

    @Test
    void authenticateUser_WithEmptyPassword_ShouldReturnFalse() throws SQLException {
        String username = "testUser";
        String hashedPassword = authenticationService.hashPassword("somePassword");

        User user = new User();
        user.setName(username);
        user.setPasswordHash(hashedPassword);

        when(userRepository.findByName(username)).thenReturn(Optional.of(user));

        boolean result = authenticationService.authenticateUser(username, "");

        assertFalse(result);
    }
}