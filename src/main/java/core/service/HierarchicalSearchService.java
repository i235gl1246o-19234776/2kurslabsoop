package core.service;

import core.entity.*;
import core.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HierarchicalSearchService {

    private static final Logger log = LoggerFactory.getLogger(HierarchicalSearchService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Autowired
    private OperationRepository operationRepository;

    public Optional<UserHierarchy> findUserWithFullHierarchy(String username) {
        log.info("Иерархический поиск (DFS-стиль): полная загрузка для пользователя '{}'", username);
        UserEntity user = userRepository.findByName(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден", username);
            return Optional.empty();
        }

        List<FunctionEntity> functions = functionRepository.findByUser_Id(user.getId());
        initializeLazyCollections(functions);
        return Optional.of(new UserHierarchy(user, functions));
    }

    public List<UserHierarchy> findAllUsersWithHierarchy(int limit) {
        log.info("Иерархический поиск (BFS-стиль): загрузка первых {} пользователей", limit);
        List<UserEntity> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream().limit(limit).collect(Collectors.toList());

        return users.stream().map(user -> {
            List<FunctionEntity> funcs = functionRepository.findByUser_Id(user.getId());
            initializeLazyCollections(funcs);
            return new UserHierarchy(user, funcs);
        }).collect(Collectors.toList());
    }

    private void initializeLazyCollections(List<FunctionEntity> functions) {
        functions.forEach(f -> {
            if (f.getTypeFunction() == FunctionEntity.FunctionType.tabular) {
                f.getTabulatedValues().size(); // trigger lazy load
            } else {
                f.getOperations().size();
            }
        });
    }

    public static class UserHierarchy {
        private final UserEntity user;
        private final List<FunctionEntity> functions;

        public UserHierarchy(UserEntity user, List<FunctionEntity> functions) {
            this.user = user;
            this.functions = functions;
        }

        public UserEntity getUser() { return user; }
        public List<FunctionEntity> getFunctions() { return functions; }
    }
}