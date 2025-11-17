package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.OperationRequestDTO;
import model.dto.response.OperationResponseDTO;
import model.entity.Operation;
import model.entity.User;
import model.service.OperationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet("/api/operations/*")
public class OperationServlet extends AuthServlet {
    private static final Logger logger = Logger.getLogger(OperationServlet.class.getName());
    private final OperationService operationService;
    private final ObjectMapper objectMapper;

    public OperationServlet() {
        this.operationService = new OperationService();
        this.objectMapper = new ObjectMapper();
    }

    public OperationServlet(OperationService operationService) {
        this.operationService = operationService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("resp", resp);

        if (!isAuthenticated(req)) {
            sendUnauthorized(resp);
            return;
        }

        super.service(req, resp);
    }

    private HttpServletResponse resp(HttpServletRequest req) {
        return (HttpServletResponse) req.getAttribute("resp");
    }

    private boolean checkOperationAccess(HttpServletRequest req, Long operationId) throws SQLException, IOException {
        User user = getAuthenticatedUser(req);

        if (isUser(req)) {
            boolean allowed = operationService.isOperationOwnedByUser(operationId, user.getId()); // Предполагаемый метод в Service
            if (!allowed) {
                sendError(resp(req), 403, "Users may access only their own operations");
                return false;
            }
        }
        return true;
    }

    private boolean checkFunctionAccess(HttpServletRequest req, Long functionId) throws SQLException, IOException {
        User user = getAuthenticatedUser(req);

        if (isUser(req)) {
            boolean allowed = operationService.isFunctionOwnedByUser(functionId, user.getId()); // Предполагаемый метод в Service
            if (!allowed) {
                sendError(resp(req), 403, "Users may access only operations of their own functions");
                return false;
            }
        }
        return true;
    }

    private void sendError(HttpServletResponse resp, int status, String errorMessage) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"error\":\"" + errorMessage + "\"}");
        }
    }

    protected void sendUnauthorized(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"error\":\"Unauthorized\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String functionIdParam = req.getParameter("functionId");

        if (functionIdParam == null || functionIdParam.isEmpty()) {
            logger.warning("Параметр functionId обязателен для GET запросов к операциям");
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
            return;
        }

        Long functionId;
        try {
            functionId = Long.parseLong(functionIdParam);
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат functionId: " + functionIdParam);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат functionId");
            return;
        }

        try {
            if (!checkFunctionAccess(req, functionId)) {
                return;
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при проверке доступа к функции: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при проверке доступа");
            return;
        }

        if (pathInfo == null) {
            logger.warning("Путь для GET запроса отсутствует. Используйте /api/operations/{id}");
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Путь для GET запроса отсутствует");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length == 2) {
            try {
                Long id = Long.parseLong(pathParts[1]);
                logger.info("GET /api/operations/" + id + " вызван для functionId: " + functionId);

                if (!checkOperationAccess(req, id)) {
                    return;
                }

                Optional<OperationResponseDTO> response = operationService.getOperationById(id, functionId);
                if (response.isPresent()) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json; charset=UTF-8");
                    PrintWriter out = resp.getWriter();
                    out.print(objectMapper.writeValueAsString(response.get()));
                    out.flush();
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Операция не найдена");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат ID операции: " + pathParts[1]);
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID операции");
            } catch (SQLException e) {
                logger.severe("Ошибка при получении операции: " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при получении операции");
            }
        } else {
            logger.warning("Неверный путь для GET: " + pathInfo);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный путь для GET");
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

            Long functionId = operationRequest.getFunctionId();
            if (functionId != null) {
                if (!checkFunctionAccess(req, functionId)) {
                    return;
                }
            } else {
                logger.warning("functionId отсутствует в запросе на создание операции");
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
                return;
            }

            OperationResponseDTO response = operationService.createOperation(operationRequest);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print(objectMapper.writeValueAsString(response));
            out.flush();
        } catch (IOException e) {
            logger.warning("Неверный формат JSON в теле запроса: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON");
        } catch (SQLException e) {
            logger.severe("Ошибка при создании операции: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при создании операции");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            logger.warning("Путь для PUT запроса отсутствует");
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Путь для PUT запроса отсутствует");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 2) {
            logger.warning("Неверный путь для PUT: " + pathInfo);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный путь для PUT");
            return;
        }

        try {
            Long id = Long.parseLong(pathParts[1]);

            if (!checkOperationAccess(req, id)) {
                return;
            }

            StringBuilder jsonBuffer = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }

            Operation operation = objectMapper.readValue(jsonBuffer.toString(), Operation.class);
            if (!id.equals(operation.getId())) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID в URL не совпадает с ID в теле запроса");
                return;
            }

            logger.info("PUT /api/operations/" + id + " вызван");

            boolean updated = operationService.updateOperation(operation);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json; charset=UTF-8"); // Указываем UTF-8
                resp.getWriter().write("{\"message\":\"Операция обновлена\"}");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Операция не найдена для обновления");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат ID операции: " + pathParts[1]);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID операции");
        } catch (IOException e) {
            logger.warning("Неверный формат JSON в теле PUT запроса: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON");
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении операции: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при обновлении операции");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String functionIdParam = req.getParameter("functionId");

        if (pathInfo == null) {
            logger.warning("Путь для DELETE запроса отсутствует");
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Путь для DELETE запроса отсутствует");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length == 2) { // ['', 'id'] -> DELETE /api/operations/{id}?functionId=1
            if (functionIdParam == null || functionIdParam.isEmpty()) {
                logger.warning("Параметр functionId обязателен для DELETE операции по ID");
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
                return;
            }

            try {
                Long id = Long.parseLong(pathParts[1]);
                Long functionId = Long.parseLong(functionIdParam);

                if (!checkFunctionAccess(req, functionId)) {
                    return;
                }

                if (!checkOperationAccess(req, id)) {
                    return;
                }

                logger.info("DELETE /api/operations/" + id + " вызван для functionId: " + functionId);

                boolean deleted = operationService.deleteOperation(id, functionId);
                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write("{\"message\":\"Операция удалена\"}");
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Операция не найдена для удаления");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат ID операции или functionId: " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID операции или functionId");
            } catch (SQLException e) {
                logger.severe("Ошибка при удалении операции: " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при удалении операции");
            }
        } else if (pathParts.length == 1 && pathParts[0].equals("")) { // '' -> DELETE /api/operations/function?functionId=1
            if (functionIdParam == null || functionIdParam.isEmpty()) {
                logger.warning("Параметр functionId обязателен для DELETE всех операций функции");
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
                return;
            }

            try {
                Long functionId = Long.parseLong(functionIdParam);

                if (!checkFunctionAccess(req, functionId)) {
                    return;
                }

                logger.info("DELETE /api/operations/function вызван для functionId: " + functionId);

                boolean deleted = operationService.deleteAllOperations(functionId);
                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write("{\"message\":\"Все операции функции удалены\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write("{\"message\":\"Запрос обработан, возможно, операций не было\"}");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат functionId: " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат functionId");
            } catch (SQLException e) {
                logger.severe("Ошибка при удалении всех операций функции: " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при удалении всех операций функции");
            }
        } else {
            logger.warning("Неверный путь для DELETE: " + pathInfo);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный путь для DELETE");
        }
    }
}