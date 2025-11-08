package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Operation;
import model.dto.request.OperationRequestDTO;
import model.dto.response.OperationResponseDTO;
import repository.OperationRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet("/api/operations/*")
public class OperationServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(OperationServlet.class.getName());
    private OperationRepository operationRepository;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        try {
            this.operationRepository = new OperationRepository();
            this.objectMapper = new ObjectMapper();
            logger.info("OperationServlet инициализирован");
        } catch (Exception e) {
            logger.severe("Ошибка инициализации OperationServlet: " + e.getMessage());
            throw new ServletException("Не удалось инициализировать OperationServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // GET /api/operations/{id} - получить операцию по ID
                getOperationById(req, resp, pathInfo);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден. Используйте /api/operations/{id}");
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/operations - создать новую операцию
                createOperation(req, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден");
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // PUT /api/operations/{id} - обновить операцию
                updateOperation(req, resp, pathInfo);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден");
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // DELETE /api/operations/{id} - удалить операцию
                deleteOperation(req, resp, pathInfo);
            } else if (pathInfo != null && pathInfo.equals("/function")) {
                // DELETE /api/operations/function?functionId=123 - удалить все операции функции
                deleteAllOperationsForFunction(req, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден");
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    private void getOperationById(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, SQLException {
        String functionIdParam = req.getParameter("functionId");

        if (functionIdParam == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
            return;
        }

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            Long functionId = Long.parseLong(functionIdParam);

            Optional<Operation> operation = operationRepository.findById(id, functionId);
            if (operation.isPresent()) {
                OperationResponseDTO responseDTO = convertToResponseDTO(operation.get());
                String jsonResponse = objectMapper.writeValueAsString(responseDTO);
                resp.getWriter().write(jsonResponse);
                logger.info("Возвращена операция с ID: " + id);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Операция не найдена");
            }

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID или functionId");
        }
    }

    private void createOperation(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        try {
            OperationRequestDTO requestDTO = objectMapper.readValue(req.getReader(), OperationRequestDTO.class);

            // Валидация
            if (requestDTO.getFunctionId() == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "functionId обязателен");
                return;
            }
            if (requestDTO.getOperationsTypeId() == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "operationsTypeId обязателен");
                return;
            }

            Operation operation = convertToEntity(requestDTO);
            Long operationId = operationRepository.createOperation(operation);

            ApiResponse<Long> apiResponse = new ApiResponse<>(true, "Операция успешно создана", operationId);
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(jsonResponse);
            logger.info("Создана новая операция с ID: " + operationId);

        } catch (Exception e) {
            logger.severe("Ошибка при создании операции: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат данных");
        }
    }

    private void updateOperation(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, SQLException {
        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            OperationRequestDTO requestDTO = objectMapper.readValue(req.getReader(), OperationRequestDTO.class);

            if (requestDTO.getFunctionId() == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "functionId обязателен");
                return;
            }

            // Проверяем существование операции
            Optional<Operation> existingOperation = operationRepository.findById(id, requestDTO.getFunctionId());
            if (!existingOperation.isPresent()) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Операция не найдена");
                return;
            }

            Operation operation = convertToEntity(requestDTO);
            operation.setId(id);

            boolean updated = operationRepository.updateOperation(operation);
            if (updated) {
                ApiResponse<String> apiResponse = new ApiResponse<>(true, "Операция успешно обновлена", null);
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                resp.getWriter().write(jsonResponse);
                logger.info("Обновлена операция с ID: " + id);
            } else {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Не удалось обновить операцию");
            }

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID");
        } catch (Exception e) {
            logger.severe("Ошибка при обновлении операции: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат данных");
        }
    }

    private void deleteOperation(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, SQLException {
        String functionIdParam = req.getParameter("functionId");

        if (functionIdParam == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
            return;
        }

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            Long functionId = Long.parseLong(functionIdParam);

            boolean deleted = operationRepository.deleteOperation(id, functionId);
            if (deleted) {
                ApiResponse<String> apiResponse = new ApiResponse<>(true, "Операция успешно удалена", null);
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                resp.getWriter().write(jsonResponse);
                logger.info("Удалена операция с ID: " + id);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Операция не найдена");
            }

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID или functionId");
        }
    }

    private void deleteAllOperationsForFunction(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String functionIdParam = req.getParameter("functionId");

        if (functionIdParam == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
            return;
        }

        try {
            Long functionId = Long.parseLong(functionIdParam);

            boolean deleted = operationRepository.deleteAllOperations(functionId);
            if (deleted) {
                ApiResponse<String> apiResponse = new ApiResponse<>(true, "Все операции для функции успешно удалены", null);
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                resp.getWriter().write(jsonResponse);
                logger.info("Удалены все операции для функции с ID: " + functionId);
            } else {
                ApiResponse<String> apiResponse = new ApiResponse<>(true, "Операции для функции не найдены", null);
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                resp.getWriter().write(jsonResponse);
            }

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат functionId");
        }
    }

    // Вспомогательные методы
    private Operation convertToEntity(OperationRequestDTO dto) {
        Operation operation = new Operation();
        operation.setFunctionId(dto.getFunctionId());
        operation.setOperationsTypeId(dto.getOperationsTypeId());
        return operation;
    }

    private OperationResponseDTO convertToResponseDTO(Operation operation) {
        return new OperationResponseDTO(
                operation.getId(),
                operation.getFunctionId(),
                operation.getOperationsTypeId()
        );
    }

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        ApiResponse<String> errorResponse = new ApiResponse<>(false, message, null);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        resp.getWriter().write(jsonResponse);
    }

    // Внутренний класс для стандартизированного ответа
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        // геттеры и сеттеры
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }
}