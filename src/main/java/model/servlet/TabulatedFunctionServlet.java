package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import functions.IdentityFunction;
import functions.MathFunction;
import functions.SqrFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import model.dto.request.TabulatedFunctionRequestDTO;
import model.dto.response.TabulatedFunctionResponseDTO;
import model.entity.TabulatedFunction;
import model.entity.User;
import model.service.TabulatedFunctionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet("/api/tabulated-points/*")
public class TabulatedFunctionServlet extends AuthServlet {

    private static final Logger logger = Logger.getLogger(TabulatedFunctionServlet.class.getName());
    private final TabulatedFunctionService tabulatedFunctionService;
    private final ObjectMapper objectMapper;

    // --- НОВОЕ: Карта сопоставления названий -> объектов MathFunction ---
    // Лучше вынести в отдельный класс, например, MathFunctionRegistry
    private static final Map<String, MathFunction> MATH_FUNCTION_MAP = new HashMap<>();
    static {
        // Зарегистрируйте все ваши MathFunction без аргументов конструктора
        MATH_FUNCTION_MAP.put("Тождественная функция", new IdentityFunction());
        MATH_FUNCTION_MAP.put("Квадратичная функция", new SqrFunction());
        // MATH_FUNCTION_MAP.put("Экспоненциальная функция", new ExpFunction());
        // ... добавьте остальные
        // ВАЖНО: Названия должны быть такими, как вы их хотите видеть в выпадающем списке на фронтенде
        // И ключи должны совпадать с теми, что приходят в JSON от фронтенда (mathFunctionName)
        // Пример (замените на реальные классы):
        MATH_FUNCTION_MAP.put("IdentityFunction", new IdentityFunction());
        MATH_FUNCTION_MAP.put("SqrFunction", new SqrFunction());
        // Для локализованных названий, лучше создать отдельный класс-реестр.
        // Здесь для примера используем имя класса.
        // В реальном приложении, возможно, лучше использовать Enum.
    }
    // --- КОНЕЦ НОВОГО ---

    public TabulatedFunctionServlet() {
        this.tabulatedFunctionService = new TabulatedFunctionService();
        this.objectMapper = new ObjectMapper();
    }

    public TabulatedFunctionServlet(TabulatedFunctionService tabulatedFunctionService) {
        this.tabulatedFunctionService = tabulatedFunctionService;
        this.objectMapper = new ObjectMapper();
    }

    private boolean checkFunctionAccess(HttpServletRequest req, Long functionId) throws SQLException, IOException {
        User user = getAuthenticatedUser(req);

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        // --- НОВЫЙ МЕТОД: Обработка /calculate ---
        if (pathInfo != null && pathInfo.equals("/calculate")) {
            handleCalculateAndSavePoints(req, resp);
            return;
        }
        // --- КОНЕЦ НОВОГО МЕТОДА ---

        // --- СТАРЫЙ МЕТОД: Обработка создания точек из массива ---
        try {
            TabulatedFunctionRequestDTO dto = parseJsonRequest(req, TabulatedFunctionRequestDTO.class);

            if (!checkFunctionAccess(req, dto.getFunctionId())) return;

            TabulatedFunctionResponseDTO result = tabulatedFunctionService.createTabulatedFunction(dto);
            sendJsonResponse(resp, 201, result);

        } catch (SQLException e) {
            sendError(resp, 500, "DB error");
        }
    }

    // --- НОВЫЙ МЕТОД: Вычисление и сохранение точек ---
    private void handleCalculateAndSavePoints(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Читаем тело запроса
            StringBuilder jsonBuffer = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }
            String jsonString = jsonBuffer.toString();
            logger.info("Получен запрос на вычисление точек: " + jsonString);

            // Парсим JSON
            // Создадим DTO для этого запроса
            CalculatePointsRequestDTO requestDTO = objectMapper.readValue(jsonString, CalculatePointsRequestDTO.class);

            // Проверяем доступ к функции
            if (!checkFunctionAccess(req, requestDTO.getFunctionId())) return;

            // Получаем MathFunction по имени
            MathFunction mathFunction = MATH_FUNCTION_MAP.get(requestDTO.getMathFunctionName());
            if (mathFunction == null) {
                logger.warning("Unknown MathFunction name: " + requestDTO.getMathFunctionName());
                sendError(resp, 400, "Unknown MathFunction name: " + requestDTO.getMathFunctionName());
                return;
            }

            // Проверяем параметры интервала
            if (requestDTO.getStart() >= requestDTO.getEnd() || requestDTO.getCount() <= 0) {
                logger.warning("Invalid interval parameters: start=" + requestDTO.getStart() + ", end=" + requestDTO.getEnd() + ", count=" + requestDTO.getCount());
                sendError(resp, 400, "Invalid interval parameters: start must be less than end, count must be positive.");
                return;
            }

            // --- ОПРЕДЕЛЯЕМ ФАБРИКУ ПО ТИПУ ---
            TabulatedFunctionFactory factory;
            if ("linked-list".equals(requestDTO.getFactoryType())) {
                factory = new LinkedListTabulatedFunctionFactory();
            } else {
                // По умолчанию используем ArrayTabulatedFunctionFactory
                factory = new ArrayTabulatedFunctionFactory();
            }
            // --- КОНЕЦ ОПРЕДЕЛЕНИЯ ФАБРИКИ ---

            // Вызываем сервис для вычисления и сохранения точек, передавая фабрику
            tabulatedFunctionService.calculateAndSaveTabulatedPoints(
                    requestDTO.getFunctionId(),
                    mathFunction,
                    requestDTO.getStart(),
                    requestDTO.getEnd(),
                    requestDTO.getCount(),
                    factory // <-- ПЕРЕДАЁМ ФАБРИКУ
            );

            // Отправляем успешный ответ
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"message\":\"Tabulated points calculated and saved successfully\"}");

        } catch (IOException e) {
            logger.severe("Error reading request body for calculate points: " + e.getMessage());
            sendError(resp, 400, "Invalid JSON format");
        } catch (SQLException e) {
            logger.severe("Database error during calculate and save points: " + e.getMessage());
            sendError(resp, 500, "Database error");
        } catch (Exception e) {
            logger.severe("Unexpected error during calculate and save points: " + e.getMessage());
            sendError(resp, 500, "Internal server error");
        }
    }
// --- КОНЕЦ НОВОГО МЕТОДА ---


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

    // --- ВСПОМОГАТЕЛЬНЫЙ DTO ДЛЯ ЗАПРОСА ВЫЧИСЛЕНИЯ ТОЧЕК ---
    private static class CalculatePointsRequestDTO {
        private Long functionId;
        private String mathFunctionName; // Имя функции из MATH_FUNCTION_MAP
        private double start;
        private double end;
        private int count;
        private String factoryType;

        // Геттеры и сеттеры
        public Long getFunctionId() {
            return functionId;
        }

        public void setFunctionId(Long functionId) {
            this.functionId = functionId;
        }

        public String getMathFunctionName() {
            return mathFunctionName;
        }

        public void setMathFunctionName(String mathFunctionName) {
            this.mathFunctionName = mathFunctionName;
        }

        public double getStart() {
            return start;
        }

        public void setStart(double start) {
            this.start = start;
        }

        public double getEnd() {
            return end;
        }

        public void setEnd(double end) {
            this.end = end;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getFactoryType() { return factoryType.toString(); }

    }
    // --- КОНЕЦ ВСПОМОГАТЕЛЬНОГО DTO ---
}