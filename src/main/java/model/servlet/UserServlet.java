package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.CreateUserRequest;
import model.dto.response.UserResponseDTO;
import model.entity.User;
import repository.dao.UserRepository;
import service.AuthenticationService;
import model.dto.DTOTransformService;

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
    private DTOTransformService transformService;
    private ObjectMapper objectMapper;
    private Validator validator;

    @Override
    public void init() throws ServletException {
        super.init();
        this.userRepository = new UserRepository();
        this.authService = new AuthenticationService(userRepository);
        this.transformService = new DTOTransformService();
        this.objectMapper = new ObjectMapper();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        logger.info("UserServlet инициализирован с JSON поддержкой и DTOTransformService");
    }

    // ==================== УТИЛИТНЫЕ МЕТОДЫ ДЛЯ ФОРМИРОВАНИЯ ОТВЕТОВ ====================

    /**
     * ✅ ХОРОШИЙ ОТВЕТ - Успешная операция с данными
     */
    private String createSuccessResponse(Object data) {
        return convertToJson(Map.of("success", true, "data", data));
    }

    /**
     * ✅ ХОРОШИЙ ОТВЕТ - Успешная операция без данных
     */
    private String createSuccessResponse(String message) {
        return convertToJson(Map.of("success", true, "message", message));
    }

    /**
     * ❌ ПЛОХОЙ ОТВЕТ - Ошибка клиента (4xx)
     */
    private String createErrorResponse(int status, String errorMessage) {
        return convertToJson(Map.of(
                "success", false,
                "error", errorMessage,
                "status", status
        ));
    }

    /**
     * ❌ ПЛОХОЙ ОТВЕТ - Ошибка валидации
     */
    private String createValidationErrorResponse(Set<ConstraintViolation<CreateUserRequest>> violations) {
        String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
    }

    /**
     * Безопасное преобразование в JSON
     */
    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.severe("Ошибка преобразования в JSON: " + e.getMessage());
            return "{\"success\":false,\"error\":\"Ошибка сервера при формировании ответа\",\"status\":500}";
        }
    }

    /**
     * Вспомогательный метод для преобразования User в JSON
     */
    private Optional<String> userToJsonResponse(User user) {
        try {
            UserResponseDTO responseDto = transformService.toResponseDTO(user);
            return Optional.of(convertToJson(responseDto));
        } catch (Exception e) {
            logger.severe("Ошибка при сериализации пользователя в JSON: " + e.getMessage());
            return Optional.empty();
        }
    }

    // ==================== ОБРАБОТКА GET ЗАПРОСОВ ====================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("Обработка GET запроса: " + request.getRequestURI());

        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String pathInfo = request.getPathInfo();
            String responseData;
            int httpStatus;

            if (pathInfo == null || pathInfo.equals("/")) {
                // ✅ ХОРОШИЙ ЗАПРОС: GET /api/users - получить всех пользователей
                // ❌ ПЛОХОЙ ЗАПРОС: Ошибка БД при получении списка
                try {
                    List<User> users = userRepository.findAll();
                    List<UserResponseDTO> responseDtos = transformService.toUserResponseDTOs(users);

                    if (users.isEmpty()) {
                        // ✅ ХОРОШИЙ ОТВЕТ: Пустой список - это нормально
                        responseData = createSuccessResponse("Пользователи не найдены");
                        httpStatus = HttpServletResponse.SC_OK;
                    } else {
                        // ✅ ХОРОШИЙ ОТВЕТ: Список пользователей
                        responseData = createSuccessResponse(responseDtos);
                        httpStatus = HttpServletResponse.SC_OK;
                    }
                    logger.info("Получено " + users.size() + " пользователей");
                } catch (SQLException e) {
                    // ❌ ПЛОХОЙ ОТВЕТ: Ошибка базы данных
                    logger.severe("Ошибка БД при получении списка пользователей: " + e.getMessage());
                    httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    responseData = createErrorResponse(httpStatus, "Ошибка базы данных");
                }
            } else if (pathInfo.matches("/\\d+")) {
                // ✅ ХОРОШИЙ ЗАПРОС: GET /api/users/1 - получить пользователя по ID
                // ❌ ПЛОХОЙ ЗАПРОС: GET /api/users/999 - несуществующий ID
                // ❌ ПЛОХОЙ ЗАПРОС: GET /api/users/abc - неверный формат ID
                String idStr = pathInfo.substring(1);
                Optional<String> userJsonOpt = getUserByIdOpt(idStr);
                if (userJsonOpt.isPresent()) {
                    // ✅ ХОРОШИЙ ОТВЕТ: Пользователь найден
                    responseData = userJsonOpt.get();
                    httpStatus = HttpServletResponse.SC_OK;
                } else {
                    // ❌ ПЛОХОЙ ОТВЕТ: Пользователь не найден
                    httpStatus = HttpServletResponse.SC_NOT_FOUND;
                    responseData = createErrorResponse(httpStatus, "Пользователь не найден");
                }
            } else if (pathInfo.startsWith("/name/")) {
                // ✅ ХОРОШИЙ ЗАПРОС: GET /api/users/name/ivan - получить по имени
                // ❌ ПЛОХОЙ ЗАПРОС: GET /api/users/name/nonexistent - несуществующее имя
                String name = pathInfo.substring(6);
                if (name == null || name.trim().isEmpty()) {
                    // ❌ ПЛОХОЙ ОТВЕТ: Пустое имя
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = createErrorResponse(httpStatus, "Имя пользователя не может быть пустым");
                } else {
                    Optional<String> userJsonOpt = getUserByNameOpt(name);
                    if (userJsonOpt.isPresent()) {
                        // ✅ ХОРОШИЙ ОТВЕТ: Пользователь найден
                        responseData = userJsonOpt.get();
                        httpStatus = HttpServletResponse.SC_OK;
                    } else {
                        // ❌ ПЛОХОЙ ОТВЕТ: Пользователь не найден
                        httpStatus = HttpServletResponse.SC_NOT_FOUND;
                        responseData = createErrorResponse(httpStatus, "Пользователь не найден");
                    }
                }
            } else if (pathInfo.startsWith("/exist/")) {
                // ✅ ХОРОШИЙ ЗАПРОС: GET /api/users/exist/ivan - проверить существование
                String name = pathInfo.substring(7);
                Optional<Boolean> existsOpt = checkUserExistsByNameOpt(name);
                if (existsOpt.isPresent()) {
                    // ✅ ХОРОШИЙ ОТВЕТ: Результат проверки
                    responseData = createSuccessResponse(Map.of("exists", existsOpt.get()));
                    httpStatus = HttpServletResponse.SC_OK;
                } else {
                    // ❌ ПЛОХОЙ ОТВЕТ: Ошибка БД
                    httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    responseData = createErrorResponse(httpStatus, "Ошибка базы данных");
                }
            } else {
                // ❌ ПЛОХОЙ ЗАПРОС: GET /api/users/invalid-path - неверный URL
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = createErrorResponse(httpStatus, "Неверный формат URL");
            }

            response.setStatus(httpStatus);
            response.getWriter().write(responseData);

        } catch (Exception e) {
            // ❌ ПЛОХОЙ ОТВЕТ: Неожиданная ошибка сервера
            logger.severe("Неожиданная ошибка в GET запросе: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(createErrorResponse(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Внутренняя ошибка сервера"
            ));
        }

        long endTime = System.currentTimeMillis();
        logger.info("GET запрос обработан за " + (endTime - startTime) + " мс");
    }

    // ==================== ОБРАБОТКА POST ЗАПРОСОВ ====================

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
                // ✅ ХОРОШИЙ ЗАПРОС: POST /api/users/authenticate с правильными данными
                // ❌ ПЛОХОЙ ЗАПРОС: POST /api/users/authenticate без пароля
                String body = request.getReader().lines().collect(Collectors.joining());
                logger.fine("Получено тело запроса аутентификации: " + body);

                Map<String, String> authRequest = objectMapper.readValue(body, Map.class);
                String name = authRequest.get("name");
                String password = authRequest.get("password");

                if (name == null || password == null) {
                    // ❌ ПЛОХОЙ ОТВЕТ: Отсутствуют обязательные поля
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = createErrorResponse(httpStatus, "Поля 'name' и 'password' обязательны");
                } else {
                    boolean authenticated = authService.authenticateUser(name, password);

                    if (authenticated) {
                        Optional<User> userOpt = userRepository.findByName(name);
                        if (userOpt.isPresent()) {
                            // ✅ ХОРОШИЙ ОТВЕТ: Успешная аутентификация
                            UserResponseDTO userResponse = transformService.toResponseDTO(userOpt.get());
                            httpStatus = HttpServletResponse.SC_OK;
                            responseData = createSuccessResponse(userResponse);
                            logger.info("Успешная аутентификация пользователя: " + name);
                        } else {
                            // ❌ ПЛОХОЙ ОТВЕТ: Несоответствие данных
                            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                            responseData = createErrorResponse(httpStatus, "Ошибка аутентификации: пользователь не найден");
                            logger.severe("Пользователь " + name + " аутентифицирован, но не найден в БД");
                        }
                    } else {
                        // ❌ ПЛОХОЙ ОТВЕТ: Неверные учетные данные
                        httpStatus = HttpServletResponse.SC_UNAUTHORIZED;
                        responseData = createErrorResponse(httpStatus, "Неверные имя пользователя или пароль");
                        logger.warning("Неудачная аутентификация для пользователя: " + name);
                    }
                }
            } else if (pathInfo == null || pathInfo.equals("/")) {
                // ✅ ХОРОШИЙ ЗАПРОС: POST /api/users с корректными данными
                // ❌ ПЛОХОЙ ЗАПРОС: POST /api/users с существующим именем
                // ❌ ПЛОХОЙ ЗАПРОС: POST /api/users с невалидными данными
                String body = request.getReader().lines().collect(Collectors.joining());
                logger.fine("Получено тело запроса: " + body);

                CreateUserRequest userRequest = null;
                try {
                    userRequest = objectMapper.readValue(body, CreateUserRequest.class);
                    logger.info("JSON успешно преобразован в CreateUserRequest: " + userRequest);
                } catch (Exception e) {
                    // ❌ ПЛОХОЙ ОТВЕТ: Неверный JSON
                    logger.warning("Ошибка парсинга JSON: " + e.getMessage());
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = createErrorResponse(httpStatus, "Неверный формат JSON");
                }

                if (userRequest != null) {
                    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);
                    if (!violations.isEmpty()) {
                        // ❌ ПЛОХОЙ ОТВЕТ: Ошибки валидации
                        httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                        responseData = createValidationErrorResponse(violations);
                        logger.warning("Ошибки валидации: " + responseData);
                    } else {
                        String name = userRequest.getName();
                        String password = userRequest.getPassword();

                        if (userRepository.userNameExists(name)) {
                            // ❌ ПЛОХОЙ ОТВЕТ: Конфликт имен
                            httpStatus = HttpServletResponse.SC_CONFLICT;
                            responseData = createErrorResponse(httpStatus, "Пользователь с таким именем уже существует");
                            logger.warning("Попытка создания пользователя с существующим именем: " + name);
                        } else {
                            // ✅ ХОРОШИЙ ОТВЕТ: Успешное создание
                            authService.registerUser(name, password);
                            Optional<User> createdUser = userRepository.findByName(name);

                            if (createdUser.isPresent()) {
                                UserResponseDTO userResponse = transformService.toResponseDTO(createdUser.get());
                                httpStatus = HttpServletResponse.SC_CREATED;
                                responseData = createSuccessResponse(userResponse);
                                logger.info("Создан новый пользователь: " + name + " с ID: " + createdUser.get().getId());
                            } else {
                                throw new SQLException("Пользователь не найден после создания");
                            }
                        }
                    }
                }
            } else {
                // ❌ ПЛОХОЙ ЗАПРОС: POST /api/users/invalid-path
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = createErrorResponse(httpStatus, "Неверный формат URL");
            }
        } catch (SQLException e) {
            // ❌ ПЛОХОЙ ОТВЕТ: Ошибка БД
            logger.severe("Ошибка при работе с БД: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = createErrorResponse(httpStatus, "Ошибка при работе с базой данных");
        } catch (Exception e) {
            // ❌ ПЛОХОЙ ОТВЕТ: Неожиданная ошибка
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = createErrorResponse(httpStatus, "Внутренняя ошибка сервера");
        }

        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseData);

        long endTime = System.currentTimeMillis();
        logger.info("POST запрос обработан за " + (endTime - startTime) + " мс");
    }

    // ==================== ОБРАБОТКА PUT ЗАПРОСОВ ====================

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
                // ❌ ПЛОХОЙ ЗАПРОС: PUT /api/users без ID
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = createErrorResponse(httpStatus, "ID пользователя обязателен");
            } else {
                String idStr = pathInfo.substring(1);
                Long id;
                try {
                    id = Long.parseLong(idStr);
                } catch (NumberFormatException e) {
                    // ❌ ПЛОХОЙ ОТВЕТ: Неверный формат ID
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = createErrorResponse(httpStatus, "Неверный формат ID");
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
                    // ❌ ПЛОХОЙ ОТВЕТ: Неверный JSON
                    logger.warning("Ошибка парсинга JSON для обновления: " + e.getMessage());
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = createErrorResponse(httpStatus, "Неверный формат JSON");
                }

                if (updateRequest != null) {
                    Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(updateRequest);
                    if (!violations.isEmpty()) {
                        // ❌ ПЛОХОЙ ОТВЕТ: Ошибки валидации
                        httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                        responseData = createValidationErrorResponse(violations);
                        logger.warning("Ошибки валидации при обновлении: " + responseData);
                    } else {
                        Optional<User> existingUserOpt = userRepository.findById(id);
                        if (existingUserOpt.isEmpty()) {
                            // ❌ ПЛОХОЙ ОТВЕТ: Пользователь не найден
                            httpStatus = HttpServletResponse.SC_NOT_FOUND;
                            responseData = createErrorResponse(httpStatus, "Пользователь не найден");
                            logger.warning("Попытка обновления несуществующего пользователя с ID: " + id);
                        } else {
                            User userToUpdate = existingUserOpt.get();

                            if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty()) {
                                String newName = updateRequest.getName().trim();
                                if (!userToUpdate.getName().equals(newName) && userRepository.userNameExists(newName)) {
                                    // ❌ ПЛОХОЙ ОТВЕТ: Конфликт имен
                                    httpStatus = HttpServletResponse.SC_CONFLICT;
                                    responseData = createErrorResponse(httpStatus, "Пользователь с таким именем уже существует");
                                } else {
                                    userToUpdate.setName(newName);
                                }
                            }

                            if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
                                String hashedPassword = authService.hashPassword(updateRequest.getPassword());
                                userToUpdate.setPasswordHash(hashedPassword);
                            }

                            if (httpStatus == HttpServletResponse.SC_OK) {
                                boolean updated = userRepository.updateUser(userToUpdate);

                                if (updated) {
                                    // ✅ ХОРОШИЙ ОТВЕТ: Успешное обновление
                                    UserResponseDTO responseDto = transformService.toResponseDTO(userToUpdate);
                                    responseData = createSuccessResponse(responseDto);
                                    logger.info("Пользователь обновлен: " + userToUpdate.getId());
                                } else {
                                    // ❌ ПЛОХОЙ ОТВЕТ: Ошибка обновления
                                    httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                                    responseData = createErrorResponse(httpStatus, "Ошибка при обновлении пользователя");
                                    logger.warning("Ошибка при обновлении пользователя с ID: " + id);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // ❌ ПЛОХОЙ ОТВЕТ: Ошибка БД
            logger.severe("Ошибка при обновлении пользователя: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = createErrorResponse(httpStatus, "Ошибка при обновлении пользователя");
        } catch (Exception e) {
            // ❌ ПЛОХОЙ ОТВЕТ: Неожиданная ошибка
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = createErrorResponse(httpStatus, "Внутренняя ошибка сервера");
        }

        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseData);

        long endTime = System.currentTimeMillis();
        logger.info("PUT запрос обработан за " + (endTime - startTime) + " мс");
    }

    // ==================== ОБРАБОТКА DELETE ЗАПРОСОВ ====================

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
                // ❌ ПЛОХОЙ ЗАПРОС: DELETE /api/users без ID
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = createErrorResponse(httpStatus, "ID пользователя обязателен");
            } else {
                String idStr = pathInfo.substring(1);
                Long id;
                try {
                    id = Long.parseLong(idStr);
                } catch (NumberFormatException e) {
                    // ❌ ПЛОХОЙ ОТВЕТ: Неверный формат ID
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = createErrorResponse(httpStatus, "Неверный формат ID");
                    response.setStatus(httpStatus);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(responseData);
                    return;
                }

                boolean deleted = userRepository.deleteUser(id);

                if (deleted) {
                    // ✅ ХОРОШИЙ ОТВЕТ: Успешное удаление
                    responseData = createSuccessResponse("Пользователь удален");
                    logger.info("Удален пользователь с ID: " + id);
                } else {
                    // ❌ ПЛОХОЙ ОТВЕТ: Пользователь не найден
                    httpStatus = HttpServletResponse.SC_NOT_FOUND;
                    responseData = createErrorResponse(httpStatus, "Пользователь не найден");
                    logger.warning("Попытка удаления несуществующего пользователя с ID: " + id);
                }
            }
        } catch (SQLException e) {
            // ❌ ПЛОХОЙ ОТВЕТ: Ошибка БД
            logger.severe("Ошибка при удалении пользователя: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = createErrorResponse(httpStatus, "Ошибка при удалении пользователя");
        } catch (Exception e) {
            // ❌ ПЛОХОЙ ОТВЕТ: Неожиданная ошибка
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = createErrorResponse(httpStatus, "Внутренняя ошибка сервера");
        }

        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseData);

        long endTime = System.currentTimeMillis();
        logger.info("DELETE запрос обработан за " + (endTime - startTime) + " мс");
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private Optional<String> getUserByIdOpt(String idStr) {
        try {
            Long id = Long.parseLong(idStr);
            Optional<User> userOpt = userRepository.findById(id);

            if (userOpt.isPresent()) {
                UserResponseDTO responseDto = transformService.toResponseDTO(userOpt.get());
                logger.info("Найден пользователь по ID: " + id);
                // ✅ ХОРОШИЙ ОТВЕТ: Используем стандартный формат успеха
                return Optional.of(createSuccessResponse(responseDto));
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
                return userToJsonResponse(userOpt.get());
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