package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.FunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import model.entity.Function;
import model.service.FunctionService;

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

@WebServlet("/api/functions/*")
public class FunctionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(FunctionServlet.class.getName());
    private final FunctionService functionService;
    private final ObjectMapper objectMapper;

    public FunctionServlet() {
        this.functionService = new FunctionService();
        this.objectMapper = new ObjectMapper();
    }

    // Конструктор для тестирования
    public FunctionServlet(FunctionService functionService) {
        this.functionService = functionService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
            PrintWriter out = resp.getWriter();
            out.print(objectMapper.writeValueAsString(response));
            out.flush();
        } catch (SQLException e) {
            logger.severe("Ошибка при создании функции: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при создании функции\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String userIdParam = req.getParameter("userId");

        if (userIdParam == null || userIdParam.isEmpty()) {
            logger.warning("Параметр userId обязателен для GET запросов");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Параметр userId обязателен\"}");
            return;
        }

        Long userId = Long.parseLong(userIdParam);

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/functions?userId=1
            handleGetFunctionsByUserId(req, resp, userId);
        } else {
            // GET /api/functions/{id}?userId=1
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length == 2) { // ['', 'id']
                try {
                    Long id = Long.parseLong(pathParts[1]);
                    handleGetFunctionById(req, resp, id, userId);
                } catch (NumberFormatException e) {
                    logger.warning("Неверный формат ID функции: " + pathParts[1]);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Неверный формат ID функции\"}");
                }
            } else {
                logger.warning("Неверный путь: " + pathInfo);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный путь\"}");
            }
        }
    }

    private void handleGetFunctionById(HttpServletRequest req, HttpServletResponse resp, Long id, Long userId) throws IOException {
        logger.info("GET /api/functions/" + id + " вызван для userId: " + userId);
        try {
            Optional<FunctionResponseDTO> response = functionService.getFunctionById(id, userId);
            if (response.isPresent()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                PrintWriter out = resp.getWriter();
                out.print(objectMapper.writeValueAsString(response.get()));
                out.flush();
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Функция не найдена\"}");
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при получении функции: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при получении функции\"}");
        }
    }

    private void handleGetFunctionsByUserId(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        String nameParam = req.getParameter("name");
        String typeParam = req.getParameter("type");

        if (nameParam != null) {
            // GET /api/functions?userId=1&name=pattern
            logger.info("Поиск функций по имени для userId: " + userId + ", шаблон: " + nameParam);
            try {
                List<FunctionResponseDTO> functions = functionService.getFunctionsByName(userId, nameParam);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                PrintWriter out = resp.getWriter();
                out.print(objectMapper.writeValueAsString(functions));
                out.flush();
            } catch (SQLException e) {
                logger.severe("Ошибка при поиске функций по имени: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при поиске функций по имени\"}");
            }
        } else if (typeParam != null) {
            // GET /api/functions?userId=1&type=math
            logger.info("Получение функций по типу для userId: " + userId + ", тип: " + typeParam);
            try {
                List<FunctionResponseDTO> functions = functionService.getFunctionsByType(userId, typeParam);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                PrintWriter out = resp.getWriter();
                out.print(objectMapper.writeValueAsString(functions));
                out.flush();
            } catch (SQLException e) {
                logger.severe("Ошибка при получении функций по типу: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при получении функций по типу\"}");
            }
        } else {
            // GET /api/functions?userId=1
            logger.info("Получение всех функций для userId: " + userId);
            try {
                List<FunctionResponseDTO> functions = functionService.getFunctionsByUserId(userId);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                PrintWriter out = resp.getWriter();
                out.print(objectMapper.writeValueAsString(functions));
                out.flush();
            } catch (SQLException e) {
                logger.severe("Ошибка при получении функций пользователя: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при получении функций пользователя\"}");
            }
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

            Function function = objectMapper.readValue(jsonBuffer.toString(), Function.class);
            // Убедимся, что ID в сущности совпадает с URL

            logger.info("PUT /api/functions/" + id + " вызван");

            boolean updated = functionService.updateFunction(function);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\":\"Функция обновлена\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Функция не найдена для обновления\"}");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат ID функции: " + pathParts[1]);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат ID функции\"}");
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении функции: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при обновлении функции\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String userIdParam = req.getParameter("userId");

        if (userIdParam == null || userIdParam.isEmpty()) {
            logger.warning("Параметр userId обязателен для DELETE запроса");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Параметр userId обязателен\"}");
            return;
        }

        Long userId = Long.parseLong(userIdParam);

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
            logger.info("DELETE /api/functions/" + id + " вызван для userId: " + userId);

            boolean deleted = functionService.deleteFunction(id, userId);
            if (deleted) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\":\"Функция удалена\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Функция не найдена для удаления\"}");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат ID функции: " + pathParts[1]);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат ID функции\"}");
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении функции: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при удалении функции\"}");
        }
    }
}