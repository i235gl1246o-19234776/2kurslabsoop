// src/main/java/model/servlet/LoginServlet.java
package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.entity.User;
import model.service.AuthenticationService;
import repository.DatabaseConnection;
import repository.dao.UserRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private AuthenticationService authenticationService;
    private UserRepository userRepository;
    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");
            DatabaseConnection connection = new DatabaseConnection();
            this.userRepository = new UserRepository(connection);
            this.authenticationService = new AuthenticationService(userRepository);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ошибка инициализации LoginServlet", e);
            throw new ServletException("Failed to initialize LoginServlet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // Читаем тело запроса
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = req.getReader().readLine()) != null) {
                buffer.append(line);
            }
            String body = buffer.toString();

            // Парсим JSON
            Map<String, String> requestData = objectMapper.readValue(body, Map.class);
            String username = requestData.get("username");
            String password = requestData.get("password");

            // Валидация (минимальная)
            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, String> error = new HashMap<>();
                error.put("message", "Username and password are required");
                resp.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }

            // Проверяем аутентификацию
            boolean isAuthenticated = authenticationService.authenticateUser(username, password);

            if (!isAuthenticated) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Map<String, String> error = new HashMap<>();
                error.put("message", "Invalid username or password");
                resp.getWriter().write(objectMapper.writeValueAsString(error));
                logger.info("Login failed for user: " + username);
                return;
            }

            // Получаем данные пользователя из БД
            Optional<User> userOpt = userRepository.findByName(username);
            if (userOpt.isEmpty()) {
                // Это редкий случай, но возможен
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, String> error = new HashMap<>();
                error.put("message", "User data not found after successful authentication");
                resp.getWriter().write(objectMapper.writeValueAsString(error));
                logger.severe("User data not found for authenticated user: " + username);
                return;
            }

            User user = userOpt.get();

            // Проверяем роль (если нужно)
            if (user.getRole() != model.entity.UserRole.ADMIN && user.getRole() != model.entity.UserRole.USER) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                Map<String, String> error = new HashMap<>();
                error.put("message", "Access denied: invalid role");
                resp.getWriter().write(objectMapper.writeValueAsString(error));
                logger.warning("Login failed for user with invalid role: " + username + ", role: " + user.getRole());
                return;
            }

            // --- УСПЕШНЫЙ ЛОГИН ---
            // В реальном приложении здесь создается сессия или JWT-токен.
            // В данном примере мы просто возвращаем информацию о пользователе.
            // Клиент может хранить эту информацию в состоянии (state) или сессии браузера.

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getName());
            response.put("role", user.getRole().toString());
            // Не включайте пароль или хеш пароля в ответ!

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(objectMapper.writeValueAsString(response));
            logger.info("User logged in successfully: " + username);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading request body", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid JSON format");
            resp.getWriter().write(objectMapper.writeValueAsString(error));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during login", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Database error");
            resp.getWriter().write(objectMapper.writeValueAsString(error));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during login", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Internal server error");
            resp.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }
}