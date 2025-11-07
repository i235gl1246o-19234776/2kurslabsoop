package core.repository;

import core.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("classpath:test.properties")
@Transactional

public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void testUserCrudAndCustomSearch() {
        User user = new User();
        user.setName("alice");
        user.setPasswordHash("hash123");
        User saved = userRepository.save(user);

        assertTrue(userRepository.findById(saved.getId()).isPresent());

        var foundByName = userRepository.findByName("alice");
        assertTrue(foundByName.isPresent());
        assertEquals(saved.getId(), foundByName.get().getId());

        assertTrue(userRepository.existsByName("alice"));
        assertFalse(userRepository.existsByName("nonexistent"));

        userRepository.deleteById(saved.getId());
        assertFalse(userRepository.findById(saved.getId()).isPresent());
    }
}