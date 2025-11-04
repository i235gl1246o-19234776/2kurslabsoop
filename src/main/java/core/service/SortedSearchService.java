package core.service;

import core.entity.*;
import core.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SortedSearchService {

    private static final Logger log = LoggerFactory.getLogger(SortedSearchService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private FunctionRepository functionRepository;
    @Autowired private TabulatedFunctionRepository tabulatedFunctionRepository;

    public List<UserEntity> findAllUsersSortedByName(){
        log.info("Поиск с сортировкой: пользователи по имени (A :: Z)");
        return userRepository.findAll(Sort.by("name"));
    }

    public List<FunctionEntity> findFunctionsByUserSortedByName(Long userId){
        log.info("Поиск с сортировкой: функции пользователя ID={} по имени", userId);
        return functionRepository.findByUser_IdOrderByFunctionNameAsc(userId);
    }

    public List<FunctionEntity> findFunctionsByUserSortedByType(Long userId){
        log.info("Поиск с сортировкой: функции пользователя ID={} по типу", userId);
        return functionRepository.findByUser_IdOrderByTypeFunctionAsc(userId);
    }

    public List<TabulatedFunctionEntity> findPointsByFunctionSortedByX(Long functionId) {
        log.info("Поиск с сортировкой: точки функции ID={} по X (возрастание)", functionId);
        return tabulatedFunctionRepository.findByFunction_IdOrderByXValAsc(functionId);
    }

    public List<TabulatedFunctionEntity> findPointsByFunctionSortedByXDesc(Long functionId) {
        log.info("Поиск с сортировкой: точки функции ID={} по X (убывание)", functionId);
        return tabulatedFunctionRepository.findByFunction_IdOrderByXValDesc(functionId);
    }
}
