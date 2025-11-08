package core.repository;

import core.entity.FunctionEntity;
import core.entity.OperationEntity;
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
class OperationRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Test
    void testSaveFindDeleteOperation() {
        // Подготовка зависимостей
        UserEntity user = new UserEntity("testuser", "hash123");
        UserEntity savedUser = userRepository.save(user);

        FunctionEntity function = new FunctionEntity(savedUser, FunctionEntity.FunctionType.analytic, "f(x)", "x^2");
        FunctionEntity savedFunction = functionRepository.save(function);

        // Создаём операцию (например, операция с ID = 1 = "дифференцирование")
        OperationEntity operation = new OperationEntity(savedFunction, 1);

        OperationEntity saved = operationRepository.save(operation);
        OperationEntity found = operationRepository.findById(saved.getId()).orElse(null);
        operationRepository.delete(saved);
        OperationEntity deleted = operationRepository.findById(saved.getId()).orElse(null);

        assertThat(saved).isNotNull();
        assertThat(found).isNotNull();
        assertThat(found.getOperationsTypeId()).isEqualTo(1);
        assertThat(deleted).isNull();
    }
}