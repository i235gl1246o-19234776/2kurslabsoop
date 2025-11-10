package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.entity.TabulatedFunction;
import model.dto.request.TabulatedFunctionRequestDTO;
import model.dto.response.TabulatedFunctionResponseDTO;
import repository.TabulatedFunctionRepository;
import model.dto.DTOTransformService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet("/api/tabulated-points/*")
public class TabulatedFunctionPointServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(TabulatedFunctionPointServlet.class.getName());
    private TabulatedFunctionRepository repository;
    private DTOTransformService transformService;
    private ObjectMapper objectMapper;
    private Validator validator;

    @Override
    public void init() throws ServletException {
        super.init();
        this.repository = new TabulatedFunctionRepository();
        this.transformService = new DTOTransformService();
        this.objectMapper = new ObjectMapper();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        logger.info("TabulatedFunctionPointServlet инициализирован");
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

    private String createValidationErrorResponse(Set<ConstraintViolation<TabulatedFunctionRequestDTO>> violations) {
        String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
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

    private Optional<Long> parseIdFromPath(String pathInfo, int startIndex) {
        try {
            return Optional.of(Long.parseLong(pathInfo.substring(startIndex)));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    // ==================== ОБРАБОТКА GET ЗАПРОСОВ ====================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("GET: " + request.getRequestURI());

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный URL"));
                return;
            }

            String responseData;
            int httpStatus;

            if (pathInfo.matches("/function/\\d+/x/[-+]?\\d*\\.?\\d+")) {
                // GET /api/tabulated-points/function/{functionId}/x/{xVal}
                responseData = handleGetPointByXValue(pathInfo);
                httpStatus = responseData.contains("\"success\":true") ?
                        HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND;
            } else if (pathInfo.matches("/function/\\d+/range")) {
                // GET /api/tabulated-points/function/{functionId}/range
                responseData = handleGetPointsInRange(pathInfo, request);
                httpStatus = responseData.contains("\"success\":true") ?
                        HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST;
            } else if (pathInfo.matches("/function/\\d+")) {
                // GET /api/tabulated-points/function/{functionId}
                responseData = handleGetAllPointsByFunctionId(pathInfo);
                httpStatus = HttpServletResponse.SC_OK; // Пустой массив - это нормально
            } else {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = createErrorResponse(httpStatus, "Неверный формат URL");
            }

            sendJsonResponse(response, httpStatus, responseData);

        } catch (Exception e) {
            logger.severe("Ошибка в GET: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("GET обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    // ==================== ОБРАБОТКА POST ЗАПРОСОВ ====================

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("POST: создание точки функции");

        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            logger.fine("Тело запроса: " + body);

            TabulatedFunctionRequestDTO pointRequest = parseJsonBody(body, TabulatedFunctionRequestDTO.class);
            if (pointRequest == null) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON"));
                return;
            }

            Set<ConstraintViolation<TabulatedFunctionRequestDTO>> violations = validator.validate(pointRequest);
            if (!violations.isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        createValidationErrorResponse(violations));
                return;
            }

            TabulatedFunction newPoint = transformService.toEntity(pointRequest);
            Long pointId = repository.createTabulatedFunction(newPoint);

            if (pointId == null) {
                throw new SQLException("Не удалось создать точку функции");
            }

            newPoint.setId(pointId);
            TabulatedFunctionResponseDTO createdPoint = transformService.toResponseDTO(newPoint);

            sendJsonResponse(response, HttpServletResponse.SC_CREATED, createSuccessResponse(createdPoint));
            logger.info("Создана точка: " + createdPoint);

        } catch (SQLException e) {
            logger.severe("Ошибка БД при создании: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при создании точки"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("POST обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    // ==================== ОБРАБОТКА PUT ЗАПРОСОВ ====================

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("PUT: обновление точки функции");

        try {
            String pathInfo = request.getPathInfo();
            Optional<Long> pointIdOpt = parseIdFromPath(pathInfo, 1);

            if (pointIdOpt.isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "ID точки обязателен"));
                return;
            }

            Long pointId = pointIdOpt.get();
            Optional<TabulatedFunction> existingPointOpt = repository.findById(pointId);

            if (existingPointOpt.isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Точка не найдена"));
                return;
            }

            String body = request.getReader().lines().collect(Collectors.joining());
            TabulatedFunctionRequestDTO updateRequest = parseJsonBody(body, TabulatedFunctionRequestDTO.class);

            if (updateRequest == null) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON"));
                return;
            }

            Set<ConstraintViolation<TabulatedFunctionRequestDTO>> violations = validator.validate(updateRequest);
            if (!violations.isEmpty()) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        createValidationErrorResponse(violations));
                return;
            }

            TabulatedFunction pointToUpdate = existingPointOpt.get();
            updatePointFromDTO(pointToUpdate, updateRequest);

            boolean updated = repository.updateTabulatedFunction(pointToUpdate);
            if (!updated) {
                throw new SQLException("Ошибка при обновлении точки");
            }

            TabulatedFunctionResponseDTO responseDto = transformService.toResponseDTO(pointToUpdate);
            sendJsonResponse(response, HttpServletResponse.SC_OK, createSuccessResponse(responseDto));
            logger.info("Точка обновлена: " + pointId);

        } catch (SQLException e) {
            logger.severe("Ошибка БД при обновлении: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при обновлении точки"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("PUT обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    // ==================== ОБРАБОТКА DELETE ЗАПРОСОВ ====================

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("DELETE: удаление точки/точек");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "ID точки или функции обязательно"));
                return;
            }

            String responseData;
            int httpStatus;

            if (pathInfo.matches("/\\d+")) {
                // DELETE /api/tabulated-points/{id}
                Long pointId = Long.parseLong(pathInfo.substring(1));
                boolean deleted = repository.deleteTabulatedFunction(pointId);

                if (deleted) {
                    responseData = createSuccessResponse("Точка удалена");
                    httpStatus = HttpServletResponse.SC_OK;
                    logger.info("Точка удалена: " + pointId);
                } else {
                    responseData = createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Точка не найдена");
                    httpStatus = HttpServletResponse.SC_NOT_FOUND;
                }
            } else if (pathInfo.matches("/function/\\d+")) {
                // DELETE /api/tabulated-points/function/{functionId}
                Long functionId = Long.parseLong(pathInfo.substring(10));
                List<TabulatedFunction> points = repository.findAllByFunctionId(functionId);

                if (points.isEmpty()) {
                    responseData = createSuccessResponse("Точки функции не найдены");
                    httpStatus = HttpServletResponse.SC_OK;
                } else {
                    boolean deleted = repository.deleteAllTabulatedFunctions(functionId);
                    if (deleted) {
                        responseData = createSuccessResponse("Удалено " + points.size() + " точек функции");
                        httpStatus = HttpServletResponse.SC_OK;
                        logger.info("Удалено точек: " + points.size() + " для функции: " + functionId);
                    } else {
                        throw new SQLException("Не удалось удалить точки функции");
                    }
                }
            } else {
                responseData = createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат URL");
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
            }

            sendJsonResponse(response, httpStatus, responseData);

        } catch (NumberFormatException e) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID"));
        } catch (SQLException e) {
            logger.severe("Ошибка БД при удалении: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при удалении"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера"));
        }

        logger.info("DELETE обработан за " + (System.currentTimeMillis() - startTime) + " мс");
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ОБРАБОТКИ ====================

    private String handleGetPointByXValue(String pathInfo) {
        try {
            String[] parts = pathInfo.split("/");
            Long functionId = Long.parseLong(parts[2]);
            Double xVal = Double.parseDouble(parts[4]);

            Optional<TabulatedFunction> pointOpt = repository.findByXValue(functionId, xVal);

            return pointOpt.map(point -> {
                TabulatedFunctionResponseDTO responseDto = transformService.toResponseDTO(point);
                return createSuccessResponse(responseDto);
            }).orElse(createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Точка не найдена"));

        } catch (Exception e) {
            logger.warning("Ошибка парсинга пути: " + e.getMessage());
            return createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат URL");
        }
    }

    private String handleGetPointsInRange(String pathInfo, HttpServletRequest request) {
        try {
            Long functionId = Long.parseLong(pathInfo.split("/")[2]);
            String fromXStr = request.getParameter("fromX");
            String toXStr = request.getParameter("toX");

            if (fromXStr == null || toXStr == null) {
                return createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Параметры fromX и toX обязательны");
            }

            Double fromX = Double.parseDouble(fromXStr);
            Double toX = Double.parseDouble(toXStr);

            if (fromX > toX) {
                return createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "fromX не может быть больше toX");
            }

            List<TabulatedFunction> points = repository.findBetweenXValues(functionId, fromX, toX);
            List<TabulatedFunctionResponseDTO> responsePoints = transformService.toResponseDTOs(points);

            logger.info("Найдено " + points.size() + " точек в диапазоне для функции " + functionId);
            return createSuccessResponse(responsePoints);

        } catch (NumberFormatException e) {
            return createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат параметров");
        } catch (Exception e) {
            logger.warning("Ошибка при поиске в диапазоне: " + e.getMessage());
            return createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при поиске");
        }
    }

    private String handleGetAllPointsByFunctionId(String pathInfo) {
        try {
            Long functionId = Long.parseLong(pathInfo.substring(9));
            List<TabulatedFunction> points = repository.findAllByFunctionId(functionId);
            List<TabulatedFunctionResponseDTO> responsePoints = transformService.toResponseDTOs(points);

            logger.info("Найдено " + points.size() + " точек для функции " + functionId);
            return createSuccessResponse(responsePoints);

        } catch (NumberFormatException e) {
            return createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID функции");
        } catch (SQLException e) {
            logger.severe("Ошибка БД: " + e.getMessage());
            return createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        } catch (Exception e) {
            logger.severe("Ошибка обработки: " + e.getMessage());
            return createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка сервера");
        }
    }

    // ==================== УТИЛИТНЫЕ МЕТОДЫ ПРЕОБРАЗОВАНИЯ ====================

    private <T> T parseJsonBody(String body, Class<T> valueType) {
        try {
            return objectMapper.readValue(body, valueType);
        } catch (Exception e) {
            logger.warning("Ошибка парсинга JSON: " + e.getMessage());
            return null;
        }
    }

    private void updatePointFromDTO(TabulatedFunction point, TabulatedFunctionRequestDTO dto) {
        point.setFunctionId(dto.getFunctionId());
        point.setXVal(dto.getXVal());
        point.setYVal(dto.getYVal());
    }

    @Override
    public void destroy() {
        logger.info("TabulatedFunctionPointServlet уничтожается");
        super.destroy();
    }
}