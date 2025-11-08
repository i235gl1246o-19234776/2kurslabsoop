package core.repository;

import core.entity.FunctionEntity;
import core.entity.TabulatedFunctionEntity;
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
class TabulatedFunctionRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Test
    void testSaveFindDeleteTabulatedFunction() {
        // Подготовка зависимостей
        UserEntity user = new UserEntity("testuser", "hash123");
        UserEntity savedUser = userRepository.save(user);

        FunctionEntity function = new FunctionEntity(savedUser, FunctionEntity.FunctionType.tabular, "data", null);
        FunctionEntity savedFunction = functionRepository.save(function);

        // Создаём табулированное значение
        TabulatedFunctionEntity point = new TabulatedFunctionEntity(savedFunction, 1.0, 2.5);

        TabulatedFunctionEntity saved = tabulatedFunctionRepository.save(point);
        TabulatedFunctionEntity found = tabulatedFunctionRepository.findById(saved.getId()).orElse(null);
        tabulatedFunctionRepository.delete(saved);
        TabulatedFunctionEntity deleted = tabulatedFunctionRepository.findById(saved.getId()).orElse(null);

        assertThat(saved).isNotNull();
        assertThat(found).isNotNull();
        assertThat(found.getXVal()).isEqualTo(1.0);
        assertThat(found.getYVal()).isEqualTo(2.5);
        assertThat(deleted).isNull();
    }
}