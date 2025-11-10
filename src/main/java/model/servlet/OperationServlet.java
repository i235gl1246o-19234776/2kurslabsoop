package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.entity.Operation;
import model.dto.request.OperationRequestDTO;
import model.dto.response.OperationResponseDTO;
import repository.OperationRepository;
import model.dto.DTOTransformService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/api/operations/*")
public class OperationServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(OperationServlet.class.getName());
    private OperationRepository operationRepository;
    private DTOTransformService transformService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        try {
            this.operationRepository = new OperationRepository();
            this.transformService = new DTOTransformService();
            this.objectMapper = new ObjectMapper();
            logger.info("OperationServlet инициализирован с DTOTransformService");
        } catch (Exception e) {
            logger.severe("Ошибка инициализации OperationServlet: " + e.getMessage());
            throw new ServletException("Не удалось инициализировать OperationServlet", e);
        }
    }

    // ==================== УТИЛИТНЫЕ МЕТОДЫ ====================

    private String createSuccessResponse(Object data) {
        return convertToJson(Map.of("success", true, "data", data));
    }

    private String createSuccessResponse(String message) {
        return convertToJson(Map.of("success", true, "message", message));
    }

    private String createErrorResponse(int status, String errorMessage) {
        return convertToJson(Map.of(
                "success", false,
                "error", errorMessage,
                "status", status
        ));
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.severe("Ошибка преобразования в JSON: " + e.getMessage());
            return "{\"success\":false,\"error\":\"Ошибка сервера\",\"status\":500}";
        }
    }

    private void sendJsonResponse(HttpServletResponse response, int status, String jsonData) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonData);
    }

    private Optional<Long> parseLongParameter(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Optional.of(Long.parseLong(value.trim()));
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат параметра " + paramName + ": " + value);
            }
        }
        return Optional.empty();
    }

    private Optional<Long> parseIdFromPath(String pathInfo) {
        try {
            return Optional.of(Long.parseLong(pathInfo.substring(1)));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    private <T> T parseJsonBody(String body, Class<T> valueType) {
        try {
            return objectMapper.readValue(body, valueType);
        } catch (Exception e) {
            logger.warning("Ошибка парсинга JSON: " + e.getMessage());
            return null;
        }
    }

    // ==================== ОБРАБОТКА GET ЗАПРОСОВ ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("GET операция: " + req.getRequestURI());

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // ✅ ХОРОШИЙ ЗАПРОС: GET /api/operations/1?functionId=1
                // ❌ ПЛОХОЙ ЗАПРОС: GET /api/operations/abc?functionId=1
                getOperationById(req, resp, pathInfo);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND,
                                "Ресурс не найден. Используйте /api/operations/{id}"));
            }
        } catch (SQLException e) {
            logger.severe("Ошибка БД при получении операции: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("GET операция обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    private void getOperationById(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws IOException, SQLException {

        Optional<Long> functionIdOpt = parseLongParameter(req, "functionId");
        if (functionIdOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен"));
            return;
        }

        Optional<Long> idOpt = parseIdFromPath(pathInfo);
        if (idOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID операции"));
            return;
        }

        Long id = idOpt.get();
        Long functionId = functionIdOpt.get();

        Optional<Operation> operation = operationRepository.findById(id, functionId);
        if (operation.isPresent()) {
            // ✅ Используем transformService для преобразования Entity в DTO
            OperationResponseDTO responseDTO = transformService.toResponseDTO(operation.get());
            sendJsonResponse(resp, HttpServletResponse.SC_OK, createSuccessResponse(responseDTO));
            logger.info("Возвращена операция с ID: " + id);
        } else {
            sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Операция не найдена"));
        }
    }

    // ==================== ОБРАБОТКА POST ЗАПРОСОВ ====================

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("POST создание операции");

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // ✅ ХОРОШИЙ ЗАПРОС: POST /api/operations с корректным JSON
                // ❌ ПЛОХОЙ ЗАПРОС: POST /api/operations с невалидным JSON
                createOperation(req, resp);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден"));
            }
        } catch (SQLException e) {
            logger.severe("Ошибка БД при создании операции: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("POST операция обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    private void createOperation(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String body = req.getReader().lines().collect(Collectors.joining());
        logger.fine("Тело запроса создания: " + body);

        OperationRequestDTO requestDTO = parseJsonBody(body, OperationRequestDTO.class);
        if (requestDTO == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON"));
            return;
        }

        // Валидация обязательных полей
        if (requestDTO.getFunctionId() == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "functionId обязателен"));
            return;
        }
        if (requestDTO.getOperationsTypeId() == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "operationsTypeId обязателен"));
            return;
        }

        // ✅ Используем transformService для преобразования DTO в Entity
        Operation operation = transformService.toEntity(requestDTO);
        Long operationId = operationRepository.createOperation(operation);

        if (operationId != null) {
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
                    createSuccessResponse(Map.of("id", operationId, "message", "Операция успешно создана")));
            logger.info("Создана новая операция с ID: " + operationId);
        } else {
            throw new SQLException("Не удалось создать операцию");
        }
    }

    // ==================== ОБРАБОТКА PUT ЗАПРОСОВ ====================

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("PUT обновление операции");

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // ✅ ХОРОШИЙ ЗАПРОС: PUT /api/operations/1 с корректным JSON
                // ❌ ПЛОХОЙ ЗАПРОС: PUT /api/operations/999 (несуществующий ID)
                updateOperation(req, resp, pathInfo);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден"));
            }
        } catch (SQLException e) {
            logger.severe("Ошибка БД при обновлении операции: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("PUT операция обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    private void updateOperation(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws IOException, SQLException {

        Optional<Long> idOpt = parseIdFromPath(pathInfo);
        if (idOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID операции"));
            return;
        }

        Long id = idOpt.get();
        String body = req.getReader().lines().collect(Collectors.joining());
        logger.fine("Тело запроса обновления: " + body);

        OperationRequestDTO requestDTO = parseJsonBody(body, OperationRequestDTO.class);
        if (requestDTO == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON"));
            return;
        }

        if (requestDTO.getFunctionId() == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "functionId обязателен"));
            return;
        }

        // Проверяем существование операции
        Optional<Operation> existingOperation = operationRepository.findById(id, requestDTO.getFunctionId());
        if (existingOperation.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Операция не найдена"));
            return;
        }

        // ✅ Используем transformService для преобразования DTO в Entity
        Operation operation = transformService.toEntity(requestDTO);
        operation.setId(id);

        boolean updated = operationRepository.updateOperation(operation);
        if (updated) {
            OperationResponseDTO responseDTO = transformService.toResponseDTO(operation);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, createSuccessResponse(responseDTO));
            logger.info("Обновлена операция с ID: " + id);
        } else {
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Не удалось обновить операцию"));
        }
    }

    // ==================== ОБРАБОТКА DELETE ЗАПРОСОВ ====================

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("DELETE операция: " + req.getRequestURI());

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // ✅ ХОРОШИЙ ЗАПРОС: DELETE /api/operations/1?functionId=1
                deleteOperation(req, resp, pathInfo);
            } else if (pathInfo != null && pathInfo.equals("/function")) {
                // ✅ ХОРОШИЙ ЗАПРОС: DELETE /api/operations/function?functionId=1
                // ❌ ПЛОХОЙ ЗАПРОС: DELETE /api/operations/function (без functionId)
                deleteAllOperationsForFunction(req, resp);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден"));
            }
        } catch (SQLException e) {
            logger.severe("Ошибка БД при удалении: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("DELETE операция обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    private void deleteOperation(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws IOException, SQLException {

        Optional<Long> functionIdOpt = parseLongParameter(req, "functionId");
        if (functionIdOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен"));
            return;
        }

        Optional<Long> idOpt = parseIdFromPath(pathInfo);
        if (idOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID операции"));
            return;
        }

        Long id = idOpt.get();
        Long functionId = functionIdOpt.get();

        boolean deleted = operationRepository.deleteOperation(id, functionId);
        if (deleted) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    createSuccessResponse("Операция успешно удалена"));
            logger.info("Удалена операция с ID: " + id);
        } else {
            sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Операция не найдена"));
        }
    }

    private void deleteAllOperationsForFunction(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {

        Optional<Long> functionIdOpt = parseLongParameter(req, "functionId");
        if (functionIdOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен"));
            return;
        }

        Long functionId = functionIdOpt.get();
        boolean deleted = operationRepository.deleteAllOperations(functionId);

        if (deleted) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    createSuccessResponse("Все операции для функции успешно удалены"));
            logger.info("Удалены все операции для функции с ID: " + functionId);
        } else {
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    createSuccessResponse("Операции для функции не найдены"));
        }
    }

    @Override
    public void destroy() {
        logger.info("OperationServlet уничтожается");
        super.destroy();
    }
}