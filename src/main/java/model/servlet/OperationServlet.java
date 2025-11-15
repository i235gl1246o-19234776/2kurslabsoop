package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.OperationRequestDTO;
import model.dto.response.OperationResponseDTO;
import model.entity.Operation;
import model.service.OperationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet("/api/operations/*")
public class OperationServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(OperationServlet.class.getName());
    private final OperationService operationService;
    private final ObjectMapper objectMapper;

    public OperationServlet() {
        this.operationService = new OperationService();
        this.objectMapper = new ObjectMapper();
    }

    // Конструктор для тестирования
    public OperationServlet(OperationService operationService) {
        this.operationService = operationService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String functionIdParam = req.getParameter("functionId");

        if (functionIdParam == null || functionIdParam.isEmpty()) {
            logger.warning("Параметр functionId обязателен для GET запросов к операциям");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Параметр functionId обязателен\"}");
            return;
        }

        Long functionId = Long.parseLong(functionIdParam);

        if (pathInfo == null) {
            logger.warning("Путь для GET запроса отсутствует. Используйте /api/operations/{id}");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Путь для GET запроса отсутствует\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length == 2) { // ['', 'id']
            try {
                Long id = Long.parseLong(pathParts[1]);
                logger.info("GET /api/operations/" + id + " вызван для functionId: " + functionId);

                Optional<OperationResponseDTO> response = operationService.getOperationById(id, functionId);
                if (response.isPresent()) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json");
                    PrintWriter out = resp.getWriter();
                    out.print(objectMapper.writeValueAsString(response.get()));
                    out.flush();
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Операция не найдена\"}");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат ID операции: " + pathParts[1]);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный формат ID операции\"}");
            } catch (SQLException e) {
                logger.severe("Ошибка при получении операции: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при получении операции\"}");
            }
        } else {
            logger.warning("Неверный путь для GET: " + pathInfo);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь для GET\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /api/operations вызван");

        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        try {
            OperationRequestDTO operationRequest = objectMapper.readValue(jsonBuffer.toString(), OperationRequestDTO.class);
            OperationResponseDTO response = operationService.createOperation(operationRequest);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.print(objectMapper.writeValueAsString(response));
            out.flush();
        } catch (IOException e) {
            logger.warning("Неверный формат JSON в теле запроса: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат JSON\"}");
        } catch (SQLException e) {
            logger.severe("Ошибка при создании операции: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при создании операции\"}");
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

            Operation operation = objectMapper.readValue(jsonBuffer.toString(), Operation.class);
            // Убедимся, что ID в сущности совпадает с URL

            logger.info("PUT /api/operations/" + id + " вызван");

            boolean updated = operationService.updateOperation(operation);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\":\"Операция обновлена\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Операция не найдена для обновления\"}");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат ID операции: " + pathParts[1]);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат ID операции\"}");
        } catch (IOException e) {
            logger.warning("Неверный формат JSON в теле PUT запроса: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат JSON\"}");
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении операции: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при обновлении операции\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String functionIdParam = req.getParameter("functionId");

        if (pathInfo == null) {
            logger.warning("Путь для DELETE запроса отсутствует");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Путь для DELETE запроса отсутствует\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length == 2) { // ['', 'id'] -> DELETE /api/operations/{id}?functionId=1
            if (functionIdParam == null || functionIdParam.isEmpty()) {
                logger.warning("Параметр functionId обязателен для DELETE операции по ID");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Параметр functionId обязателен\"}");
                return;
            }

            try {
                Long id = Long.parseLong(pathParts[1]);
                Long functionId = Long.parseLong(functionIdParam);

                logger.info("DELETE /api/operations/" + id + " вызван для functionId: " + functionId);

                boolean deleted = operationService.deleteOperation(id, functionId);
                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\":\"Операция удалена\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Операция не найдена для удаления\"}");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат ID операции или functionId: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный формат ID операции или functionId\"}");
            } catch (SQLException e) {
                logger.severe("Ошибка при удалении операции: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при удалении операции\"}");
            }
        } else if (pathParts.length == 1 && pathParts[0].equals("")) { // '' -> DELETE /api/operations/function?functionId=1
            if (functionIdParam == null || functionIdParam.isEmpty()) {
                logger.warning("Параметр functionId обязателен для DELETE всех операций функции");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Параметр functionId обязателен\"}");
                return;
            }

            try {
                Long functionId = Long.parseLong(functionIdParam);
                logger.info("DELETE /api/operations/function вызван для functionId: " + functionId);

                boolean deleted = operationService.deleteAllOperations(functionId);
                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\":\"Все операции функции удалены\"}");
                } else {
                    // deleteAllOperations может вернуть true даже если записей не было
                    // В данном случае, если логика возвращает false, можно интерпретировать как ошибку
                    // или просто как "ничего не удалено". Здесь мы интерпретируем как успех.
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\":\"Запрос обработан, возможно, операций не было\"}");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат functionId: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный формат functionId\"}");
            } catch (SQLException e) {
                logger.severe("Ошибка при удалении всех операций функции: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при удалении всех операций функции\"}");
            }
        } else {
            logger.warning("Неверный путь для DELETE: " + pathInfo);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь для DELETE\"}");
        }
    }
}