package core.repository;

import core.entity.TabulatedFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TabulatedFunctionRepository extends JpaRepository<TabulatedFunction, Long>{
    List<TabulatedFunction> findByFunctionId(Long functionId);
    @Query("SELECT t FROM TabulatedFunction t WHERE t.xVal = :xVal")
    List<TabulatedFunction> findByXVal(@Param("xVal") Double xVal);
}
