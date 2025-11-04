package core.repository;

import core.entity.FunctionEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FunctionRepository extends JpaRepository<FunctionEntity, Long> {
    List<FunctionEntity> findByUser_Id(Long userId);
    List<FunctionEntity> findByUser_IdOrderByFunctionNameAsc(Long userId);
    List<FunctionEntity> findByUser_IdOrderByTypeFunctionAsc(Long userId);
    List<FunctionEntity> findByUser_IdAndTypeFunction(Long userId, FunctionEntity.FunctionType functionType);
}