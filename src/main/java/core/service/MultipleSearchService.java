package core.service;

import core.entity.*;
import core.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MultipleSearchService {

    private static final Logger log = LoggerFactory.getLogger(MultipleSearchService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private FunctionRepository functionRepository;
    @Autowired private TabulatedFunctionRepository tabulatedFunctionRepository;
    @Autowired private OperationRepository operationRepository;

    public List<UserEntity> findAllUsers(){
        log.info("Множественный поиск: все пользователи");
        return userRepository.findAll();
    }

    public List<FunctionEntity> findFunctionsByUser(Long userId){
        log.info("Множественный поиск: функции пользователя ID={}", userId);
        return functionRepository.findByUser_Id(userId);
    }

    public List<FunctionEntity> findAnalyticFunctionByUser(Long userId){
        log.info("Множественный поиск: аналитические функции пользователя ID={}", userId);
        return functionRepository.findByUser_IdAndTypeFunction(userId, FunctionEntity.FunctionType.ANALYTIC);
    }

    public List<TabulatedFunctionEntity> findPointsInRange(Long functionId, Double xMin, Double xMax){
        log.info("Множественный поиск: точки функции ID={} в диапазоне X=[{},{}]",functionId, xMin, xMax);
        return tabulatedFunctionRepository.findByFunction_IdAndXValBetween(functionId, xMin, xMax);
    }

    public List<OperationEntity> findOperationsByFunction(Long functionId){
        log.info("Множественный поиск: операции функции ID={}", functionId);
        return operationRepository.findByFunction_Id(functionId);
    }
}
