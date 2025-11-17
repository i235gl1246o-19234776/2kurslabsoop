package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.TabulatedFunctionRequestDTO;
import model.dto.response.TabulatedFunctionResponseDTO;
import model.entity.TabulatedFunction;
import model.entity.User;
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
public class TabulatedFunctionServlet extends AuthServlet {

        private static final Logger logger = Logger.getLogger(TabulatedFunctionServlet.class.getName());
        private final TabulatedFunctionService tabulatedFunctionService;
        private final ObjectMapper objectMapper;

        public TabulatedFunctionServlet() {
            this.tabulatedFunctionService = new TabulatedFunctionService();
            this.objectMapper = new ObjectMapper();
        }

        public TabulatedFunctionServlet(TabulatedFunctionService tabulatedFunctionService) {
            this.tabulatedFunctionService = tabulatedFunctionService;
            this.objectMapper = new ObjectMapper();
        }

        // ======================= ДОБАВЛЕНО: проверка прав доступа =======================

        private boolean checkFunctionAccess(HttpServletRequest req, Long functionId) throws SQLException, IOException {
            User user = getAuthenticatedUser(req);

            // USER может только свои функции
            if (isUser(req)) {
                boolean allowed = tabulatedFunctionService.isFunctionOwnedByUser(functionId, user.getId());
                if (!allowed) {
                    sendError(resp(req), 403, "Users may access only their own functions");
                    return false;
                }
            }
            return true;
        }

        private boolean checkPointAccess(HttpServletRequest req, Long pointId) throws SQLException, IOException {
            Optional<Long> functionIdOpt = tabulatedFunctionService.getFunctionIdByPointId(pointId);

            if (functionIdOpt.isEmpty()) {
                sendError(resp(req), 404, "Point not found");
                return false;
            }

            return checkFunctionAccess(req, functionIdOpt.get());
        }

        // Чтобы можно было использовать resp в методах
        private HttpServletResponse resp(HttpServletRequest req) {
            return (HttpServletResponse) req.getAttribute("resp");
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

        // ================================================================================


        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String pathInfo = req.getPathInfo();

            try {
                if (pathInfo == null || pathInfo.equals("/")) {
                    // GET /api/tabulated-points?functionId=1
                    Long functionId = Long.parseLong(req.getParameter("functionId"));
                    if (!checkFunctionAccess(req, functionId)) return;

                    handleGetByRange(req, resp);
                    return;
                }

                String[] p = pathInfo.split("/");

                if (p.length == 2) {
                    Long pointId = Long.parseLong(p[1]);
                    if (!checkPointAccess(req, pointId)) return;

                    handleGetPointById(p[1], resp);
                    return;
                }

                if (p.length == 3 && "function".equals(p[1])) {
                    Long functionId = Long.parseLong(p[2]);
                    if (!checkFunctionAccess(req, functionId)) return;

                    handleGetPointsByFunctionId(p[2], resp);
                    return;
                }

                if (p.length == 5 && "function".equals(p[1]) && "x".equals(p[3])) {
                    Long functionId = Long.parseLong(p[2]);
                    if (!checkFunctionAccess(req, functionId)) return;

                    handleGetPointByXValue(p[2], p[4], resp);
                    return;
                }

                if (p.length == 4 && "function".equals(p[1]) && "range".equals(p[3])) {
                    Long functionId = Long.parseLong(p[2]);
                    if (!checkFunctionAccess(req, functionId)) return;

                    handleGetPointsInRange(p[2], req, resp);
                    return;
                }

                sendError(resp, 400, "Invalid GET path");

            } catch (NumberFormatException e) {
                sendError(resp, 400, "Invalid numeric parameter");
            } catch (SQLException e) {
                sendError(resp, 500, "Database error");
            }
        }


        // ===================== POST ========================

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try {
                TabulatedFunctionRequestDTO dto = parseJsonRequest(req, TabulatedFunctionRequestDTO.class);

                if (!checkFunctionAccess(req, dto.getFunctionId())) return;

                TabulatedFunctionResponseDTO result = tabulatedFunctionService.createTabulatedFunction(dto);
                sendJsonResponse(resp, 201, result);

            } catch (SQLException e) {
                sendError(resp, 500, "DB error");
            }
        }


        // ===================== PUT ========================

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(resp, 400, "Path required");
                return;
            }

            String[] p = pathInfo.split("/");
            if (p.length != 2) {
                sendError(resp, 400, "Invalid PUT path");
                return;
            }

            try {
                Long pointId = Long.parseLong(p[1]);

                if (!checkPointAccess(req, pointId)) return;

                TabulatedFunction tf = parseJsonRequest(req, TabulatedFunction.class);

                if (!pointId.equals(tf.getId())) {
                    sendError(resp, 400, "ID mismatch");
                    return;
                }

                boolean updated = tabulatedFunctionService.updateTabulatedFunction(tf);

                if (updated)
                    sendMessage(resp, 200, "Updated");
                else
                    sendError(resp, 404, "Not found");

            } catch (SQLException e) {
                sendError(resp, 500, "DB error");
            }
        }


        // ===================== DELETE ========================

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(resp, 400, "Path required");
                return;
            }

            String[] p = pathInfo.split("/");

            try {

                if (p.length == 2) {
                    Long pointId = Long.parseLong(p[1]);

                    if (!checkPointAccess(req, pointId)) return;

                    boolean deleted = tabulatedFunctionService.deleteTabulatedFunction(pointId);

                    if (deleted) sendMessage(resp, 200, "Deleted");
                    else sendError(resp, 404, "Not found");

                    return;
                }

                if (p.length == 3 && "function".equals(p[1])) {
                    Long functionId = Long.parseLong(p[2]);

                    if (!checkFunctionAccess(req, functionId)) return;

                    tabulatedFunctionService.deleteAllTabulatedFunctions(functionId);
                    sendMessage(resp, 200, "All points deleted");
                    return;
                }

                sendError(resp, 400, "Invalid DELETE path");

            } catch (SQLException e) {
                sendError(resp, 500, "DB error");
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