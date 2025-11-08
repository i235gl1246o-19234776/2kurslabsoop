package core.service;

import core.entity.FunctionEntity;
import core.entity.UserEntity;
import core.repository.FunctionRepository;
import core.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    public Optional<UserEntity> findUserByName(String name) {
        log.debug("Одиночный поиск: пользователь по имени '{}'", name);
        return Optional.ofNullable(userRepository.findByName(name));
    }

    public Optional<UserEntity> findUserById(Long id) {
        log.debug("Одиночный поиск: пользователь по ID {}", id);
        return userRepository.findById(id);
    }

    public Optional<FunctionEntity> findFunctionById(Long id) {
        log.debug("Одиночный поиск: функция по ID {}", id);
        return functionRepository.findById(id);
    }
}