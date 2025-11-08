package core.service;

import core.entity.TabulatedFunctionEntity;
import core.entity.UserEntity;
import core.repository.TabulatedFunctionRepository;
import core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SortedSearchService {

    private static final Logger log = LoggerFactory.getLogger(SortedSearchService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    public List<UserEntity> findAllUsersSorted(Sort.Direction direction, String... properties) {
        log.debug("Поиск со сортировкой: пользователи, сортировка по {} {}", properties, direction);
        return userRepository.findAll(Sort.by(direction, properties));
    }

    public List<TabulatedFunctionEntity> findTabulatedSortedByX(Long functionId, Sort.Direction direction) {
        log.debug("Поиск со сортировкой: точки функции ID={}, сортировка по X {}", functionId, direction);
        List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(functionId);
        if (direction == Sort.Direction.ASC) {
            points.sort(Comparator.comparing(TabulatedFunctionEntity::getXVal));
        } else {
            points.sort(Comparator.comparing(TabulatedFunctionEntity::getXVal).reversed());
        }
        return points;
    }
}