package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.TabulatedFunctionRequestDTO;
import model.dto.response.TabulatedFunctionResponseDTO;
import model.entity.TabulatedFunction;
import model.service.TabulatedFunctionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet("/api/tabulated-points/*")
public class TabulatedFunctionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(TabulatedFunctionServlet.class.getName());
    private final TabulatedFunctionService tabulatedFunctionService;
    private final ObjectMapper objectMapper;

    public TabulatedFunctionServlet() {
        this.tabulatedFunctionService = new TabulatedFunctionService();
        this.objectMapper = new ObjectMapper();
    }

    // Конструктор для тестирования
    public TabulatedFunctionServlet(TabulatedFunctionService tabulatedFunctionService) {
        this.tabulatedFunctionService = tabulatedFunctionService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/tabulated-points?functionId=1&fromX=0&toX=10
            try {
                handleGetByRange(req, resp);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        String[] pathParts = pathInfo.split("/");

        try {
            if (pathParts.length == 2) {
                // GET /api/tabulated-points/{id} - поиск точки по ID
                handleGetPointById(pathParts[1], resp);
            } else if (pathParts.length == 3 && "function".equals(pathParts[1])) {
                // GET /api/tabulated-points/function/{functionId} - поиск всех точек функции
                handleGetPointsByFunctionId(pathParts[2], resp);
            } else if (pathParts.length == 5 && "function".equals(pathParts[1]) && "x".equals(pathParts[3])) {
                // GET /api/tabulated-points/function/{functionId}/x/{xValue} - поиск точки по X
                handleGetPointByXValue(pathParts[2], pathParts[4], resp);
            } else if (pathParts.length == 4 && "function".equals(pathParts[1]) && "range".equals(pathParts[3])) {
                // GET /api/tabulated-points/function/{functionId}/range - поиск точек в диапазоне X
                handleGetPointsInRange(pathParts[2], req, resp);
            } else if (pathParts.length == 3 && "search".equals(pathParts[1])) {
                // GET /api/tabulated-points/search/{functionId} - поиск функции по ID
                handleGetFunctionById(pathParts[2], resp);
            } else if (pathParts.length == 4 && "search".equals(pathParts[1]) && "x".equals(pathParts[3])) {
                // GET /api/tabulated-points/search/{functionId}/x - поиск функции по значению X
                handleGetFunctionByXValue(pathParts[2], req, resp);
            } else if (pathParts.length == 4 && "search".equals(pathParts[1]) && "range".equals(pathParts[3])) {
                // GET /api/tabulated-points/search/{functionId}/range - поиск функций в диапазоне X
                handleGetFunctionsInRange(pathParts[2], req, resp);
            } else {
                logger.warning("Неверный путь для GET: " + pathInfo);
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный путь для GET");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат числового параметра: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат числового параметра");
        } catch (SQLException e) {
            logger.severe("Ошибка базы данных: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        }
    }

    private void handleGetByRange(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {
        String functionIdParam = req.getParameter("functionId");
        String fromXParam = req.getParameter("fromX");
        String toXParam = req.getParameter("toX");

        if (functionIdParam == null || functionIdParam.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
            return;
        }

        Long functionId = Long.parseLong(functionIdParam);
        Double fromX = parseDoubleParameter(fromXParam);
        Double toX = parseDoubleParameter(toXParam);

        if (fromX == null || toX == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметры fromX и toX обязательны");
            return;
        }

        logger.info("GET по диапазону X для functionId: " + functionId + ", диапазон: " + fromX + " - " + toX);
        List<TabulatedFunctionResponseDTO> points = tabulatedFunctionService.getTabulatedFunctionsBetweenXValues(functionId, fromX, toX);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, points);
    }

    private void handleGetPointById(String idStr, HttpServletResponse resp)
            throws IOException, SQLException {
        Long pointId = Long.parseLong(idStr);
        logger.info("GET точки по ID: " + pointId);

        Optional<TabulatedFunctionResponseDTO> point = tabulatedFunctionService.getTabulatedFunctionById(pointId);

        if (point.isPresent()) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK, point.get());
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Точка с ID=" + pointId + " не найдена");
        }
    }

    private void handleGetPointsByFunctionId(String functionIdStr, HttpServletResponse resp)
            throws IOException, SQLException {
        Long functionId = Long.parseLong(functionIdStr);
        logger.info("GET точек по functionId: " + functionId);

        List<TabulatedFunctionResponseDTO> points = tabulatedFunctionService.getTabulatedFunctionsByFunctionId(functionId);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, points);
    }

    private void handleGetPointByXValue(String functionIdStr, String xValueStr, HttpServletResponse resp)
            throws IOException, SQLException {
        Long functionId = Long.parseLong(functionIdStr);
        Double xValue = Double.parseDouble(xValueStr);
        logger.info("GET точки по functionId: " + functionId + " и X: " + xValue);

        Optional<TabulatedFunctionResponseDTO> point = tabulatedFunctionService.getTabulatedFunctionByXValue(functionId, xValue);

        if (point.isPresent()) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK, point.get());
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND,
                    "Точка с X=" + xValue + " не найдена для функции " + functionId);
        }
    }

    private void handleGetPointsInRange(String functionIdStr, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {
        Long functionId = Long.parseLong(functionIdStr);
        String fromXParam = req.getParameter("fromX");
        String toXParam = req.getParameter("toX");

        Double fromX = parseDoubleParameter(fromXParam);
        Double toX = parseDoubleParameter(toXParam);

        if (fromX == null || toX == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметры fromX и toX обязательны");
            return;
        }

        logger.info("GET точек в диапазоне для functionId: " + functionId + ", диапазон: " + fromX + " - " + toX);
        List<TabulatedFunctionResponseDTO> points = tabulatedFunctionService.getTabulatedFunctionsBetweenXValues(functionId, fromX, toX);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, points);
    }

    // НОВЫЕ МЕТОДЫ ДЛЯ ПОИСКА ФУНКЦИЙ

    private void handleGetFunctionById(String functionIdStr, HttpServletResponse resp)
            throws IOException, SQLException {
        Long functionId = Long.parseLong(functionIdStr);
        logger.info("GET функции по ID: " + functionId);

        // Предполагаем, что в сервисе есть метод getFunctionById
        Optional<TabulatedFunctionResponseDTO> function = tabulatedFunctionService.getTabulatedFunctionById(functionId);

        if (function.isPresent()) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK, function.get());
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Функция с ID=" + functionId + " не найдена");
        }
    }

    private void handleGetFunctionByXValue(String functionIdStr, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {
        Long functionId = Long.parseLong(functionIdStr);
        String xValueParam = req.getParameter("x");

        if (xValueParam == null || xValueParam.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр x обязателен");
            return;
        }

        Double xValue = Double.parseDouble(xValueParam);
        logger.info("GET функции по ID: " + functionId + " и значению X: " + xValue);

        // Предполагаем, что в сервисе есть метод getFunctionByXValue
        Optional<TabulatedFunctionResponseDTO> function = tabulatedFunctionService.getTabulatedFunctionByXValue(functionId, xValue);

        if (function.isPresent()) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK, function.get());
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND,
                    "Функция с ID=" + functionId + " и значением X=" + xValue + " не найдена");
        }
    }

    private void handleGetFunctionsInRange(String functionIdStr, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {
        Long functionId = Long.parseLong(functionIdStr);
        String fromXParam = req.getParameter("fromX");
        String toXParam = req.getParameter("toX");

        Double fromX = parseDoubleParameter(fromXParam);
        Double toX = parseDoubleParameter(toXParam);

        if (fromX == null || toX == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметры fromX и toX обязательны");
            return;
        }

        logger.info("GET функций в диапазоне для functionId: " + functionId + ", диапазон: " + fromX + " - " + toX);

        // Предполагаем, что в сервисе есть метод getFunctionsInRange
        List<TabulatedFunctionResponseDTO> functions = tabulatedFunctionService.getTabulatedFunctionsBetweenXValues(functionId, fromX, toX);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, functions);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /api/tabulated-points вызван");

        try {
            TabulatedFunctionRequestDTO request = parseJsonRequest(req, TabulatedFunctionRequestDTO.class);
            TabulatedFunctionResponseDTO response = tabulatedFunctionService.createTabulatedFunction(request);

            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, response);
        } catch (IOException e) {
            logger.warning("Неверный формат JSON в теле запроса: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON");
        } catch (SQLException e) {
            logger.severe("Ошибка при создании точки: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при создании точки");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Путь для PUT запроса отсутствует");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 2) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный путь для PUT");
            return;
        }

        try {
            Long id = Long.parseLong(pathParts[1]);
            TabulatedFunction tabulatedFunction = parseJsonRequest(req, TabulatedFunction.class);

            // Убедимся, что ID в сущности совпадает с URL
            if (!id.equals(tabulatedFunction.getId())) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID в пути и теле запроса не совпадают");
                return;
            }

            logger.info("PUT точки с ID: " + id);
            boolean updated = tabulatedFunctionService.updateTabulatedFunction(tabulatedFunction);

            if (updated) {
                sendMessage(resp, HttpServletResponse.SC_OK, "Точка обновлена");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Точка не найдена для обновления");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат ID точки: " + pathParts[1]);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID точки");
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении точки: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при обновлении точки");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Путь для DELETE запроса отсутствует");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        try {
            if (pathParts.length == 2) {
                // DELETE /api/tabulated-points/{id}
                handleDeletePointById(pathParts[1], resp);
            } else if (pathParts.length == 3 && "function".equals(pathParts[1])) {
                // DELETE /api/tabulated-points/function/{functionId}
                handleDeletePointsByFunctionId(pathParts[2], resp);
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный путь для DELETE");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат числового параметра: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат числового параметра");
        } catch (SQLException e) {
            logger.severe("Ошибка при удалении: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при удалении");
        }
    }

    private void handleDeletePointById(String idStr, HttpServletResponse resp)
            throws IOException, SQLException {
        Long id = Long.parseLong(idStr);
        logger.info("DELETE точки с ID: " + id);

        boolean deleted = tabulatedFunctionService.deleteTabulatedFunction(id);

        if (deleted) {
            sendMessage(resp, HttpServletResponse.SC_OK, "Точка удалена");
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Точка не найдена для удаления");
        }
    }

    private void handleDeletePointsByFunctionId(String functionIdStr, HttpServletResponse resp)
            throws IOException, SQLException {
        Long functionId = Long.parseLong(functionIdStr);
        logger.info("DELETE всех точек функции с ID: " + functionId);

        boolean deleted = tabulatedFunctionService.deleteAllTabulatedFunctions(functionId);

        if (deleted) {
            sendMessage(resp, HttpServletResponse.SC_OK, "Все точки функции удалены");
        } else {
            sendMessage(resp, HttpServletResponse.SC_OK, "Запрос обработан, возможно, точек не было");
        }
    }

    // Вспомогательные методы
    private <T> T parseJsonRequest(HttpServletRequest req, Class<T> valueType) throws IOException {
        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }
        return objectMapper.readValue(jsonBuffer.toString(), valueType);
    }

    private void sendJsonResponse(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(objectMapper.writeValueAsString(data));
        }
    }

    private void sendError(HttpServletResponse resp, int status, String errorMessage) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"error\":\"" + errorMessage + "\"}");
        }
    }

    private void sendMessage(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"message\":\"" + message + "\"}");
        }
    }

    private Double parseDoubleParameter(String param) {
        if (param != null && !param.isEmpty()) {
            try {
                return Double.parseDouble(param);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}