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

@WebServlet("/api/users/*")
public class UserServlet extends AuthServlet {
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
        if (!isAuthenticated(req)) {
            sendUnauthorized(resp);
            return;
        }

        if (!isAdmin(req)) {
            sendForbidden(resp);
            return;
        }

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null) {
                List<UserResponseDTO> users = userService.getAllUsers();
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write(objectMapper.writeValueAsString(users));
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
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
                } else if (pathParts.length == 3 && "name".equals(pathParts[1])) {
                    String userName = pathParts[2];
                    Optional<UserResponseDTO> user = userService.getUserByName(userName);
                    if (user.isPresent()) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.setContentType("application/json");
                        resp.getWriter().write(objectMapper.writeValueAsString(user.get()));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"error\":\"Пользователь не найден\"}");
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

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