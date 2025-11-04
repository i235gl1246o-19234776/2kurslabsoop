package core.service;

import core.entity.*;
import core.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SingleSearchService {

    private static final Logger log = LoggerFactory.getLogger(SingleSearchService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private FunctionRepository functionRepository;
    @Autowired private TabulatedFunctionRepository tabulatedFunctionRepository;
    @Autowired private OperationRepository operationRepository;

    public Optional<UserEntity> findUserById(Long id){
        log.info("Одиночный поиск: пользователь по ID={}", id);
        return userRepository.findById(id);
    }

    public Optional<UserEntity> findUserByName(String name){
        log.info("Одиночный поиск: пользователь по имени '{}'",name);
        return userRepository.findByName(name);
    }

    public Optional<FunctionEntity> findFunctionById(Long id){
        log.info("Одиночный поиск: функция по ID={}", id);
        return functionRepository.findById(id);
    }

    public Optional<TabulatedFunctionEntity> findPointById(Long id){
        log.info("Одиночный поиск: точка по ID={}", id);
        return tabulatedFunctionRepository.findById(id);
    }

    public Optional<OperationEntity> findOperationById(Long id){
        log.info("Одиночный поиск: операция по ID={}", id);
        return operationRepository.findById(id);
    }
}
