package core.repository;

import core.entity.OperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<OperationEntity, Long> {
    List<OperationEntity> findByFunction_Id(Long functionId);
    List<OperationEntity> findByFunction_User_Id(Long userId);
}