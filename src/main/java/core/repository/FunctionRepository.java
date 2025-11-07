package core.repository;

import core.entity.Function;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Long>{
    List<Function> findByUserId(Long userId);
    List<Function> findByTypeFunction(String typeFunction);
    boolean existsByFunctionNameAndUserId(String functionName, Long userId);
}
