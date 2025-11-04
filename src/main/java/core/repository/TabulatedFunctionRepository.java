package core.repository;

import core.entity.TabulatedFunctionEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface TabulatedFunctionRepository extends JpaRepository<TabulatedFunctionEntity, Long> {
    List<TabulatedFunctionEntity> findByFunction_Id(Long functionId);
    @Query("SELECT t FROM TabulatedFunctionEntity t WHERE t.function.id = :functionId AND t.xVal BETWEEN :min AND :max")
    List<TabulatedFunctionEntity> findByFunction_IdAndXValBetween(
            @Param("functionId") Long functionId,
            @Param("min") Double min,
            @Param("max") Double max
    );
    @Query("SELECT t FROM TabulatedFunctionEntity t WHERE t.function.id = :functionId ORDER BY t.xVal ASC")
    List<TabulatedFunctionEntity> findByFunction_IdOrderByXValAsc(@Param("functionId") Long functionId);
    @Query("SELECT t FROM TabulatedFunctionEntity t WHERE t.function.id = :functionId ORDER BY t.xVal DESC")
    List<TabulatedFunctionEntity> findByFunction_IdOrderByXValDesc(@Param("functionId") Long functionId);
}