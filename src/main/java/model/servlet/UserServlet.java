// model/servlet/UserServlet.java
package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.CreateUserRequest;
import model.dto.response.UserResponseDTO;
import model.entity.User;
import model.entity.UserRole;
import model.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

// ИЗМЕНЕНО: Убираем наследование от AuthServlet, т.к. аутентификация не требует аутентификации
@WebServlet("/api/users/*")
public class UserServlet extends AuthServlet { // Или просто HttpServlet, если не наследуетесь от AuthServlet для других целей

    private static final Logger logger = Logger.getLogger(UserServlet.class.getName());
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserServlet() {
        this.userService = new UserService();
        this.objectMapper = new ObjectMapper();
    }

    public UserServlet(UserService userService) {
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // --- ИЗМЕНЕНО: Проверка аутентификации теперь только для не-аутентификационных путей ---
        String pathInfo = req.getPathInfo();

        // Проверяем, является ли запрос аутентификационным
        if (pathInfo != null && pathInfo.startsWith("/name/")) {
            handleAuthentication(req, resp, pathInfo);
            return;
        }

        // Для остальных GET-запросов (например, /api/users, /api/users/{id}) требуется аутентификация администратора
        if (!isAuthenticated(req)) {
            sendUnauthorized(resp);
            return;
        }

        if (!isAdmin(req)) {
            sendForbidden(resp);
            return;
        }

        try {
            if (pathInfo == null) {
                List<UserResponseDTO> users = userService.getAllUsers();
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write(objectMapper.writeValueAsString(users));
            } else {
                String[] pathParts = pathInfo.split("/", 0); // 0 значит не ограничивать количество
                if (pathParts.length > 1) {
                    try {
                        Long id = Long.parseLong(pathParts[1]);
                        Optional<UserResponseDTO> user = userService.getUserById(id);
                        if (user.isPresent()) {
                            resp.setStatus(HttpServletResponse.SC_OK);
                            resp.setContentType("application/json");
                            resp.getWriter().write(objectMapper.writeValueAsString(user.get()));
                        } else {
                            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            resp.getWriter().write("{\"error\":\"Пользователь не найден\"}");
                        }
                    } catch (NumberFormatException e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write("{\"error\":\"Неверный ID пользователя\"}");
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Неверный путь для GET\"}");
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при получении пользователей: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при получении пользователей\"}");
        }
    }

    private void handleAuthentication(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        String[] pathParts = pathInfo.split("/", 0);
        if (pathParts.length < 3) { // Ожидаем "/name/someuser"
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь для аутентификации\"}");
            return;
        }
        String username = pathParts[2];

        // Получаем пароль из заголовка Authorization (Basic Auth)
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Authorization header required\"}");
            return;
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials;
        try {
            credentials = new String(java.util.Base64.getDecoder().decode(base64Credentials), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Invalid Base64 encoding\"}");
            return;
        }

        String[] values = credentials.split(":", 2);
        if (values.length != 2) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Invalid credentials format\"}");
            return;
        }

        String inputUsername = values[0].trim();
        String inputPassword = values[1].trim();

        // Проверяем, совпадает ли имя в URL с именем в заголовке
        if (!inputUsername.equals(username)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Username in URL does not match Authorization header\"}");
            return;
        }

        if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Username and password required\"}");
            return;
        }

        try {
            // ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД
            Optional<UserResponseDTO> userOpt = userService.authenticateAndReturnUser(inputUsername, inputPassword);

            if (userOpt.isPresent()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write(objectMapper.writeValueAsString(userOpt.get()));
                logger.info("User authenticated successfully: " + inputUsername);
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"error\":\"Invalid username or password\"}");
                logger.info("Authentication failed for user: " + inputUsername);
            }
        } catch (SQLException e) {
            logger.severe("Database error during authentication: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Database error\"}");
        }
    }
    // --- КОНЕЦ НОВОГО МЕТОДА ---

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Регистрация не требует аутентификации
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
            resp.getWriter().write(objectMapper.writeValueAsString(response));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при создании пользователя\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAuthenticated(req)) {
            sendUnauthorized(resp);
            return;
        }

        if (!isAdmin(req)) {
            sendForbidden(resp);
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.split("/").length != 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь для PUT\"}");
            return;
        }

        Long id = Long.parseLong(pathInfo.split("/")[1]);
        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        try {
            User user = objectMapper.readValue(jsonBuffer.toString(), User.class);
            boolean updated = userService.updateUser(user);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\":\"Пользователь обновлён\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Пользователь не найден для обновления\"}");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при обновлении пользователя\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAuthenticated(req)) {
            sendUnauthorized(resp);
            return;
        }

        if (!isAdmin(req)) {
            sendForbidden(resp);
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.split("/").length != 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь для DELETE\"}");
            return;
        }

        Long id = Long.parseLong(pathInfo.split("/")[1]);

        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\":\"Пользователь удалён\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Пользователь не найден для удаления\"}");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при удалении пользователя\"}");
        }
    }
}