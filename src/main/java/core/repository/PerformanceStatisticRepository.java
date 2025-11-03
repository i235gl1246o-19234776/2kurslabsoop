package core.repository;

import core.entity.PerformanceStatisticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
public interface PerformanceStatisticRepository extends JpaRepository<PerformanceStatisticEntity, Long>{

}
