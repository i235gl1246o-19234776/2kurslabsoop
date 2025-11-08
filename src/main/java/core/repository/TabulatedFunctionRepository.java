package core.repository;

import core.entity.TabulatedFunctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TabulatedFunctionRepository extends JpaRepository<TabulatedFunctionEntity, Long> {
    List<TabulatedFunctionEntity> findByFunction_Id(Long functionId);
}