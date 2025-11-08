package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.CreateUserRequest; // Предполагается, что CreateUserRequest также используется для Update
import model.dto.response.UserResponseDTO;
import model.User;
import repository.UserRepository;
import service.AuthenticationService;
import service.DTOTransformService; // Добавляем импорт

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UserServlet.class.getName());
    private UserRepository userRepository;
    private AuthenticationService authService;
    private DTOTransformService transformService; // Добавляем поле для сервиса преобразований
    private ObjectMapper objectMapper;
    private Validator validator;

    @Override
    public void init() throws ServletException {
        super.init();
        this.userRepository = new UserRepository();
        this.authService = new AuthenticationService(userRepository);
        this.transformService = new DTOTransformService(); // Инициализируем сервис преобразований
        this.objectMapper = new ObjectMapper();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        logger.info("UserServlet инициализирован с JSON поддержкой и DTOTransformService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("Обработка GET запроса: " + request.getRequestURI());

        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String pathInfo = request.getPathInfo();
            String responseData = null;
            int httpStatus = HttpServletResponse.SC_OK;

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/users
                try {
                    List<User> users = userRepository.findAll();
                    // Используем transformService для пакетного преобразования
                    List<UserResponseDTO> responseDtos = transformService.toUserResponseDTOs(users);
                    responseData = objectMapper.writeValueAsString(responseDtos);
                    logger.info("Получено " + users.size() + " пользователей");
                } catch (SQLException e) {
                    logger.severe("Ошибка БД при получении списка пользователей: " + e.getMessage());
                    httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка базы данных"));
                }
            } else if (pathInfo.matches("/\\d+")) {
                // GET /api/users/{id}
                String idStr = pathInfo.substring(1);
                Optional<String> userJsonOpt = getUserByIdOpt(idStr);
                if (userJsonOpt.isPresent()) {
                    responseData = userJsonOpt.get();
                    httpStatus = HttpServletResponse.SC_OK;
                } else {
                    httpStatus = HttpServletResponse.SC_NOT_FOUND;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Пользователь не найден"));
                }
            } else if (pathInfo.startsWith("/name/")) {
                // GET /api/users/name/{name}
                String name = pathInfo.substring(6); // "/name/".length()
                Optional<String> userJsonOpt = getUserByNameOpt(name);
                if (userJsonOpt.isPresent()) {
                    responseData = userJsonOpt.get();
                    httpStatus = HttpServletResponse.SC_OK;
                } else {
                    httpStatus = HttpServletResponse.SC_NOT_FOUND;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Пользователь не найден"));
                }
            } else if (pathInfo.startsWith("/exist/")) {
                // GET /api/users/exist/{name}
                String name = pathInfo.substring(7); // "/exist/".length()
                Optional<Boolean> existsOpt = checkUserExistsByNameOpt(name);
                if (existsOpt.isPresent()) {
                    responseData = objectMapper.writeValueAsString(Map.of("exists", existsOpt.get()));
                    httpStatus = HttpServletResponse.SC_OK;
                } else {
                    httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка базы данных"));
                }
            } else {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат URL"));
            }

            // Устанавливаем статус ПЕРЕД записью данных
            response.setStatus(httpStatus);
            response.getWriter().write(responseData);

        } catch (Exception e) {
            logger.severe("Ошибка в GET запросе: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", "Внутренняя ошибка сервера")));
        }

        long endTime = System.currentTimeMillis();
        logger.info("GET запрос обработан за " + (endTime - startTime) + " мс");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("Обработка POST запроса");

        String pathInfo = request.getPathInfo();
        int httpStatus = HttpServletResponse.SC_OK;
        String responseData = null;

        try {
            if (pathInfo != null && pathInfo.equals("/authenticate")) {
                // POST /api/users/authenticate
                String body = request.getReader().lines().collect(Collectors.joining());
                logger.fine("Получено тело запроса аутентификации: " + body);

                Map<String, String> authRequest = objectMapper.readValue(body, Map.class);
                String name = authRequest.get("name");
                String password = authRequest.get("password");

                if (name == null || password == null) {
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Поля 'name' и 'password' обязательны"));
                } else {
                    boolean authenticated = authService.authenticateUser(name, password);

                    if (authenticated) {
                        Optional<User> userOpt = userRepository.findByName(name);
                        if (userOpt.isPresent()) {
                            // Используем transformService для преобразования
                            UserResponseDTO userResponse = transformService.toResponseDTO(userOpt.get());
                            httpStatus = HttpServletResponse.SC_OK;
                            responseData = objectMapper.writeValueAsString(userResponse);
                            logger.info("Успешная аутентификация пользователя: " + name);
                        } else {
                            // Неожиданная ситуация: аутентификация прошла, но пользователь не найден в БД
                            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                            responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка аутентификации: пользователь не найден"));
                            logger.severe("Пользователь " + name + " аутентифицирован, но не найден в БД");
                        }
                    } else {
                        httpStatus = HttpServletResponse.SC_UNAUTHORIZED; // 401 Unauthorized
                        responseData = objectMapper.writeValueAsString(Map.of("error", "Неверные имя пользователя или пароль"));
                        logger.warning("Неудачная аутентификация для пользователя: " + name);
                    }
                }
            } else if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/users (создание)
                String body = request.getReader().lines().collect(Collectors.joining());
                logger.fine("Получено тело запроса: " + body);

                CreateUserRequest userRequest = null;
                try {
                    userRequest = objectMapper.readValue(body, CreateUserRequest.class);
                    logger.info("JSON успешно преобразован в CreateUserRequest: " + userRequest);
                } catch (Exception e) {
                    logger.warning("Ошибка парсинга JSON: " + e.getMessage());
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат JSON"));
                }

                if (userRequest != null) {
                    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);
                    if (!violations.isEmpty()) {
                        String errorMessage = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                        logger.warning("Ошибки валидации: " + errorMessage);
                        httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                        responseData = objectMapper.writeValueAsString(Map.of("error", errorMessage));
                    } else {
                        String name = userRequest.getName();
                        String password = userRequest.getPassword();

                        if (userRepository.userNameExists(name)) {
                            httpStatus = HttpServletResponse.SC_CONFLICT;
                            responseData = objectMapper.writeValueAsString(Map.of("error", "Пользователь с таким именем уже существует"));
                            logger.warning("Попытка создания пользователя с существующим именем: " + name);
                        } else {
                            authService.registerUser(name, password);
                            Optional<User> createdUser = userRepository.findByName(name);

                            if (createdUser.isPresent()) {
                                // Используем transformService для преобразования
                                UserResponseDTO userResponse = transformService.toResponseDTO(createdUser.get());
                                httpStatus = HttpServletResponse.SC_CREATED;
                                responseData = objectMapper.writeValueAsString(userResponse);
                                logger.info("Создан новый пользователь: " + name + " с ID: " + createdUser.get().getId());
                            } else {
                                throw new SQLException("Пользователь не найден после создания");
                            }
                        }
                    }
                }
            } else {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат URL"));
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при работе с БД: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка при работе с базой данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Внутренняя ошибка сервера"));
        }

        // Устанавливаем статус ПЕРЕД записью данных
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseData);

        long endTime = System.currentTimeMillis();
        logger.info("POST запрос обработан за " + (endTime - startTime) + " мс");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Обработка PUT запроса для обновления пользователя");

        int httpStatus = HttpServletResponse.SC_OK;
        String responseData = null;

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = objectMapper.writeValueAsString(Map.of("error", "ID пользователя обязателен"));
            } else {
                String idStr = pathInfo.substring(1);
                Long id;
                try {
                    id = Long.parseLong(idStr);
                } catch (NumberFormatException e) {
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат ID"));
                    response.setStatus(httpStatus);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(responseData);
                    return;
                }

                String body = request.getReader().lines().collect(Collectors.joining());
                logger.fine("Получено тело запроса для обновления: " + body);

                CreateUserRequest updateRequest = null;
                try {
                    updateRequest = objectMapper.readValue(body, CreateUserRequest.class);
                    logger.info("JSON успешно преобразован для обновления: " + updateRequest);
                } catch (Exception e) {
                    logger.warning("Ошибка парсинга JSON для обновления: " + e.getMessage());
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат JSON"));
                }

                if (updateRequest != null) {
                    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(updateRequest);
                    if (!violations.isEmpty()) {
                        String errorMessage = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                        logger.warning("Ошибки валидации при обновлении: " + errorMessage);
                        httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                        responseData = objectMapper.writeValueAsString(Map.of("error", errorMessage));
                    } else {
                        Optional<User> existingUserOpt = userRepository.findById(id);
                        if (existingUserOpt.isEmpty()) {
                            httpStatus = HttpServletResponse.SC_NOT_FOUND;
                            responseData = objectMapper.writeValueAsString(Map.of("error", "Пользователь не найден"));
                            logger.warning("Попытка обновления несуществующего пользователя с ID: " + id);
                        } else {
                            User userToUpdate = existingUserOpt.get();

                            if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty()) {
                                String newName = updateRequest.getName().trim();
                                if (!userToUpdate.getName().equals(newName) && userRepository.userNameExists(newName)) {
                                    httpStatus = HttpServletResponse.SC_CONFLICT;
                                    responseData = objectMapper.writeValueAsString(Map.of("error", "Пользователь с таким именем уже существует"));
                                } else {
                                    userToUpdate.setName(newName);
                                }
                            }

                            if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
                                String hashedPassword = authService.hashPassword(updateRequest.getPassword());
                                userToUpdate.setPasswordHash(hashedPassword);
                            }

                            if (httpStatus == HttpServletResponse.SC_OK) { // Только если предыдущие проверки прошли успешно
                                boolean updated = userRepository.updateUser(userToUpdate);

                                if (updated) {
                                    // Используем transformService для преобразования
                                    UserResponseDTO responseDto = transformService.toResponseDTO(userToUpdate);
                                    responseData = objectMapper.writeValueAsString(responseDto);
                                    logger.info("Пользователь обновлен: " + userToUpdate.getId());
                                } else {
                                    httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                                    responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка при обновлении пользователя"));
                                    logger.warning("Ошибка при обновлении пользователя с ID: " + id);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении пользователя: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка при обновлении пользователя"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Внутренняя ошибка сервера"));
        }

        // Устанавливаем статус ПЕРЕД записью данных
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseData);

        long endTime = System.currentTimeMillis();
        logger.info("PUT запрос обработан за " + (endTime - startTime) + " мс");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Обработка DELETE запроса для удаления пользователя");

        int httpStatus = HttpServletResponse.SC_OK;
        String responseData = null;

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = objectMapper.writeValueAsString(Map.of("error", "ID пользователя обязателен"));
            } else {
                String idStr = pathInfo.substring(1);
                Long id;
                try {
                    id = Long.parseLong(idStr);
                } catch (NumberFormatException e) {
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат ID"));
                    response.setStatus(httpStatus);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(responseData);
                    return;
                }

                boolean deleted = userRepository.deleteUser(id);

                if (deleted) {
                    responseData = objectMapper.writeValueAsString(Map.of("message", "Пользователь удален"));
                    logger.info("Удален пользователь с ID: " + id);
                } else {
                    httpStatus = HttpServletResponse.SC_NOT_FOUND;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Пользователь не найден"));
                    logger.warning("Попытка удаления несуществующего пользователя с ID: " + id);
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении пользователя: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка при удалении пользователя"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Внутренняя ошибка сервера"));
        }

        // Устанавливаем статус ПЕРЕД записью данных
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseData);

        long endTime = System.currentTimeMillis();
        logger.info("DELETE запрос обработан за " + (endTime - startTime) + " мс");
    }

    // --- Вспомогательные методы для получения данных ---
    // Эти методы возвращают Optional, чтобы вызывающий код мог определить статус

    private Optional<String> getUserByIdOpt(String idStr) {
        try {
            Long id = Long.parseLong(idStr);
            Optional<User> userOpt = userRepository.findById(id);

            if (userOpt.isPresent()) {
                // Используем transformService для преобразования
                UserResponseDTO responseDto = transformService.toResponseDTO(userOpt.get());
                logger.info("Найден пользователь по ID: " + id);
                return Optional.of(objectMapper.writeValueAsString(responseDto));
            } else {
                logger.warning("Пользователь не найден по ID: " + id);
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат ID: " + idStr);
            return Optional.empty();
        } catch (SQLException e) {
            logger.severe("Ошибка БД при поиске пользователя: " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            logger.severe("Ошибка при сериализации пользователя в JSON: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> getUserByNameOpt(String name) {
        try {
            Optional<User> userOpt = userRepository.findByName(name);

            if (userOpt.isPresent()) {
                // Используем transformService для преобразования
                UserResponseDTO responseDto = transformService.toResponseDTO(userOpt.get());
                logger.info("Найден пользователь по имени: " + name);
                return Optional.of(objectMapper.writeValueAsString(responseDto));
            } else {
                logger.warning("Пользователь не найден по имени: " + name);
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.severe("Ошибка БД при поиске пользователя по имени: " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            logger.severe("Ошибка при сериализации пользователя в JSON: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Boolean> checkUserExistsByNameOpt(String name) {
        try {
            boolean exists = userRepository.userNameExists(name);
            logger.info("Проверка существования пользователя по имени '" + name + "': " + exists);
            return Optional.of(exists);
        } catch (SQLException e) {
            logger.severe("Ошибка БД при проверке существования пользователя: " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            logger.severe("Ошибка при проверке существования: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void destroy() {
        logger.info("UserServlet уничтожается");
        super.destroy();
    }
}