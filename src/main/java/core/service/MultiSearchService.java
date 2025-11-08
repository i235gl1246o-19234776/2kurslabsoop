package core.service;

import core.entity.FunctionEntity;
import core.entity.OperationEntity;
import core.entity.TabulatedFunctionEntity;
import core.repository.FunctionRepository;
import core.repository.OperationRepository;
import core.repository.TabulatedFunctionRepository;
import core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MultiSearchService {

    private static final Logger log = LoggerFactory.getLogger(MultiSearchService.class);

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<FunctionEntity> findFunctionsByUserId(Long userId) {
        log.debug("Множественный поиск: функции пользователя с ID {}", userId);
        return functionRepository.findByUser_Id(userId);
    }

    public List<TabulatedFunctionEntity> findTabulatedByFunctionId(Long functionId) {
        log.debug("Множественный поиск: точки функции ID={}", functionId);
        return tabulatedFunctionRepository.findByFunction_Id(functionId);
    }

    public List<OperationEntity> findOperationsByFunctionId(Long functionId) {
        log.debug("Множественный поиск: операции функции ID={}", functionId);
        return operationRepository.findAll().stream()
                .filter(op -> op.getFunction().getId().equals(functionId))
                .collect(Collectors.toList());
    }

    public List<FunctionEntity> findFunctionsByType(FunctionEntity.FunctionType type) {
        log.debug("Множественный поиск: функции типа {}", type);
        return functionRepository.findAll().stream()
                .filter(f -> f.getTypeFunction() == type)
                .collect(Collectors.toList());
    }

    public List<FunctionEntity> searchFunctionsByNamePattern(String pattern) {
        log.debug("Множественный поиск: функции, имя содержит '{}'", pattern);
        return functionRepository.findAll().stream()
                .filter(f -> f.getFunctionName().contains(pattern))
                .collect(Collectors.toList());
    }
}