package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.FunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import model.entity.Function;
import model.entity.User;
import model.service.FunctionService;

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

@WebServlet("/api/functions/*")
public class FunctionServlet extends AuthServlet {
    private static final Logger logger = Logger.getLogger(FunctionServlet.class.getName());
    private final FunctionService functionService;
    private final ObjectMapper objectMapper;

    public FunctionServlet() {
        this.functionService = new FunctionService();
        this.objectMapper = new ObjectMapper();
    }

    public FunctionServlet(FunctionService functionService) {
        this.functionService = functionService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAuthenticated(req)) {
            sendUnauthorized(resp);
            return;
        }

        if (!isAdmin(req)) {
            sendForbidden(resp);
            return;
        }

        logger.info("POST /api/functions вызван");

        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        try {
            FunctionRequestDTO functionRequest = objectMapper.readValue(jsonBuffer.toString(), FunctionRequestDTO.class);
            FunctionResponseDTO response = functionService.createFunction(functionRequest);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(response));
        } catch (SQLException e) {
            logger.severe("Ошибка при создании функции: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при создании функции\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAuthenticated(req)) {
            sendUnauthorized(resp);
            return;
        }

        User user = getAuthenticatedUser(req);
        String pathInfo = req.getPathInfo();
        String userIdParam = req.getParameter("userId");

        if (userIdParam == null || userIdParam.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Параметр userId обязателен\"}");
            return;
        }

        Long userId = Long.parseLong(userIdParam);

        // USER может запрашивать только свои функции
        if (isUser(req) && !user.getId().equals(userId)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Users may access only their own functions\"}");
            return;
        }

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<FunctionResponseDTO> functions = functionService.getFunctionsByUserId(userId);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write(objectMapper.writeValueAsString(functions));
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    Long functionId = Long.parseLong(pathParts[1]);
                    Optional<FunctionResponseDTO> response = functionService.getFunctionById(functionId, userId);
                    if (response.isPresent()) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.setContentType("application/json");
                        resp.getWriter().write(objectMapper.writeValueAsString(response.get()));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"error\":\"Функция не найдена\"}");
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Неверный путь\"}");
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при получении функций: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при получении функций\"}");
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
            Function function = objectMapper.readValue(jsonBuffer.toString(), Function.class);
            boolean updated = functionService.updateFunction(function);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\":\"Функция обновлена\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Функция не найдена для обновления\"}");
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении функции: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при обновлении функции\"}");
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
        String userIdParam = req.getParameter("userId");
        if (userIdParam == null || userIdParam.isEmpty() || pathInfo == null || pathInfo.split("/").length != 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь или отсутствует userId\"}");
            return;
        }

        Long id = Long.parseLong(pathInfo.split("/")[1]);
        Long userId = Long.parseLong(userIdParam);

        try {
            boolean deleted = functionService.deleteFunction(id, userId);
            if (deleted) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\":\"Функция удалена\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Функция не найдена для удаления\"}");
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении функции: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при удалении функции\"}");
        }
    }
}