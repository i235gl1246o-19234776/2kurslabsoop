package core.repository;

import core.entity.Operation;
import core.entity.Function;
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
public class OperationRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FunctionRepository functionRepository;
    @Autowired
    private OperationRepository operationRepository;

    @Test
    void testOperationCrudAndCustomSearch() {
        // Подготовка
        User user = new User();
        user.setName("diana");
        user.setPasswordHash("pass");
        user = userRepository.save(user);

        Function func = new Function();
        func.setUser(user);
        func.setTypeFunction("analytic");
        func.setFunctionName("op_func");
        func = functionRepository.save(func);

        Operation op = new Operation();
        op.setFunction(func);
        op.setOperationsTypeId(42);
        Operation saved = operationRepository.save(op);

        var ops = operationRepository.findByFunctionId(func.getId());
        assertEquals(1, ops.size());
        assertEquals(42, ops.get(0).getOperationsTypeId());

        var opsByType = operationRepository.findByOperationsTypeId(42);
        assertTrue(opsByType.stream().anyMatch(o -> o.getId().equals(saved.getId())));

        operationRepository.deleteById(saved.getId());
        assertFalse(operationRepository.findById(saved.getId()).isPresent());
    }
}