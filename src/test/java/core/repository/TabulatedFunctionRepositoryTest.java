package core.repository;

import core.entity.TabulatedFunction;
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
public class TabulatedFunctionRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FunctionRepository functionRepository;
    @Autowired
    private TabulatedFunctionRepository tfRepository;

    @Test
    void testTabulatedFunctionCrudAndCustomSearch() {
        User user = new User();
        user.setName("charlie");
        user.setPasswordHash("pass");
        user = userRepository.save(user);

        Function func = new Function();
        func.setUser(user);
        func.setTypeFunction("tabular");
        func.setFunctionName("table1");
        func = functionRepository.save(func);

        TabulatedFunction point = new TabulatedFunction();
        point.setFunction(func);
        point.setXVal(1.0);
        point.setYVal(1.0);
        TabulatedFunction saved = tfRepository.save(point);

        var points = tfRepository.findByFunctionId(func.getId());
        assertEquals(1, points.size());
        assertEquals(1.0, points.get(0).getXVal());

        var pointsByX = tfRepository.findByXVal(1.0);
        assertTrue(pointsByX.stream().anyMatch(p -> p.getId().equals(saved.getId())));

        tfRepository.deleteById(saved.getId());
        assertFalse(tfRepository.findById(saved.getId()).isPresent());
    }
}