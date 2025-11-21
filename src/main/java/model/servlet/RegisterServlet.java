// src/main/java/model/servlet/RegisterServlet.java
package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.entity.User;
import model.entity.UserRole;
import model.service.AuthenticationService;
import repository.DatabaseConnection;
import repository.dao.UserRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {

    private AuthenticationService authenticationService;
    private UserRepository userRepository;
    private static final Logger logger = Logger.getLogger(RegisterServlet.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");
            DatabaseConnection connection = new DatabaseConnection();
            this.userRepository = new UserRepository(connection);
            this.authenticationService = new AuthenticationService(userRepository);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Ошибка инициализации RegisterServlet", e);
            throw new ServletException("Failed to initialize RegisterServlet", e);
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

            // Проверяем, существует ли пользователь
            if (userRepository.findByName(username).isPresent()) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                Map<String, String> error = new HashMap<>();
                error.put("message", "User with this name already exists");
                resp.getWriter().write(objectMapper.writeValueAsString(error));
                return;
            }

            // Создаём нового пользователя
            User newUser = new User();
            newUser.setName(username);
            newUser.setPasswordHash(password); // Убедитесь, что hashPassword безопасен
            newUser.setRole(UserRole.USER); // Назначаем роль по умолчанию

            // Сохраняем в БД
            userRepository.createUser(newUser);

            // Возвращаем успешный ответ (например, ID нового пользователя)
            Map<String, Object> response = new HashMap<>();
            response.put("id", newUser.getId());
            response.put("username", newUser.getName());
            response.put("role", newUser.getRole().toString());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(objectMapper.writeValueAsString(response));

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading request body", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid JSON format");
            resp.getWriter().write(objectMapper.writeValueAsString(error));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during registration", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Database error");
            resp.getWriter().write(objectMapper.writeValueAsString(error));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during registration", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Internal server error");
            resp.getWriter().write(objectMapper.writeValueAsString(error));
        }
    }
}