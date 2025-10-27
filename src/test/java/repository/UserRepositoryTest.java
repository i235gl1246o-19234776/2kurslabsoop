package repository;

import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testbase.TestBase;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends TestBase {

    @Test
    void testCreateUser() throws SQLException {
        // Given
        User user = new User("testuser", "hashedpassword");

        // When
        Long userId = userRepository.createUser(user);

        // Then
        assertNotNull(userId);
        assertTrue(userId > 0);

        User createdUser = userRepository.findById(userId);
        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());
        assertEquals("hashedpassword", createdUser.getPasswordHash());
        assertNotNull(createdUser.getCreatedAt());
    }

    @Test
    void testCreateUser_DuplicateUsername() throws SQLException {
        // Given
        User user1 = new User("duplicate", "pass1");
        userRepository.createUser(user1);

        User user2 = new User("duplicate", "pass2");

        // When & Then
        assertThrows(SQLException.class, () -> {
            userRepository.createUser(user2);
        });
    }

    @Test
    void testFindById() throws SQLException {
        // Given
        Long userId = createTestUser("findbyid_user");

        // When
        User user = userRepository.findById(userId);

        // Then
        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertEquals("findbyid_user", user.getUsername());
    }

    @Test
    void testFindById_NotFound() throws SQLException {
        // When
        User user = userRepository.findById(999999L);

        // Then
        assertNull(user);
    }

    @Test
    void testFindByUsername() throws SQLException {
        // Given
        createTestUser("findbyusername_user");

        // When
        User user = userRepository.findByUsername("findbyusername_user");

        // Then
        assertNotNull(user);
        assertEquals("findbyusername_user", user.getUsername());
    }

    @Test
    void testFindByUsername_NotFound() throws SQLException {
        // When
        User user = userRepository.findByUsername("nonexistent");

        // Then
        assertNull(user);
    }


    @Test
    void testUpdateUser() throws SQLException {
        // Given
        Long userId = createTestUser("update_user");
        User user = userRepository.findById(userId);
        user.setUsername("updated_username");
        user.setPasswordHash("new_hashed_password");

        // When
        boolean success = userRepository.updateUser(user);
        User updatedUser = userRepository.findById(userId);

        // Then
        assertTrue(success);
        assertNotNull(updatedUser);
        assertEquals("updated_username", updatedUser.getUsername());
        assertEquals("new_hashed_password", updatedUser.getPasswordHash());
    }


    @Test
    void testUpdatePassword() throws SQLException {
        // Given
        Long userId = createTestUser("update_password_user");

        // When
        boolean success = userRepository.updatePassword(userId, "new_hashed_pass");
        User updatedUser = userRepository.findById(userId);

        // Then
        assertTrue(success);
        assertNotNull(updatedUser);
        assertEquals("new_hashed_pass", updatedUser.getPasswordHash());
    }

    @Test
    void testDeleteUser() throws SQLException {
        // Given
        Long userId = createTestUser("delete_user");

        // When
        boolean success = userRepository.deleteUser(userId);
        User deletedUser = userRepository.findById(userId);

        // Then
        assertTrue(success);
        assertNull(deletedUser);
    }

    @Test
    void testDeleteUser_NotFound() throws SQLException {
        // When
        boolean success = userRepository.deleteUser(999999L);

        // Then
        assertFalse(success);
    }
    @Test
    void testFindAll() throws Exception {
        createTestUser("alice");
        createTestUser("bob");

        List<User> users = userRepository.findAll();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> "alice".equals(u.getUsername())));
        assertTrue(users.stream().anyMatch(u -> "bob".equals(u.getUsername())));
    }


}