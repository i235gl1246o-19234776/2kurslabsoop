package core.repository;

import core.entity.FunctionEntity;
import core.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FunctionRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Test
    void testSaveFindDeleteFunction() {
        UserEntity user = new UserEntity("tester", "hash123");
        UserEntity savedUser = userRepository.save(user);

        FunctionEntity function = new FunctionEntity(
                savedUser,
                FunctionEntity.FunctionType.analytic,
                "f(x)",
                "x^2 + 1"
        );

        FunctionEntity saved = functionRepository.save(function);
        FunctionEntity found = functionRepository.findById(saved.getId()).orElse(null);
        functionRepository.delete(saved);
        FunctionEntity deleted = functionRepository.findById(saved.getId()).orElse(null);

        assertThat(saved).isNotNull();
        assertThat(found).isNotNull();
        assertThat(found.getFunctionName()).isEqualTo("f(x)");
        assertThat(found.getTypeFunction()).isEqualTo(FunctionEntity.FunctionType.analytic);
        assertThat(deleted).isNull();
    }
}