package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.entity.Function;
import model.dto.request.FunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import repository.dao.FunctionRepository;
import model.dto.DTOTransformService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/api/functions/*")
public class FunctionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(FunctionServlet.class.getName());
    private FunctionRepository functionRepository;
    private DTOTransformService transformService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        try {
            this.functionRepository = new FunctionRepository();
            this.functionRepository.createPerformanceTable();
            this.transformService = new DTOTransformService();
            this.objectMapper = new ObjectMapper();
            logger.info("FunctionServlet инициализирован с DTOTransformService");
        } catch (SQLException e) {
            logger.severe("Ошибка инициализации FunctionServlet: " + e.getMessage());
            throw new ServletException("Не удалось инициализировать FunctionServlet", e);
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

    private Optional<Double> parseDoubleParameter(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Optional.of(Double.parseDouble(value.trim()));
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
        logger.info("GET функции: " + req.getRequestURI());

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // ✅ ХОРОШИЙ ЗАПРОС: GET /api/functions?userId=1
                // ❌ ПЛОХОЙ ЗАПРОС: GET /api/functions (без userId)
                getAllFunctions(req, resp);
            } else if (pathInfo.matches("/\\d+")) {
                // ✅ ХОРОШИЙ ЗАПРОС: GET /api/functions/1?userId=1
                getFunctionById(req, resp, pathInfo);
            } else if (pathInfo.equals("/search")) {
                // ✅ ХОРОШИЙ ЗАПРОС: GET /api/functions/search?functionName=sin
                searchFunctions(req, resp);
            } else if (pathInfo.equals("/stats")) {
                // ✅ ХОРОШИЙ ЗАПРОС: GET /api/functions/stats
                getPerformanceStats(req, resp);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден"));
            }
        } catch (SQLException e) {
            logger.severe("Ошибка БД: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("GET функции обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    private void getAllFunctions(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        Optional<Long> userIdOpt = parseLongParameter(req, "userId");
        if (userIdOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Параметр userId обязателен"));
            return;
        }

        Long userId = userIdOpt.get();
        String namePattern = req.getParameter("name");
        String type = req.getParameter("type");

        List<Function> functions;

        if (namePattern != null && !namePattern.trim().isEmpty()) {
            functions = functionRepository.findByName(userId, namePattern.trim());
        } else if (type != null && !type.trim().isEmpty()) {
            functions = functionRepository.findByType(userId, type.trim());
        } else {
            functions = functionRepository.findByUserId(userId);
        }

        // ✅ Используем transformService для пакетного преобразования
        List<FunctionResponseDTO> responseDTOs = transformService.toFunctionResponseDTOs(functions);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, createSuccessResponse(responseDTOs));
        logger.info("Возвращено " + responseDTOs.size() + " функций для пользователя " + userId);
    }

    private void getFunctionById(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws IOException, SQLException {

        Optional<Long> userIdOpt = parseLongParameter(req, "userId");
        if (userIdOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Параметр userId обязателен"));
            return;
        }

        Optional<Long> idOpt = parseIdFromPath(pathInfo);
        if (idOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID функции"));
            return;
        }

        Long id = idOpt.get();
        Long userId = userIdOpt.get();

        Optional<Function> function = functionRepository.findById(id, userId);
        if (function.isPresent()) {
            // ✅ Используем transformService для преобразования Entity в DTO
            FunctionResponseDTO responseDTO = transformService.toResponseDTO(function.get());
            sendJsonResponse(resp, HttpServletResponse.SC_OK, createSuccessResponse(responseDTO));
            logger.info("Возвращена функция с ID: " + id);
        } else {
            sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Функция не найдена"));
        }
    }

    // ==================== ОБРАБОТКА POST ЗАПРОСОВ ====================

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("POST функция: " + req.getRequestURI());

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // ✅ ХОРОШИЙ ЗАПРОС: POST /api/functions с корректным JSON
                createFunction(req, resp);
            } else if (pathInfo.equals("/search")) {
                // ✅ ХОРОШИЙ ЗАПРОС: POST /api/functions/search с телом запроса
                searchFunctionsPost(req, resp);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден"));
            }
        } catch (SQLException e) {
            logger.severe("Ошибка БД: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("POST функция обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    private void createFunction(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String body = req.getReader().lines().collect(Collectors.joining());
        logger.fine("Тело запроса создания: " + body);

        FunctionRequestDTO requestDTO = parseJsonBody(body, FunctionRequestDTO.class);
        if (requestDTO == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON"));
            return;
        }

        // Валидация обязательных полей
        if (requestDTO.getUserId() == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "userId обязателен"));
            return;
        }
        if (requestDTO.getFunctionName() == null || requestDTO.getFunctionName().trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "functionName обязателен"));
            return;
        }
        if (requestDTO.getTypeFunction() == null || requestDTO.getTypeFunction().trim().isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "typeFunction обязателен"));
            return;
        }

        // ✅ Используем transformService для преобразования DTO в Entity
        Function function = transformService.toEntity(requestDTO);
        Long functionId = functionRepository.createFunction(function);

        if (functionId != null) {
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
                    createSuccessResponse(Map.of("id", functionId, "message", "Функция успешно создана")));
            logger.info("Создана новая функция с ID: " + functionId);
        } else {
            throw new SQLException("Не удалось создать функцию");
        }
    }

    // ==================== ОБРАБОТКА PUT ЗАПРОСОВ ====================

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("PUT функция: " + req.getRequestURI());

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // ✅ ХОРОШИЙ ЗАПРОС: PUT /api/functions/1 с корректным JSON
                // ❌ ПЛОХОЙ ЗАПРОС: PUT /api/functions/999 (несуществующий)
                updateFunction(req, resp, pathInfo);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден"));
            }
        } catch (SQLException e) {
            logger.severe("Ошибка БД: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("PUT функция обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    private void updateFunction(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws IOException, SQLException {

        Optional<Long> idOpt = parseIdFromPath(pathInfo);
        if (idOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID функции"));
            return;
        }

        Long id = idOpt.get();
        String body = req.getReader().lines().collect(Collectors.joining());
        logger.fine("Тело запроса обновления: " + body);

        FunctionRequestDTO requestDTO = parseJsonBody(body, FunctionRequestDTO.class);
        if (requestDTO == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON"));
            return;
        }

        if (requestDTO.getUserId() == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "userId обязателен"));
            return;
        }

        // Проверяем существование функции
        Optional<Function> existingFunction = functionRepository.findById(id, requestDTO.getUserId());
        if (existingFunction.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Функция не найдена"));
            return;
        }

        // ✅ Используем transformService для преобразования DTO в Entity
        Function function = transformService.toEntity(requestDTO);
        function.setId(id);

        boolean updated = functionRepository.updateFunction(function);
        if (updated) {
            FunctionResponseDTO responseDTO = transformService.toResponseDTO(function);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, createSuccessResponse(responseDTO));
            logger.info("Обновлена функция с ID: " + id);
        } else {
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Не удалось обновить функцию"));
        }
    }

    // ==================== ОБРАБОТКА DELETE ЗАПРОСОВ ====================

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("DELETE функция: " + req.getRequestURI());

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // ✅ ХОРОШИЙ ЗАПРОС: DELETE /api/functions/1?userId=1
                // ❌ ПЛОХОЙ ЗАПРОС: DELETE /api/functions/1 (без userId)
                deleteFunction(req, resp, pathInfo);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден"));
            }
        } catch (SQLException e) {
            logger.severe("Ошибка БД: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("DELETE функция обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    private void deleteFunction(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws IOException, SQLException {

        Optional<Long> userIdOpt = parseLongParameter(req, "userId");
        if (userIdOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Параметр userId обязателен"));
            return;
        }

        Optional<Long> idOpt = parseIdFromPath(pathInfo);
        if (idOpt.isEmpty()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID функции"));
            return;
        }

        Long id = idOpt.get();
        Long userId = userIdOpt.get();

        boolean deleted = functionRepository.deleteFunction(id, userId);
        if (deleted) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK,
                    createSuccessResponse("Функция успешно удалена"));
            logger.info("Удалена функция с ID: " + id);
        } else {
            sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Функция не найдена"));
        }
    }

    // ==================== МЕТОДЫ ПОИСКА И СТАТИСТИКИ ====================

    private void searchFunctions(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        // Используем уже существующий SearchFunctionServlet для поиска
        // Этот метод можно удалить, если поиск полностью перенесен в отдельный сервлет
        sendJsonResponse(resp, HttpServletResponse.SC_OK,
                createSuccessResponse("Используйте /api/search/functions для расширенного поиска"));
    }

    private void searchFunctionsPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        // Используем уже существующий SearchFunctionServlet для поиска
        sendJsonResponse(resp, HttpServletResponse.SC_OK,
                createSuccessResponse("Используйте POST /api/search/functions для расширенного поиска"));
    }

    private void getPerformanceStats(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        functionRepository.printPerformanceStats();
        sendJsonResponse(resp, HttpServletResponse.SC_OK,
                createSuccessResponse("Статистика производительности выведена в лог"));
    }

    @Override
    public void destroy() {
        logger.info("FunctionServlet уничтожается");
        super.destroy();
    }
}