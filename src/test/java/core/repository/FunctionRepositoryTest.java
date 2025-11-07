package core.repository;

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
public class FunctionRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FunctionRepository functionRepository;

    @Test
    void testFunctionCrudAndCustomSearch() {
        User user = new User();
        user.setName("bob");
        user.setPasswordHash("pass");
        User savedUser = userRepository.save(user);

        Function func = new Function();
        func.setUser(savedUser);
        func.setTypeFunction("analytic");
        func.setFunctionName("f(x)=2x");
        func.setFunctionExpression("2 * x");
        Function savedFunc = functionRepository.save(func);

        var userFuncs = functionRepository.findByUserId(savedUser.getId());
        assertEquals(1, userFuncs.size());
        assertEquals("analytic", userFuncs.get(0).getTypeFunction());

        var analyticFuncs = functionRepository.findByTypeFunction("analytic");
        assertTrue(analyticFuncs.stream().anyMatch(f -> f.getId().equals(savedFunc.getId())));

        assertTrue(functionRepository.existsByFunctionNameAndUserId("f(x)=2x", savedUser.getId()));
        assertFalse(functionRepository.existsByFunctionNameAndUserId("nonexistent", savedUser.getId()));

        functionRepository.deleteById(savedFunc.getId());
        assertFalse(functionRepository.findById(savedFunc.getId()).isPresent());
    }
}