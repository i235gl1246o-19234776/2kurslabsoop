package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.SearchFunctionResult;
import model.dto.request.SearchFunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import model.dto.response.SearchFunctionResponseDTO;
import repository.FunctionRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/api/search/functions/*")
public class SearchFunctionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(SearchFunctionServlet.class.getName());
    private FunctionRepository functionRepository;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        try {
            this.functionRepository = new FunctionRepository();
            this.objectMapper = new ObjectMapper();
            logger.info("SearchFunctionServlet инициализирован");
        } catch (Exception e) {
            logger.severe("Ошибка инициализации SearchFunctionServlet: " + e.getMessage());
            throw new ServletException("Не удалось инициализировать SearchFunctionServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/search/functions - поиск функций через параметры URL
                searchFunctionsGet(req, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден");
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных при поиске: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка при поиске: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/search/functions - расширенный поиск через тело запроса
                searchFunctionsPost(req, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден");
            }
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных при поиске: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка при поиске: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    private void searchFunctionsGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        try {
            // Получаем параметры из URL
            Long userId = getLongParameter(req, "userId");
            String userName = req.getParameter("userName");
            String functionName = req.getParameter("functionName");
            String typeFunction = req.getParameter("typeFunction");
            Double xVal = getDoubleParameter(req, "xVal");
            Double yVal = getDoubleParameter(req, "yVal");
            Long operationsTypeId = getLongParameter(req, "operationsTypeId");
            String sortBy = req.getParameter("sortBy");
            String sortOrder = req.getParameter("sortOrder");

            // Валидация параметров сортировки
            if (sortBy != null && !isValidSortBy(sortBy)) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Недопустимое поле для сортировки. Допустимые значения: function_id, function_name, type_function, user_name");
                return;
            }

            if (sortOrder != null && !sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Недопустимое направление сортировки. Допустимые значения: asc, desc");
                return;
            }

            logger.info(String.format(
                    "Поиск функций (GET): userId=%s, userName='%s', functionName='%s', type='%s', x=%s, y=%s, operationsTypeId=%s",
                    userId, userName, functionName, typeFunction, xVal, yVal, operationsTypeId
            ));

            // Выполняем поиск
            List<SearchFunctionResult> searchResults = functionRepository.search(
                    userId, userName, functionName, typeFunction,
                    xVal, yVal, operationsTypeId, sortBy, sortOrder
            );

            // Преобразуем результаты в DTO
            List<FunctionResponseDTO> functionDTOs = searchResults.stream()
                    .map(this::convertSearchResultToResponseDTO)
                    .collect(Collectors.toList());

            // Создаем финальный ответ
            SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(
                    functionDTOs,
                    functionDTOs.size()
            );
            response.setOperationsTypeId(operationsTypeId);

            String jsonResponse = objectMapper.writeValueAsString(response);
            resp.getWriter().write(jsonResponse);

            logger.info("Поиск (GET) завершен: найдено " + functionDTOs.size() + " функций");

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат числового параметра");
        }
    }

    private void searchFunctionsPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        try {
            // Читаем и валидируем запрос
            SearchFunctionRequestDTO searchRequest = objectMapper.readValue(
                    req.getReader(),
                    SearchFunctionRequestDTO.class
            );

            // Валидация параметров сортировки
            if (!searchRequest.isValidSortBy()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Недопустимое поле для сортировки. Допустимые значения: function_id, function_name, type_function, user_name");
                return;
            }

            logger.info(String.format(
                    "Поиск функций (POST): userName='%s', functionName='%s', type='%s', x=%s, y=%s, operationsTypeId=%s, sortBy=%s, sortOrder=%s",
                    searchRequest.getUserName(), searchRequest.getFunctionName(), searchRequest.getTypeFunction(),
                    searchRequest.getXVal(), searchRequest.getYVal(), searchRequest.getOperationsTypeId(),
                    searchRequest.getSortBy(), searchRequest.getSortOrder()
            ));

            // Выполняем поиск (userId = null, так как в DTO его нет)
            List<SearchFunctionResult> searchResults = functionRepository.search(
                    null, // userId можно добавить в DTO при необходимости
                    searchRequest.getUserName(),
                    searchRequest.getFunctionName(),
                    searchRequest.getTypeFunction(),
                    searchRequest.getXVal(),
                    searchRequest.getYVal(),
                    searchRequest.getOperationsTypeId(),
                    searchRequest.getSortBy(),
                    searchRequest.getSortOrder()
            );

            // Преобразуем результаты в DTO
            List<FunctionResponseDTO> functionDTOs = searchResults.stream()
                    .map(this::convertSearchResultToResponseDTO)
                    .collect(Collectors.toList());

            // Создаем финальный ответ
            SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(
                    functionDTOs,
                    functionDTOs.size()
            );
            response.setOperationsTypeId(searchRequest.getOperationsTypeId());

            String jsonResponse = objectMapper.writeValueAsString(response);
            resp.getWriter().write(jsonResponse);

            logger.info("Поиск (POST) завершен: найдено " + functionDTOs.size() + " функций");

        } catch (Exception e) {
            logger.severe("Ошибка при обработке запроса поиска: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат данных запроса");
        }
    }

    // Вспомогательные методы
    private FunctionResponseDTO convertSearchResultToResponseDTO(SearchFunctionResult result) {
        return new FunctionResponseDTO(
                result.getFunctionId(),
                result.getUserId(),
                result.getTypeFunction(),
                result.getFunctionName(),
                result.getFunctionExpression()
        );
    }

    private Long getLongParameter(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Неверный формат параметра: " + paramName);
            }
        }
        return null;
    }

    private Double getDoubleParameter(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Неверный формат параметра: " + paramName);
            }
        }
        return null;
    }

    private boolean isValidSortBy(String sortBy) {
        return switch (sortBy) {
            case "function_id", "function_name", "type_function", "user_name" -> true;
            default -> false;
        };
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
