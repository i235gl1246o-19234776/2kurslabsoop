package core.repository;

import core.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveFindDeleteUser() {
        UserEntity user = new UserEntity();
        user.setName("testuser");
        user.setPasswordHash("hash123");

        UserEntity saved = userRepository.save(user);
        UserEntity found = userRepository.findById(saved.getId()).orElse(null);
        userRepository.delete(saved);
        UserEntity deleted = userRepository.findById(saved.getId()).orElse(null);

        assertThat(saved).isNotNull();
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("testuser");
        assertThat(deleted).isNull();
    }
}