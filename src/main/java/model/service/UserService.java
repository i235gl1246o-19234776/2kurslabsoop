package model.service;

import model.entity.User;
import model.dto.request.CreateUserRequest;
import model.dto.response.UserResponseDTO;
import model.dto.DTOTransformService;
import model.entity.UserRole;
import repository.dao.UserRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final UserRepository userRepository;
    private final DTOTransformService dtoTransformService;

    public UserService() {
        this.userRepository = new UserRepository();
        this.dtoTransformService = new DTOTransformService();
    }

    public UserService(UserRepository userRepository, DTOTransformService dtoTransformService) {
        this.userRepository = userRepository;
        this.dtoTransformService = dtoTransformService;
    }

    public UserResponseDTO createUser(CreateUserRequest userRequest) throws SQLException {
        logger.info("Создание нового пользователя: " + userRequest.getName());

        if (userRepository.userNameExists(userRequest.getName())) {
            throw new IllegalArgumentException("Пользователь с именем '" + userRequest.getName() + "' уже существует");
        }

        User user = dtoTransformService.toEntity(userRequest);
        Long userId = userRepository.createUser(user);

        Optional<User> createdUser = userRepository.findById(userId);
        if (createdUser.isPresent()) {
            return dtoTransformService.toResponseDTO(createdUser.get());
        } else {
            throw new SQLException("Не удалось получить созданного пользователя с ID: " + userId);
        }
    }

    public Optional<UserResponseDTO> getUserById(Long id) throws SQLException {
        logger.info("Получение пользователя по ID: " + id);
        Optional<User> user = userRepository.findById(id);
        return user.map(dtoTransformService::toResponseDTO);
    }

    public Optional<UserResponseDTO> getUserByName(String name) throws SQLException {
        logger.info("Получение пользователя по имени: " + name);
        Optional<User> user = userRepository.findByName(name);
        return user.map(dtoTransformService::toResponseDTO);
    }

    public boolean updateUser(User user) throws SQLException {
        logger.info("Обновление пользователя с ID: " + user.getId());
        return userRepository.updateUser(user);
    }

    public boolean authenticateUser(String name, String passwordHash) throws SQLException {
        logger.info("Аутентификация пользователя: " + name);
        return userRepository.authenticateUser(name, passwordHash);
    }

    public boolean userNameExists(String name) throws SQLException {
        return userRepository.userNameExists(name);
    }

    public boolean deleteUser(Long id) throws SQLException {
        logger.info("Удаление пользователя с ID: " + id);
        return userRepository.deleteUser(id);
    }

    public List<UserResponseDTO> getAllUsers() throws SQLException {
        logger.info("Получение всех пользователей");
        List<User> users = userRepository.findAll();
        return dtoTransformService.toUserResponseDTOs(users);
    }

    public Optional<User> getUserEntityById(Long id) throws SQLException {
        return userRepository.findById(id);
    }

    public Optional<User> getUserEntityByName(String name) throws SQLException {
        return userRepository.findByName(name);
    }

    public Optional<UserResponseDTO> authenticateAndReturnUser(String username, String password) throws SQLException {
        logger.info("Аутентификация пользователя: " + username);
        Optional<User> userOpt = userRepository.findByName(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // УДАЛЯЕМ СТРОКИ СОЗДАНИЯ НОВОГО ЭКЗЕМПЛЯРА
            // UserRepository UR = new UserRepository();
            AuthenticationService auth = new AuthenticationService(userRepository);
            // ВЫЗОВ ЧЕРЕЗ СУЩЕСТВУЮЩИЙ ЭКЗЕМПЛЯР: this.authenticationService.authenticateUser(...)
            if (auth.authenticateUser(username, password)) { // <--- Вот тут
                return Optional.of(dtoTransformService.toResponseDTO(user));
            }
        }
        return Optional.empty();
    }
}