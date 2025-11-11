package service;

import model.entity.User;
import model.dto.request.CreateUserRequest;
import model.dto.response.UserResponseDTO;
import model.dto.DTOTransformService;
import repository.dao.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DTOTransformService dtoTransformService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, dtoTransformService);
    }

    @Test
    @DisplayName("Should create user successfully")
    void createUser_Success() throws SQLException {
        CreateUserRequest request = new CreateUserRequest("john_doe", "password123");
        User userEntity = createUser(1L, "john_doe", "hashed_password");
        UserResponseDTO responseDTO = createUserResponseDTO(1L, "john_doe");

        when(userRepository.userNameExists("john_doe")).thenReturn(false);
        when(dtoTransformService.toEntity(request)).thenReturn(userEntity);
        when(userRepository.createUser(userEntity)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(dtoTransformService.toResponseDTO(userEntity)).thenReturn(responseDTO);

        UserResponseDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john_doe", result.getName());

        verify(userRepository).userNameExists("john_doe");
        verify(dtoTransformService).toEntity(request);
        verify(userRepository).createUser(userEntity);
        verify(userRepository).findById(1L);
        verify(dtoTransformService).toResponseDTO(userEntity);
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void createUser_UsernameExists() throws SQLException {
        CreateUserRequest request = new CreateUserRequest("existing_user", "password123");
        when(userRepository.userNameExists("existing_user")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userService.createUser(request)
        );
        assertTrue(exception.getMessage().contains("Пользователь с именем 'existing_user' уже существует"));

        verify(userRepository).userNameExists("existing_user");
        verify(userRepository, never()).createUser(any());
    }

    @Test
    @DisplayName("Should throw exception when created user not found")
    void createUser_UserNotFoundAfterCreation() throws SQLException {
        CreateUserRequest request = new CreateUserRequest("john_doe", "password123");
        User userEntity = createUser(null, "john_doe", "hashed_password");

        when(userRepository.userNameExists("john_doe")).thenReturn(false);
        when(dtoTransformService.toEntity(request)).thenReturn(userEntity);
        when(userRepository.createUser(userEntity)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        SQLException exception = assertThrows(SQLException.class, () ->
                userService.createUser(request)
        );
        assertTrue(exception.getMessage().contains("Не удалось получить созданного пользователя с ID: 1"));

        verify(userRepository).userNameExists("john_doe");
        verify(dtoTransformService).toEntity(request);
        verify(userRepository).createUser(userEntity);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void getUserById_Success() throws SQLException {
        Long userId = 1L;
        User userEntity = createUser(userId, "john_doe", "hashed_password");
        UserResponseDTO responseDTO = createUserResponseDTO(userId, "john_doe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(dtoTransformService.toResponseDTO(userEntity)).thenReturn(responseDTO);

        Optional<UserResponseDTO> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("john_doe", result.get().getName());

        verify(userRepository).findById(userId);
        verify(dtoTransformService).toResponseDTO(userEntity);
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void getUserById_NotFound() throws SQLException {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<UserResponseDTO> result = userService.getUserById(userId);

        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should get user by name successfully")
    void getUserByName_Success() throws SQLException {
        String username = "john_doe";
        User userEntity = createUser(1L, username, "hashed_password");
        UserResponseDTO responseDTO = createUserResponseDTO(1L, username);

        when(userRepository.findByName(username)).thenReturn(Optional.of(userEntity));
        when(dtoTransformService.toResponseDTO(userEntity)).thenReturn(responseDTO);

        Optional<UserResponseDTO> result = userService.getUserByName(username);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getName());
        verify(userRepository).findByName(username);
        verify(dtoTransformService).toResponseDTO(userEntity);
    }

    @Test
    @DisplayName("Should return empty when user not found by name")
    void getUserByName_NotFound() throws SQLException {
        String username = "non_existent";
        when(userRepository.findByName(username)).thenReturn(Optional.empty());

        Optional<UserResponseDTO> result = userService.getUserByName(username);

        assertFalse(result.isPresent());
        verify(userRepository).findByName(username);
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_Success() throws SQLException {
        User user = createUser(1L, "updated_user", "new_password_hash");
        when(userRepository.updateUser(user)).thenReturn(true);

        boolean result = userService.updateUser(user);

        assertTrue(result);
        verify(userRepository).updateUser(user);
    }

    @Test
    @DisplayName("Should return false when update fails")
    void updateUser_Failure() throws SQLException {
        User user = createUser(1L, "user", "password_hash");
        when(userRepository.updateUser(user)).thenReturn(false);

        boolean result = userService.updateUser(user);

        assertFalse(result);
        verify(userRepository).updateUser(user);
    }

    @Test
    @DisplayName("Should authenticate user successfully")
    void authenticateUser_Success() throws SQLException {
        String username = "john_doe";
        String passwordHash = "hashed_password";
        when(userRepository.authenticateUser(username, passwordHash)).thenReturn(true);
        boolean result = userService.authenticateUser(username, passwordHash);

        assertTrue(result);
        verify(userRepository).authenticateUser(username, passwordHash);
    }

    @Test
    @DisplayName("Should return false when authentication fails")
    void authenticateUser_Failure() throws SQLException {
        String username = "john_doe";
        String passwordHash = "wrong_hash";
        when(userRepository.authenticateUser(username, passwordHash)).thenReturn(false);

        boolean result = userService.authenticateUser(username, passwordHash);

        assertFalse(result);
        verify(userRepository).authenticateUser(username, passwordHash);
    }

    @Test
    @DisplayName("Should check if username exists")
    void userNameExists() throws SQLException {
        String username = "existing_user";
        when(userRepository.userNameExists(username)).thenReturn(true);

        boolean result = userService.userNameExists(username);

        assertTrue(result);
        verify(userRepository).userNameExists(username);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_Success() throws SQLException {
        Long userId = 1L;
        when(userRepository.deleteUser(userId)).thenReturn(true);

        boolean result = userService.deleteUser(userId);

        assertTrue(result);
        verify(userRepository).deleteUser(userId);
    }

    @Test
    @DisplayName("Should return false when delete fails")
    void deleteUser_Failure() throws SQLException {
        Long userId = 1L;
        when(userRepository.deleteUser(userId)).thenReturn(false);

        boolean result = userService.deleteUser(userId);

        assertFalse(result);
        verify(userRepository).deleteUser(userId);
    }

    @Test
    @DisplayName("Should get all users")
    void getAllUsers_Success() throws SQLException {
        List<User> users = Arrays.asList(
                createUser(1L, "user1", "hash1"),
                createUser(2L, "user2", "hash2")
        );
        List<UserResponseDTO> responseDTOs = Arrays.asList(
                createUserResponseDTO(1L, "user1"),
                createUserResponseDTO(2L, "user2")
        );

        when(userRepository.findAll()).thenReturn(users);
        when(dtoTransformService.toUserResponseDTOs(users)).thenReturn(responseDTOs);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getName());
        assertEquals("user2", result.get(1).getName());

        verify(userRepository).findAll();
        verify(dtoTransformService).toUserResponseDTOs(users);
    }

    @Test
    @DisplayName("Should get user entity by ID")
    void getUserEntityById_Success() throws SQLException {
        Long userId = 1L;
        User expectedUser = createUser(userId, "john_doe", "hashed_password");
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.getUserEntityById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("john_doe", result.get().getName());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should get user entity by name")
    void getUserEntityByName_Success() throws SQLException {
        String username = "john_doe";
        User expectedUser = createUser(1L, username, "hashed_password");
        when(userRepository.findByName(username)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.getUserEntityByName(username);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getName());
        verify(userRepository).findByName(username);
    }

    @Test
    @DisplayName("Should handle SQLException from repository")
    void repositoryMethods_ThrowSQLException() throws SQLException {
        Long userId = 1L;
        SQLException expectedException = new SQLException("Database error");
        when(userRepository.findById(userId)).thenThrow(expectedException);

        SQLException exception = assertThrows(SQLException.class, () ->
                userService.getUserById(userId)
        );
        assertEquals("Database error", exception.getMessage());
    }

    @Test
    @DisplayName("Should use default constructor")
    void defaultConstructor() {
        UserService serviceWithDefaultConstructor = new UserService();

        assertNotNull(serviceWithDefaultConstructor);
    }

    private User createUser(Long id, String name, String passwordHash) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setPasswordHash(passwordHash);
        return user;
    }

    private UserResponseDTO createUserResponseDTO(Long id, String name) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}