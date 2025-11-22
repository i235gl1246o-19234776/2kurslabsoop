package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import model.dto.PointDTO;
import model.dto.TabulatedFunctionDifferentialOperator;
import model.dto.request.DifferentiateRequestDTO;
import model.dto.request.ExecuteOperationRequestDTO;
import model.dto.request.OperationRequestDTO;
import model.dto.response.ExecuteOperationResponseDTO;
import model.dto.response.OperationResponseDTO;
import model.entity.Operation;
import model.entity.User;
import model.service.OperationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.service.TabulatedFunctionService;
import operations.TabulatedFunctionOperationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@WebServlet("/api/operations/*")
public class OperationServlet extends AuthServlet {

    private static final Logger logger = Logger.getLogger(OperationServlet.class.getName());
    private final OperationService operationService;
    private final ObjectMapper objectMapper;

    public OperationServlet() {
        this.operationService = new OperationService();
        this.objectMapper = new ObjectMapper();
    }

    public OperationServlet(OperationService operationService) {
        this.operationService = operationService;
        this.objectMapper = new ObjectMapper();
    }

    // ======================
    // Обработка: POST /api/operations/execute
    // ======================
    private void handleExecuteOperation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("POST /api/operations/execute вызван");

        String body = readRequestBody(req);
        try {
            ExecuteOperationRequestDTO request = objectMapper.readValue(body, ExecuteOperationRequestDTO.class);

            validateExecuteRequest(request);
            if (!checkFunctionAccess(req, request.getFunctionIdA()) ||
                    !checkFunctionAccess(req, request.getFunctionIdB())) {
                return;
            }

            TabulatedFunctionService service = new TabulatedFunctionService();
            TabulatedFunction funcA = service.loadTabulatedFunction(request.getFunctionIdA());
            TabulatedFunction funcB = service.loadTabulatedFunction(request.getFunctionIdB());

            if (funcA == null || funcB == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Одна из функций не найдена");
                return;
            }

            TabulatedFunctionFactory factory = createFactory(request.getFactoryType());
            TabulatedFunctionOperationService opService = new TabulatedFunctionOperationService(factory);
            TabulatedFunction result = performOperation(opService, funcA, funcB, request.getOperation());

            ExecuteOperationResponseDTO responseDTO = buildExecuteResponse(request, result);
            writeJsonResponse(resp, responseDTO);

        } catch (IOException e) {
            logger.warning("Ошибка парсинга JSON в /execute: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON");
        } catch (SQLException e) {
            logger.severe("Ошибка БД в /execute: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        } catch (IllegalArgumentException | ArithmeticException e) {
            logger.warning("Ошибка операции: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка в /execute: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    // ======================
    // Обработка: POST /api/operations/differentiate
    // ======================
    private void handleDifferentiate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("POST /api/operations/differentiate вызван");

        String body = readRequestBody(req);
        try {
            DifferentiateRequestDTO request = objectMapper.readValue(body, DifferentiateRequestDTO.class);

            if (request.getFunctionId() == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "functionId обязателен");
                return;
            }

            if (!checkFunctionAccess(req, request.getFunctionId())) {
                return;
            }

            TabulatedFunctionService service = new TabulatedFunctionService();
            TabulatedFunction func = service.loadTabulatedFunction(request.getFunctionId());

            if (func == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Функция не найдена");
                return;
            }

            TabulatedFunctionFactory factory = createFactory(request.getFactoryType());
            TabulatedFunctionDifferentialOperator diffOp = new TabulatedFunctionDifferentialOperator(factory);
            TabulatedFunction derivative = diffOp.derive(func);

            ExecuteOperationResponseDTO responseDTO = new ExecuteOperationResponseDTO();
            responseDTO.setPoints(buildPointList(derivative));

            writeJsonResponse(resp, responseDTO);

        } catch (IOException e) {
            logger.warning("Неверный формат JSON в /differentiate: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON");
        } catch (SQLException e) {
            logger.severe("Ошибка БД в /differentiate: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка базы данных");
        } catch (IllegalArgumentException | ArithmeticException e) {
            logger.warning("Ошибка дифференцирования: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.severe("Неожиданная ошибка в /differentiate: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        }
    }

    // ======================
    // Вспомогательные методы
    // ======================

    private String readRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }
        return buffer.toString();
    }

    private void validateExecuteRequest(ExecuteOperationRequestDTO request) throws IOException {
        if (request.getFunctionIdA() == null || request.getFunctionIdB() == null) {
            throw new IOException("functionIdA и functionIdB обязательны");
        }
        if (request.getOperation() == null || request.getOperation().isEmpty()) {
            throw new IOException("Поле 'operation' обязательно");
        }
    }

    private TabulatedFunctionFactory createFactory(String factoryType) {
        if ("linked-list".equals(factoryType)) {
            return new LinkedListTabulatedFunctionFactory();
        }
        return new ArrayTabulatedFunctionFactory();
    }

    private TabulatedFunction performOperation(
            TabulatedFunctionOperationService opService,
            TabulatedFunction a,
            TabulatedFunction b,
            String op
    ) {
        String operation = op.toLowerCase();
        return switch (operation) {
            case "add" -> opService.add(a, b);
            case "subtract" -> opService.subtract(a, b);
            case "multiply" -> opService.multiply(a, b);
            case "divide" -> opService.divide(a, b);
            default -> throw new IllegalArgumentException("Неизвестная операция: " + op);
        };
    }

    private ExecuteOperationResponseDTO buildExecuteResponse(ExecuteOperationRequestDTO req, TabulatedFunction result) {
        ExecuteOperationResponseDTO dto = new ExecuteOperationResponseDTO();
        dto.setFunctionIdA(req.getFunctionIdA());
        dto.setFunctionIdB(req.getFunctionIdB());
        dto.setOperation(req.getOperation());
        dto.setPoints(buildPointList(result));
        return dto;
    }

    private List<PointDTO> buildPointList(TabulatedFunction func) {
        List<PointDTO> points = new ArrayList<>();
        for (int i = 0; i < func.getCount(); i++) {
            PointDTO p = new PointDTO();
            p.setX(func.getX(i));
            p.setY(func.getY(i));
            points.add(p);
        }
        return points;
    }

    private void writeJsonResponse(HttpServletResponse resp, Object dto) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(objectMapper.writeValueAsString(dto));
        }
    }

    private void sendError(HttpServletResponse resp, int status, String errorMessage) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.printf("{\"error\":\"%s\"}%n", errorMessage);
        }
    }

    protected void sendUnauthorized(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"error\":\"Unauthorized\"}");
        }
    }

    private boolean checkFunctionAccess(HttpServletRequest req, Long functionId) throws SQLException, IOException {
        User user = getAuthenticatedUser(req);
        boolean allowed = !isUser(req) || operationService.isFunctionOwnedByUser(functionId, user.getId());
        if (!allowed) {
            sendError((HttpServletResponse) req.getAttribute("resp"), 403,
                    "Users may access only operations of their own functions");
        }
        return allowed;
    }

    // ======================
    // Servlet lifecycle
    // ======================

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if ("/execute".equals(pathInfo)) {
            handleExecuteOperation(req, resp);
            return;
        }
        if ("/differentiate".equals(pathInfo)) {
            handleDifferentiate(req, resp);
            return;
        }

        // POST /api/operations — сохранение записи об операции
        logger.info("POST /api/operations вызван");
        String body = readRequestBody(req);
        try {
            OperationRequestDTO dto = objectMapper.readValue(body, OperationRequestDTO.class);
            if (dto.getFunctionId() == null) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
                return;
            }
            if (!checkFunctionAccess(req, dto.getFunctionId())) {
                return;
            }

            OperationResponseDTO response = operationService.createOperation(dto);
            writeJsonResponse(resp, response);
            resp.setStatus(HttpServletResponse.SC_CREATED);

        } catch (IOException e) {
            logger.warning("Неверный формат JSON: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON");
        } catch (SQLException e) {
            logger.severe("Ошибка при создании операции: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при создании операции");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String functionIdParam = req.getParameter("functionId");

        if (functionIdParam == null || functionIdParam.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
            return;
        }

        Long functionId;
        try {
            functionId = Long.parseLong(functionIdParam);
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат functionId");
            return;
        }

        try {
            if (!checkFunctionAccess(req, functionId)) return;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (pathInfo == null || pathInfo.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Путь отсутствует");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length == 2) {
            try {
                Long id = Long.parseLong(parts[1]);
                if (!checkOperationAccess(req, id)) return;

                Optional<OperationResponseDTO> op = operationService.getOperationById(id, functionId);
                if (op.isPresent()) {
                    writeJsonResponse(resp, op.get());
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Операция не найдена");
                }
            } catch (NumberFormatException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID операции");
            } catch (SQLException e) {
                logger.severe("Ошибка при получении операции: " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при получении операции");
            }
        } else {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный путь для GET");
        }
    }

    private boolean checkOperationAccess(HttpServletRequest req, Long operationId) throws SQLException, IOException {
        User user = getAuthenticatedUser(req);
        boolean allowed = !isUser(req) || operationService.isOperationOwnedByUser(operationId, user.getId());
        if (!allowed) {
            sendError((HttpServletResponse) req.getAttribute("resp"), 403,
                    "Users may access only their own operations");
        }
        return allowed;
    }

    // ======================
    // PUT / DELETE — без изменений (оставлены для полноты)
    // ======================

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            logger.warning("Путь для PUT запроса отсутствует");
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Путь для PUT запроса отсутствует");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 2) {
            logger.warning("Неверный путь для PUT: " + pathInfo);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный путь для PUT");
            return;
        }

        try {
            Long id = Long.parseLong(pathParts[1]);

            if (!checkOperationAccess(req, id)) {
                return;
            }

            StringBuilder jsonBuffer = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }

            Operation operation = objectMapper.readValue(jsonBuffer.toString(), Operation.class);
            if (!id.equals(operation.getId())) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID в URL не совпадает с ID в теле запроса");
                return;
            }

            logger.info("PUT /api/operations/" + id + " вызван");

            boolean updated = operationService.updateOperation(operation);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json; charset=UTF-8"); // Указываем UTF-8
                resp.getWriter().write("{\"message\":\"Операция обновлена\"}");
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Операция не найдена для обновления");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат ID операции: " + pathParts[1]);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID операции");
        } catch (IOException e) {
            logger.warning("Неверный формат JSON в теле PUT запроса: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат JSON");
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении операции: " + e.getMessage());
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при обновлении операции");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String functionIdParam = req.getParameter("functionId");

        if (pathInfo == null) {
            logger.warning("Путь для DELETE запроса отсутствует");
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Путь для DELETE запроса отсутствует");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length == 2) { // ['', 'id'] -> DELETE /api/operations/{id}?functionId=1
            if (functionIdParam == null || functionIdParam.isEmpty()) {
                logger.warning("Параметр functionId обязателен для DELETE операции по ID");
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
                return;
            }

            try {
                Long id = Long.parseLong(pathParts[1]);
                Long functionId = Long.parseLong(functionIdParam);

                if (!checkFunctionAccess(req, functionId)) {
                    return;
                }

                if (!checkOperationAccess(req, id)) {
                    return;
                }

                logger.info("DELETE /api/operations/" + id + " вызван для functionId: " + functionId);

                boolean deleted = operationService.deleteOperation(id, functionId);
                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write("{\"message\":\"Операция удалена\"}");
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Операция не найдена для удаления");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат ID операции или functionId: " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат ID операции или functionId");
            } catch (SQLException e) {
                logger.severe("Ошибка при удалении операции: " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при удалении операции");
            }
        } else if (pathParts.length == 1 && pathParts[0].equals("")) { // '' -> DELETE /api/operations/function?functionId=1
            if (functionIdParam == null || functionIdParam.isEmpty()) {
                logger.warning("Параметр functionId обязателен для DELETE всех операций функции");
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Параметр functionId обязателен");
                return;
            }

            try {
                Long functionId = Long.parseLong(functionIdParam);

                if (!checkFunctionAccess(req, functionId)) {
                    return;
                }

                logger.info("DELETE /api/operations/function вызван для functionId: " + functionId);

                boolean deleted = operationService.deleteAllOperations(functionId);
                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write("{\"message\":\"Все операции функции удалены\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write("{\"message\":\"Запрос обработан, возможно, операций не было\"}");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат functionId: " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный формат functionId");
            } catch (SQLException e) {
                logger.severe("Ошибка при удалении всех операций функции: " + e.getMessage());
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Ошибка при удалении всех операций функции");
            }
        } else {
            logger.warning("Неверный путь для DELETE: " + pathInfo);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Неверный путь для DELETE");
        }
    }
}