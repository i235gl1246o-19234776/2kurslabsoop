package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Function;
import model.SearchFunctionResult;
import model.dto.request.FunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import repository.FunctionRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/api/functions/*")
public class FunctionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(FunctionServlet.class.getName());
    private FunctionRepository functionRepository;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        try {
            this.functionRepository = new FunctionRepository();
            this.functionRepository.createPerformanceTable();
            this.objectMapper = new ObjectMapper();
            logger.info("FunctionServlet инициализирован");
        } catch (SQLException e) {
            logger.severe("Ошибка инициализации FunctionServlet: " + e.getMessage());
            throw new ServletException("Не удалось инициализировать FunctionServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/functions - получить все функции пользователя
                getAllFunctions(req, resp);
            } else if (pathInfo.matches("/\\d+")) {
                // GET /api/functions/{id} - получить функцию по ID
                getFunctionById(req, resp, pathInfo);
            } else if (pathInfo.equals("/search")) {
                // GET /api/functions/search - расширенный поиск
                searchFunctions(req, resp);
            } else if (pathInfo.equals("/stats")) {
                // GET /api/functions/stats - статистика производительности
                getPerformanceStats(req, resp);
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/functions - создать новую функцию
                createFunction(req, resp);
            } else if (pathInfo.equals("/search")) {
                // POST /api/functions/search - поиск с телом запроса
                searchFunctionsPost(req, resp);
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
                // PUT /api/functions/{id} - обновить функцию
                updateFunction(req, resp, pathInfo);
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
                // DELETE /api/functions/{id} - удалить функцию
                deleteFunction(req, resp, pathInfo);
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

    private void getAllFunctions(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String userIdParam = req.getParameter("userId");
        String namePattern = req.getParameter("name");
        String type = req.getParameter("type");

        if (userIdParam == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр userId обязателен");
            return;
        }

        try {
            Long userId = Long.parseLong(userIdParam);
            List<Function> functions;

            if (namePattern != null && !namePattern.trim().isEmpty()) {
                functions = functionRepository.findByName(userId, namePattern.trim());
            } else if (type != null && !type.trim().isEmpty()) {
                functions = functionRepository.findByType(userId, type.trim());
            } else {
                functions = functionRepository.findByUserId(userId);
            }

            List<FunctionResponseDTO> responseDTOs = functions.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            String jsonResponse = objectMapper.writeValueAsString(responseDTOs);
            resp.getWriter().write(jsonResponse);
            logger.info("Возвращено " + responseDTOs.size() + " функций для пользователя " + userId);

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат userId");
        }
    }

    private void getFunctionById(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, SQLException {
        String userIdParam = req.getParameter("userId");

        if (userIdParam == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр userId обязателен");
            return;
        }

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            Long userId = Long.parseLong(userIdParam);

            Optional<Function> function = functionRepository.findById(id, userId);
            if (function.isPresent()) {
                FunctionResponseDTO responseDTO = convertToResponseDTO(function.get());
                String jsonResponse = objectMapper.writeValueAsString(responseDTO);
                resp.getWriter().write(jsonResponse);
                logger.info("Возвращена функция с ID: " + id);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Функция не найдена");
            }

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID или userId");
        }
    }

    private void createFunction(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        try {
            FunctionRequestDTO requestDTO = objectMapper.readValue(req.getReader(), FunctionRequestDTO.class);

            // Валидация
            if (requestDTO.getUserId() == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "userId обязателен");
                return;
            }
            if (requestDTO.getFunctionName() == null || requestDTO.getFunctionName().trim().isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "functionName обязателен");
                return;
            }
            if (requestDTO.getTypeFunction() == null || requestDTO.getTypeFunction().trim().isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "typeFunction обязателен");
                return;
            }

            Function function = convertToEntity(requestDTO);
            Long functionId = functionRepository.createFunction(function);

            ApiResponse<Long> apiResponse = new ApiResponse<>(true, "Функция успешно создана", functionId);
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(jsonResponse);
            logger.info("Создана новая функция с ID: " + functionId);

        } catch (Exception e) {
            logger.severe("Ошибка при создании функции: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат данных");
        }
    }

    private void updateFunction(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, SQLException {
        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            FunctionRequestDTO requestDTO = objectMapper.readValue(req.getReader(), FunctionRequestDTO.class);

            if (requestDTO.getUserId() == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "userId обязателен");
                return;
            }

            // Проверяем существование функции
            Optional<Function> existingFunction = functionRepository.findById(id, requestDTO.getUserId());
            if (!existingFunction.isPresent()) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Функция не найдена");
                return;
            }

            Function function = convertToEntity(requestDTO);
            function.setId(id);

            boolean updated = functionRepository.updateFunction(function);
            if (updated) {
                ApiResponse<String> apiResponse = new ApiResponse<>(true, "Функция успешно обновлена", null);
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                resp.getWriter().write(jsonResponse);
                logger.info("Обновлена функция с ID: " + id);
            } else {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Не удалось обновить функцию");
            }

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID");
        } catch (Exception e) {
            logger.severe("Ошибка при обновлении функции: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат данных");
        }
    }

    private void deleteFunction(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, SQLException {
        String userIdParam = req.getParameter("userId");

        if (userIdParam == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр userId обязателен");
            return;
        }

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            Long userId = Long.parseLong(userIdParam);

            boolean deleted = functionRepository.deleteFunction(id, userId);
            if (deleted) {
                ApiResponse<String> apiResponse = new ApiResponse<>(true, "Функция успешно удалена", null);
                String jsonResponse = objectMapper.writeValueAsString(apiResponse);
                resp.getWriter().write(jsonResponse);
                logger.info("Удалена функция с ID: " + id);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Функция не найдена");
            }

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID или userId");
        }
    }

    private void searchFunctions(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        try {
            Long userId = getLongParameter(req, "userId");
            String userName = req.getParameter("userName");
            String functionNamePattern = req.getParameter("functionName");
            String typeFunction = req.getParameter("typeFunction");
            Double xVal = getDoubleParameter(req, "xVal");
            Double yVal = getDoubleParameter(req, "yVal");
            Long operationsTypeId = getLongParameter(req, "operationsTypeId");
            String sortBy = req.getParameter("sortBy");
            String sortOrder = req.getParameter("sortOrder");

            List<SearchFunctionResult> results = functionRepository.search(
                    userId, userName, functionNamePattern, typeFunction,
                    xVal, yVal, operationsTypeId, sortBy, sortOrder
            );

            String jsonResponse = objectMapper.writeValueAsString(results);
            resp.getWriter().write(jsonResponse);
            logger.info("Поиск завершен, найдено " + results.size() + " результатов");

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат числового параметра");
        }
    }

    private void searchFunctionsPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        try {
            SearchRequest searchRequest = objectMapper.readValue(req.getReader(), SearchRequest.class);

            List<SearchFunctionResult> results = functionRepository.search(
                    searchRequest.getUserId(),
                    searchRequest.getUserName(),
                    searchRequest.getFunctionName(),
                    searchRequest.getTypeFunction(),
                    searchRequest.getXVal(),
                    searchRequest.getYVal(),
                    searchRequest.getOperationsTypeId(),
                    searchRequest.getSortBy(),
                    searchRequest.getSortOrder()
            );

            String jsonResponse = objectMapper.writeValueAsString(results);
            resp.getWriter().write(jsonResponse);
            logger.info("Поиск (POST) завершен, найдено " + results.size() + " результатов");

        } catch (Exception e) {
            logger.severe("Ошибка при поиске функций (POST): " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат данных поиска");
        }
    }

    private void getPerformanceStats(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        functionRepository.printPerformanceStats();
        ApiResponse<String> apiResponse = new ApiResponse<>(true, "Статистика выведена в лог", null);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        resp.getWriter().write(jsonResponse);
    }

    // Вспомогательные методы
    private Function convertToEntity(FunctionRequestDTO dto) {
        Function function = new Function();
        function.setUserId(dto.getUserId());
        function.setTypeFunction(dto.getTypeFunction());
        function.setFunctionName(dto.getFunctionName());
        function.setFunctionExpression(dto.getFunctionExpression());
        return function;
    }

    private FunctionResponseDTO convertToResponseDTO(Function function) {
        return new FunctionResponseDTO(
                function.getId(),
                function.getUserId(),
                function.getTypeFunction(),
                function.getFunctionName(),
                function.getFunctionExpression()
        );
    }

    private Long getLongParameter(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        return value != null ? Long.parseLong(value) : null;
    }

    private Double getDoubleParameter(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        return value != null ? Double.parseDouble(value) : null;
    }

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        ApiResponse<String> errorResponse = new ApiResponse<>(false, message, null);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        resp.getWriter().write(jsonResponse);
    }

    // Внутренние классы для запросов/ответов
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

    public static class SearchRequest {
        private Long userId;
        private String userName;
        private String functionName;
        private String typeFunction;
        private Double xVal;
        private Double yVal;
        private Long operationsTypeId;
        private String sortBy;
        private String sortOrder;

        // геттеры и сеттеры
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getFunctionName() { return functionName; }
        public void setFunctionName(String functionName) { this.functionName = functionName; }
        public String getTypeFunction() { return typeFunction; }
        public void setTypeFunction(String typeFunction) { this.typeFunction = typeFunction; }
        public Double getXVal() { return xVal; }
        public void setXVal(Double xVal) { this.xVal = xVal; }
        public Double getYVal() { return yVal; }
        public void setYVal(Double yVal) { this.yVal = yVal; }
        public Long getOperationsTypeId() { return operationsTypeId; }
        public void setOperationsTypeId(Long operationsTypeId) { this.operationsTypeId = operationsTypeId; }
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
    }
}