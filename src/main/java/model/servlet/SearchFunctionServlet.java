package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.SearchFunctionResult;
import model.dto.request.SearchFunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import model.dto.response.SearchFunctionResponseDTO;
import repository.DatabaseConnection;
import repository.FunctionRepository;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/api/search/functions/*")
public class SearchFunctionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(SearchFunctionServlet.class.getName());
    private FunctionRepository functionRepository;
    private DTOTransformService transformService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        try {
            DatabaseConnection dbc = new DatabaseConnection();
            this.functionRepository = new FunctionRepository(dbc);
            this.transformService = new DTOTransformService();
            this.objectMapper = new ObjectMapper();
            logger.info("SearchFunctionServlet инициализирован с DTOTransformService");
        } catch (Exception e) {
            logger.severe("Ошибка инициализации SearchFunctionServlet: " + e.getMessage());
            throw new ServletException("Не удалось инициализировать SearchFunctionServlet", e);
        }
    }

    // ==================== УТИЛИТНЫЕ МЕТОДЫ ====================

    private String createSuccessResponse(Object data) {
        return convertToJson(Map.of("success", true, "data", data));
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

    // ==================== ОБРАБОТКА GET ЗАПРОСОВ ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("GET поиск функций: " + req.getQueryString());

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                searchFunctionsGet(req, resp);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден"));
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат параметра: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат числового параметра"));
        } catch (SQLException e) {
            logger.severe("Ошибка БД при поиске: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка при поиске: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("GET поиск обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    private void searchFunctionsGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        // ✅ ХОРОШИЙ ЗАПРОС: GET /api/search/functions?functionName=sin&typeFunction=math
        // ❌ ПЛОХОЙ ЗАПРОС: GET /api/search/functions?userId=abc (неверный формат)

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
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST,
                            "Недопустимое поле для сортировки. Допустимые значения: function_id, function_name, type_function, user_name"));
            return;
        }

        if (sortOrder != null && !sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST,
                            "Недопустимое направление сортировки. Допустимые значения: asc, desc"));
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

        // Преобразуем результаты в DTO с использованием transformService
        List<FunctionResponseDTO> functionDTOs = searchResults.stream()
                .map(this::convertSearchResultToResponseDTO)
                .collect(Collectors.toList());

        // Создаем финальный ответ
        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(
                functionDTOs,
                functionDTOs.size()
        );
        response.setOperationsTypeId(operationsTypeId);

        sendJsonResponse(resp, HttpServletResponse.SC_OK, createSuccessResponse(response));
        logger.info("Поиск (GET) завершен: найдено " + functionDTOs.size() + " функций");
    }

    // ==================== ОБРАБОТКА POST ЗАПРОСОВ ====================

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("POST расширенный поиск функций");

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                searchFunctionsPost(req, resp);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Ресурс не найден"));
            }
        } catch (SQLException e) {
            logger.severe("Ошибка БД при поиске: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка при поиске: " + e.getMessage());
            sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("POST поиск обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    private void searchFunctionsPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        // ✅ ХОРОШИЙ ЗАПРОС: POST /api/search/functions с корректным JSON в теле
        // ❌ ПЛОХОЙ ЗАПРОС: POST /api/search/functions с невалидным JSON

        String body = req.getReader().lines().collect(Collectors.joining());
        logger.fine("Тело POST запроса: " + body);

        SearchFunctionRequestDTO searchRequest = parseJsonBody(body, SearchFunctionRequestDTO.class);
        if (searchRequest == null) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON"));
            return;
        }

        // Валидация параметров сортировки
        if (!searchRequest.isValidSortBy()) {
            sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST,
                            "Недопустимое поле для сортировки. Допустимые значения: function_id, function_name, type_function, user_name"));
            return;
        }

        logger.info(String.format(
                "Поиск функций (POST): userName='%s', functionName='%s', type='%s', x=%s, y=%s, operationsTypeId=%s, sortBy=%s, sortOrder=%s",
                searchRequest.getUserName(), searchRequest.getFunctionName(), searchRequest.getTypeFunction(),
                searchRequest.getXVal(), searchRequest.getYVal(), searchRequest.getOperationsTypeId(),
                searchRequest.getSortBy(), searchRequest.getSortOrder()
        ));

        // Выполняем поиск
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

        // Преобразуем результаты в DTO с использованием transformService
        List<FunctionResponseDTO> functionDTOs = searchResults.stream()
                .map(this::convertSearchResultToResponseDTO)
                .collect(Collectors.toList());

        // Создаем финальный ответ
        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(
                functionDTOs,
                functionDTOs.size()
        );
        response.setOperationsTypeId(searchRequest.getOperationsTypeId());

        sendJsonResponse(resp, HttpServletResponse.SC_OK, createSuccessResponse(response));
        logger.info("Поиск (POST) завершен: найдено " + functionDTOs.size() + " функций");
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ПРЕОБРАЗОВАНИЯ ====================

    /**
     * Преобразует результат поиска в ResponseDTO
     * Использует DTOTransformService для согласованности преобразований
     */
    private FunctionResponseDTO convertSearchResultToResponseDTO(SearchFunctionResult result) {
        // Создаем DTO через transformService для единообразия
        // Если бы у нас была полная Function entity, мы бы использовали:
        // return transformService.toResponseDTO(functionEntity);

        // Но так как у нас SearchFunctionResult, создаем DTO напрямую
        // с использованием той же структуры, что и в transformService
        return new FunctionResponseDTO(
                result.getFunctionId(),
                result.getUserId(),
                result.getTypeFunction(),
                result.getFunctionName(),
                result.getFunctionExpression()
        );
    }

    private <T> T parseJsonBody(String body, Class<T> valueType) {
        try {
            return objectMapper.readValue(body, valueType);
        } catch (Exception e) {
            logger.warning("Ошибка парсинга JSON: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void destroy() {
        logger.info("SearchFunctionServlet уничтожается");
        super.destroy();
    }
}