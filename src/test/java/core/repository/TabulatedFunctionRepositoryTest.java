package core.repository;

import core.entity.FunctionEntity;
import core.entity.TabulatedFunctionEntity;
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
class TabulatedFunctionRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Test
    void testSaveFindDeleteTabulatedFunction() {
        UserEntity user = new UserEntity();
        user.setName("tester");
        user.setPasswordHash("hash123");
        UserEntity savedUser = userRepository.save(user);

        FunctionEntity function = new FunctionEntity();
        function.setUser(savedUser);
        function.setTypeFunction(FunctionEntity.FunctionType.tabular);
        function.setFunctionName("data");
        FunctionEntity savedFunction = functionRepository.save(function);

        TabulatedFunctionEntity point = new TabulatedFunctionEntity();
        point.setFunction(savedFunction);
        point.setXVal(1.0);
        point.setYVal(2.5);

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