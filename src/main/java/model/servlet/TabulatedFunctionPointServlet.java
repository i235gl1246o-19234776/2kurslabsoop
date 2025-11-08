package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.TabulatedFunction;
import model.dto.request.TabulatedFunctionRequestDTO;
import model.dto.response.TabulatedFunctionResponseDTO;
import repository.TabulatedFunctionRepository;
import service.DTOTransformService;

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
    private DTOTransformService transformService; // Добавляем поле для сервиса преобразований
    private ObjectMapper objectMapper;
    private Validator validator;

    @Override
    public void init() throws ServletException {
        super.init();
        this.repository = new TabulatedFunctionRepository();
        this.transformService = new DTOTransformService(); // Инициализируем сервис преобразований
        this.objectMapper = new ObjectMapper();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        logger.info("TabulatedFunctionPointServlet инициализирован с JSON поддержкой и DTOTransformService");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("Обработка GET запроса: " + request.getRequestURI());

        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String pathInfo = request.getPathInfo();
            String responseData = null;
            int httpStatus = HttpServletResponse.SC_OK;

            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", "Неверный URL")));
                return;
            }

            if (pathInfo.matches("/function/\\d+/x/\\d+(\\.\\d+)?")) {
                // GET /api/tabulated-points/function/{functionId}/x/{xVal}
                Optional<String> pointJsonOpt = getPointByXValueOpt(pathInfo);
                if (pointJsonOpt.isPresent()) {
                    responseData = pointJsonOpt.get();
                    httpStatus = HttpServletResponse.SC_OK;
                } else {
                    httpStatus = HttpServletResponse.SC_NOT_FOUND;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Точка не найдена"));
                }
            } else if (pathInfo.matches("/function/\\d+/range")) {
                // GET /api/tabulated-points/function/{functionId}/range?fromX={min}&toX={max}
                Optional<String> pointsJsonOpt = getPointsInRangeOpt(pathInfo, request);
                if (pointsJsonOpt.isPresent()) {
                    responseData = pointsJsonOpt.get();
                    httpStatus = HttpServletResponse.SC_OK;
                } else {
                    // Ошибка уже обработана внутри getPointsInRangeOpt
                    // Предположим, что она возвращает JSON с ошибкой и соответствующий статус
                    // Проверим, начинается ли строка с "error"
                    if (pointsJsonOpt.orElse("").startsWith("{\"error\"")) {
                        httpStatus = HttpServletResponse.SC_BAD_REQUEST; // Или 500, в зависимости от ошибки внутри
                        responseData = pointsJsonOpt.get(); // Это будет строка JSON с ошибкой
                    } else {
                        // Если Optional пуст, но не ошибка, вернем 500
                        httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка сервера при обработке данных"));
                    }
                }
            } else if (pathInfo.matches("/function/\\d+")) {
                // GET /api/tabulated-points/function/{functionId}
                Optional<String> pointsJsonOpt = getAllPointsByFunctionIdOpt(pathInfo);
                if (pointsJsonOpt.isPresent()) {
                    responseData = pointsJsonOpt.get();
                    httpStatus = HttpServletResponse.SC_OK;
                } else {
                    // Ошибка уже обработана внутри getAllPointsByFunctionIdOpt
                    // Предположим, что она возвращает JSON с ошибкой и соответствующий статус
                    if (pointsJsonOpt.orElse("").startsWith("{\"error\"")) {
                        httpStatus = HttpServletResponse.SC_BAD_REQUEST; // Или 500, в зависимости от ошибки внутри
                        responseData = pointsJsonOpt.get(); // Это будет строка JSON с ошибкой
                    } else {
                        // Если Optional пуст, но не ошибка, вернем 500
                        httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка сервера при обработке данных"));
                    }
                }
            } else {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат URL"));
            }

            response.setStatus(httpStatus);
            response.getWriter().write(responseData);

        } catch (Exception e) {
            logger.severe("Ошибка в GET запросе: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(Map.of("error", "Внутренняя ошибка сервера")));
        }

        long endTime = System.currentTimeMillis();
        logger.info("GET запрос обработан за " + (endTime - startTime) + " мс");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("Обработка POST запроса для создания точки функции");

        int httpStatus = HttpServletResponse.SC_OK;
        String responseData = null;

        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            logger.fine("Получено тело запроса: " + body);

            TabulatedFunctionRequestDTO pointRequest = null;
            try {
                pointRequest = objectMapper.readValue(body, TabulatedFunctionRequestDTO.class);
                logger.info("JSON успешно преобразован: " + pointRequest);
            } catch (Exception e) {
                logger.warning("Ошибка парсинга JSON: " + e.getMessage());
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат JSON"));
            }

            if (pointRequest != null) {
                Set<ConstraintViolation<TabulatedFunctionRequestDTO>> violations = validator.validate(pointRequest);
                if (!violations.isEmpty()) {
                    String errorMessage = violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(", "));

                    logger.warning("Ошибки валидации: " + errorMessage);
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = objectMapper.writeValueAsString(Map.of("error", errorMessage));
                } else {
                    // Преобразование DTO в Entity
                    TabulatedFunction newPointEntity = transformService.toEntity(pointRequest);

                    Long pointId = repository.createTabulatedFunction(newPointEntity);

                    if (pointId != null) {
                        // Обновляем ID у entity (если репозиторий не делает этого автоматически)
                        newPointEntity.setId(pointId);
                        // Преобразование Entity в Response DTO
                        TabulatedFunctionResponseDTO createdPointDto = transformService.toResponseDTO(newPointEntity);

                        httpStatus = HttpServletResponse.SC_CREATED;
                        responseData = objectMapper.writeValueAsString(createdPointDto);
                        logger.info("Создана новая точка функции: " + createdPointDto);
                    } else {
                        throw new SQLException("Не удалось создать точку функции");
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Ошибка при создании точки функции: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка при создании точки функции"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Внутренняя ошибка сервера"));
        }

        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseData);

        long endTime = System.currentTimeMillis();
        logger.info("POST запрос обработан за " + (endTime - startTime) + " мс");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("Обработка PUT запроса для обновления точки функции");

        int httpStatus = HttpServletResponse.SC_OK;
        String responseData = null;

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || !pathInfo.matches("/\\d+")) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = objectMapper.writeValueAsString(Map.of("error", "ID точки обязателен"));
            } else {
                Long pointId = Long.parseLong(pathInfo.substring(1));

                Optional<TabulatedFunction> existingPointOpt = repository.findById(pointId);
                if (existingPointOpt.isEmpty()) {
                    httpStatus = HttpServletResponse.SC_NOT_FOUND;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Точка функции не найдена"));
                    logger.warning("Попытка обновления несуществующей точки с ID: " + pointId);
                } else {
                    TabulatedFunction pointToUpdate = existingPointOpt.get();

                    String body = request.getReader().lines().collect(Collectors.joining());
                    logger.fine("Получено тело запроса для обновления: " + body);

                    TabulatedFunctionRequestDTO updateRequest = null;
                    try {
                        updateRequest = objectMapper.readValue(body, TabulatedFunctionRequestDTO.class);
                        logger.info("JSON успешно преобразован для обновления: " + updateRequest);
                    } catch (Exception e) {
                        logger.warning("Ошибка парсинга JSON для обновления: " + e.getMessage());
                        httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                        responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат JSON"));
                    }

                    if (updateRequest != null) {
                        Set<ConstraintViolation<TabulatedFunctionRequestDTO>> violations = validator.validate(updateRequest);
                        if (!violations.isEmpty()) {
                            String errorMessage = violations.stream()
                                    .map(ConstraintViolation::getMessage)
                                    .collect(Collectors.joining(", "));

                            logger.warning("Ошибки валидации при обновлении: " + errorMessage);
                            httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                            responseData = objectMapper.writeValueAsString(Map.of("error", errorMessage));
                        } else {
                            // Обновление полей Entity из DTO
                            pointToUpdate.setFunctionId(updateRequest.getFunctionId());
                            pointToUpdate.setXVal(updateRequest.getXVal());
                            pointToUpdate.setYVal(updateRequest.getYVal());

                            boolean updated = repository.updateTabulatedFunction(pointToUpdate);

                            if (updated) {
                                // Преобразование обновленной Entity в Response DTO
                                TabulatedFunctionResponseDTO responseDto = transformService.toResponseDTO(pointToUpdate);
                                responseData = objectMapper.writeValueAsString(responseDto);
                                logger.info("Точка функции обновлена: " + pointToUpdate.getId());
                            } else {
                                httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                                responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка при обновлении точки функции"));
                                logger.warning("Ошибка при обновлении точки функции с ID: " + pointId);
                            }
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            httpStatus = HttpServletResponse.SC_BAD_REQUEST;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат ID"));
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении точки функции: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка при обновлении точки функции"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Внутренняя ошибка сервера"));
        }

        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseData);

        long endTime = System.currentTimeMillis();
        logger.info("PUT запрос обработан за " + (endTime - startTime) + " мс");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("Обработка DELETE запроса для удаления точки функции");

        int httpStatus = HttpServletResponse.SC_OK;
        String responseData = null;

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                responseData = objectMapper.writeValueAsString(Map.of("error", "ID точки или функции обязательно"));
            } else {
                boolean deleted = false;
                String message = "Неизвестная ошибка";

                if (pathInfo.matches("/\\d+")) {
                    // DELETE /api/tabulated-points/{id} - удалить одну точку
                    Long pointId = Long.parseLong(pathInfo.substring(1));
                    deleted = repository.deleteTabulatedFunction(pointId); // Предполагается, что метод удаляет по ID точки
                    message = deleted ? "Точка удалена" : "Точка не найдена";
                } else if (pathInfo.matches("/function/\\d+")) {
                    // DELETE /api/tabulated-points/function/{functionId} - удалить все точки функции
                    Long functionId = Long.parseLong(pathInfo.substring(10)); // "/function/".length() = 10
                    List<TabulatedFunction> points = repository.findAllByFunctionId(functionId);
                    if (points.isEmpty()) {
                        deleted = false; // или true, если "ничего не удалено" считается успехом
                        message = "Точки функции не найдены";
                    } else {
                        deleted = repository.deleteAllTabulatedFunctions(functionId);
                        message = "Все точки функции удалены (" + points.size() + " точек)";
                    }
                } else {
                    httpStatus = HttpServletResponse.SC_BAD_REQUEST;
                    responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат URL"));
                    response.setStatus(httpStatus);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(responseData);
                    return;
                }

                if (deleted) {
                    httpStatus = HttpServletResponse.SC_OK;
                    responseData = objectMapper.writeValueAsString(Map.of("message", message));
                    logger.info("Удаление выполнено: " + message);
                } else {
                    httpStatus = HttpServletResponse.SC_NOT_FOUND; // или 200 OK, если "ничего не удалено" считается успехом
                    responseData = objectMapper.writeValueAsString(Map.of("error", message));
                }
            }
        } catch (NumberFormatException e) {
            httpStatus = HttpServletResponse.SC_BAD_REQUEST;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Неверный формат ID"));
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Ошибка при удалении"));
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка: " + e.getMessage());
            httpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseData = objectMapper.writeValueAsString(Map.of("error", "Внутренняя ошибка сервера"));
        }

        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseData);

        long endTime = System.currentTimeMillis();
        logger.info("DELETE запрос обработан за " + (endTime - startTime) + " мс");
    }

    // --- Вспомогательные методы ---
    // Эти методы возвращают Optional<String> (JSON) или Optional.empty() в случае ошибки/не найдено

    private Optional<String> getAllPointsByFunctionIdOpt(String pathInfo) {
        try {
            Long functionId = Long.parseLong(pathInfo.substring(9)); // "/function/".length() = 9
            List<TabulatedFunction> points = repository.findAllByFunctionId(functionId);

            if (points.isEmpty()) {
                logger.info("Точки не найдены для функции: " + functionId);
                return Optional.of("[]"); // Возвращаем пустой массив как строку
            }

            // Используем transformService для пакетного преобразования
            List<TabulatedFunctionResponseDTO> responsePoints = transformService.toResponseDTOs(points);

            String jsonArray = objectMapper.writeValueAsString(responsePoints);
            logger.info("Найдено точек для функции " + functionId + ": " + points.size());
            return Optional.of(jsonArray);

        } catch (NumberFormatException e) {
            logger.warning("Неверный формат functionId: " + pathInfo);
            return Optional.of("{\"error\": \"Неверный формат ID функции\"}");
        } catch (SQLException e) {
            logger.severe("Ошибка БД при поиске точек функции: " + e.getMessage());
            return Optional.of("{\"error\": \"Ошибка базы данных\"}");
        } catch (Exception e) {
            logger.severe("Ошибка при сериализации точек в JSON: " + e.getMessage());
            return Optional.of("{\"error\": \"Ошибка сервера при обработке данных\"}");
        }
    }

    private Optional<String> getPointByXValueOpt(String pathInfo) {
        try {
            // /function/{functionId}/x/{xVal}
            String[] parts = pathInfo.split("/");
            Long functionId = Long.parseLong(parts[2]);
            Double xVal = Double.parseDouble(parts[4]);

            Optional<TabulatedFunction> pointOpt = repository.findByXValue(functionId, xVal);

            if (pointOpt.isPresent()) {
                // Используем transformService для преобразования
                TabulatedFunctionResponseDTO responseDto = transformService.toResponseDTO(pointOpt.get());
                String pointJson = objectMapper.writeValueAsString(responseDto);
                logger.info("Найдена точка по functionId: " + functionId + ", xVal: " + xVal);
                return Optional.of(pointJson);
            } else {
                logger.info("Точка не найдена по functionId: " + functionId + ", xVal: " + xVal);
                return Optional.empty(); // Возвращаем пустой Optional, вызывающий метод установит 404
            }

        } catch (Exception e) {
            logger.warning("Ошибка при парсинге пути или параметров: " + e.getMessage());
            return Optional.of("{\"error\": \"Неверный формат URL\"}"); // Возвращаем JSON ошибки как строку
        }
    }

    private Optional<String> getPointsInRangeOpt(String pathInfo, HttpServletRequest request) {
        try {
            // /function/{functionId}/range?fromX={min}&toX={max}
            String[] parts = pathInfo.split("/");
            Long functionId = Long.parseLong(parts[2]);

            String fromXStr = request.getParameter("fromX");
            String toXStr = request.getParameter("toX");

            if (fromXStr == null || toXStr == null) {
                return Optional.of("{\"error\": \"Параметры fromX и toX обязательны\"}");
            }

            Double fromX = Double.parseDouble(fromXStr);
            Double toX = Double.parseDouble(toXStr);

            if (fromX > toX) {
                return Optional.of("{\"error\": \"fromX не может быть больше toX\"}");
            }

            List<TabulatedFunction> points = repository.findBetweenXValues(functionId, fromX, toX);

            // Используем transformService для пакетного преобразования
            List<TabulatedFunctionResponseDTO> responsePoints = transformService.toResponseDTOs(points);

            String jsonArray = objectMapper.writeValueAsString(responsePoints);
            logger.info("Найдено " + points.size() + " точек в диапазоне для функции " + functionId + " (" + fromX + " - " + toX + ")");
            return Optional.of(jsonArray);

        } catch (NumberFormatException e) {
            return Optional.of("{\"error\": \"Неверный формат параметров\"}");
        } catch (Exception e) {
            logger.warning("Ошибка при поиске в диапазоне: " + e.getMessage());
            return Optional.of("{\"error\": \"Ошибка при поиске в диапазоне\"}");
        }
    }

    @Override
    public void destroy() {
        logger.info("TabulatedFunctionPointServlet уничтожается");
        super.destroy();
    }
}