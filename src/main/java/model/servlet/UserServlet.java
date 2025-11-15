package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.CreateUserRequest;
import model.dto.response.UserResponseDTO;
import model.entity.User;
import model.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UserServlet.class.getName());
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserServlet() {
        this.userService = new UserService();
        this.objectMapper = new ObjectMapper();
    }

    // Конструктор для тестирования
    public UserServlet(UserService userService) {
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            // GET /api/users
            logger.info("GET /api/users вызван");
            try {
                List<UserResponseDTO> users = userService.getAllUsers();

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                PrintWriter out = resp.getWriter();
                out.print(objectMapper.writeValueAsString(users));
                out.flush();
            } catch (SQLException e) {
                logger.severe("Ошибка при получении всех пользователей: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при получении всех пользователей\"}");
            }
        } else {
            String[] pathParts = pathInfo.split("/");

            if (pathParts.length == 2) { // ['', 'id'] -> GET /api/users/{id}
                try {
                    Long id = Long.parseLong(pathParts[1]);
                    logger.info("GET /api/users/" + id + " вызван");

                    Optional<UserResponseDTO> user = userService.getUserById(id);

                    if (user.isPresent()) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        out.print(objectMapper.writeValueAsString(user.get()));
                        out.flush();
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"error\":\"Пользователь не найден\"}");
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Неверный формат ID пользователя: " + pathParts[1]);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Неверный формат ID пользователя\"}");
                } catch (SQLException e) {
                    logger.severe("Ошибка при получении пользователя по ID: " + e.getMessage());
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("{\"error\":\"Ошибка при получении пользователя по ID\"}");
                }
            } else if (pathParts.length == 3) { // ['', 'name', 'userName'] -> GET /api/users/name/{name}
                if ("name".equals(pathParts[1])) {
                    String userName = pathParts[2]; // URL декодирование не требуется для простых имён
                    logger.info("GET /api/users/name/" + userName + " вызван");

                    try {
                        Optional<UserResponseDTO> user = userService.getUserByName(userName);

                        if (user.isPresent()) {
                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.setContentType("application/json");
                            PrintWriter out = resp.getWriter();
                            out.print(objectMapper.writeValueAsString(user.get()));
                            out.flush();
                        } else {
                            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            resp.getWriter().write("{\"error\":\"Пользователь не найден\"}");
                        }
                    } catch (SQLException e) {
                        logger.severe("Ошибка при получении пользователя по имени: " + e.getMessage());
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write("{\"error\":\"Ошибка при получении пользователя по имени\"}");
                    }
                } else if ("exist".equals(pathParts[1])) { // ['', 'exist', 'userName'] -> GET /api/users/exist/{name}
                    String userName = pathParts[2];
                    logger.info("GET /api/users/exist/" + userName + " вызван");

                    try {
                        boolean exists = userService.userNameExists(userName);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        out.print(objectMapper.writeValueAsString(new UserExistsResponse(exists)));
                        out.flush();
                    } catch (SQLException e) {
                        logger.severe("Ошибка при проверке существования пользователя: " + e.getMessage());
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        resp.getWriter().write("{\"error\":\"Ошибка при проверке существования пользователя\"}");
                    }
                } else {
                    logger.warning("Неверный путь для GET: " + pathInfo);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Неверный путь для GET\"}");
                }
            } else {
                logger.warning("Неверный путь для GET: " + pathInfo);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный путь для GET\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // POST /api/users
            logger.info("POST /api/users вызван");

            StringBuilder jsonBuffer = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }

            try {
                CreateUserRequest userRequest = objectMapper.readValue(jsonBuffer.toString(), CreateUserRequest.class);
                UserResponseDTO response = userService.createUser(userRequest);

                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.setContentType("application/json");
                PrintWriter out = resp.getWriter();
                out.print(objectMapper.writeValueAsString(response));
                out.flush();
            } catch (IOException e) {
                logger.warning("Неверный формат JSON в теле запроса: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный формат JSON\"}");
            } catch (IllegalArgumentException e) {
                // Обработка случая, когда пользователь уже существует
                logger.warning("Ошибка при создании пользователя: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            } catch (SQLException e) {
                logger.severe("Ошибка при создании пользователя: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при создании пользователя\"}");
            }
        } else if (pathInfo.equals("/authenticate")) {
            // POST /api/users/authenticate
            logger.info("POST /api/users/authenticate вызван");

            StringBuilder jsonBuffer = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }

            try {
                // Предположим, тело запроса содержит JSON с полями "name" и "passwordHash"
                AuthenticateRequest authRequest = objectMapper.readValue(jsonBuffer.toString(), AuthenticateRequest.class);

                boolean authenticated = userService.authenticateUser(authRequest.getName(), authRequest.getPasswordHash());
                if (authenticated) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\":\"Аутентификация успешна\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                    resp.getWriter().write("{\"error\":\"Неверные имя пользователя или пароль\"}");
                }
            } catch (IOException e) {
                logger.warning("Неверный формат JSON в теле запроса аутентификации: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный формат JSON\"}");
            } catch (SQLException e) {
                logger.severe("Ошибка при аутентификации пользователя: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при аутентификации пользователя\"}");
            }
        } else {
            logger.warning("Неверный путь для POST: " + pathInfo);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь для POST\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            logger.warning("Путь для PUT запроса отсутствует");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Путь для PUT запроса отсутствует\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 2) { // ['', 'id']
            logger.warning("Неверный путь для PUT: " + pathInfo);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь для PUT\"}");
            return;
        }

        try {
            Long id = Long.parseLong(pathParts[1]);

            StringBuilder jsonBuffer = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }

            User user = objectMapper.readValue(jsonBuffer.toString(), User.class);
            // Убедимся, что ID в сущности совпадает с URL

            logger.info("PUT /api/users/" + id + " вызван");

            boolean updated = userService.updateUser(user);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\":\"Пользователь обновлён\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Пользователь не найден для обновления\"}");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат ID пользователя: " + pathParts[1]);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат ID пользователя\"}");
        } catch (IOException e) {
            logger.warning("Неверный формат JSON в теле PUT запроса: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат JSON\"}");
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении пользователя: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при обновлении пользователя\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            logger.warning("Путь для DELETE запроса отсутствует");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Путь для DELETE запроса отсутствует\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 2) { // ['', 'id']
            logger.warning("Неверный путь для DELETE: " + pathInfo);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь для DELETE\"}");
            return;
        }

        try {
            Long id = Long.parseLong(pathParts[1]);
            logger.info("DELETE /api/users/" + id + " вызван");

            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\":\"Пользователь удалён\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Пользователь не найден для удаления\"}");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат ID пользователя: " + pathParts[1]);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат ID пользователя\"}");
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении пользователя: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при удалении пользователя\"}");
        }
    }

    // Вспомогательный класс для ответа на /exist
    private static class UserExistsResponse {
        private final boolean exists;

        public UserExistsResponse(boolean exists) {
            this.exists = exists;
        }

        public boolean isExists() {
            return exists;
        }
    }

    // Вспомогательный класс для тела запроса аутентификации
    private static class AuthenticateRequest {
        private String name;
        private String passwordHash;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public void setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
        }
    }
}