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

        if (pathInfo == null) {
            logger.warning("Путь для GET запроса отсутствует");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Путь для GET запроса отсутствует\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length == 2) { // ['', 'functionId'] -> GET /api/tabulated-points/function/{id}
            try {
                Long functionId = Long.parseLong(pathParts[1]);
                logger.info("GET /api/tabulated-points/function/" + functionId + " вызван");

                List<TabulatedFunctionResponseDTO> points = tabulatedFunctionService.getTabulatedFunctionsByFunctionId(functionId);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                PrintWriter out = resp.getWriter();
                out.print(objectMapper.writeValueAsString(points));
                out.flush();
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат functionId: " + pathParts[1]);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный формат functionId\"}");
            } catch (SQLException e) {
                logger.severe("Ошибка при получении точек функции: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при получении точек функции\"}");
            }
        } else if (pathParts.length == 4 && pathParts[1].equals("function") && pathParts[3].equals("x")) { // ['', 'function', 'id', 'x', 'xValue'] -> GET /api/tabulated-points/function/{id}/x/{xValue}
            try {
                Long functionId = Long.parseLong(pathParts[2]);
                Double xValue = Double.parseDouble(pathParts[4]); // pathParts[4] - это xValue после 'x'
                logger.info("GET /api/tabulated-points/function/" + functionId + "/x/" + xValue + " вызван");

                Optional<TabulatedFunctionResponseDTO> point = tabulatedFunctionService.getTabulatedFunctionByXValue(functionId, xValue);

                if (point.isPresent()) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json");
                    PrintWriter out = resp.getWriter();
                    out.print(objectMapper.writeValueAsString(point.get()));
                    out.flush();
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Точка с X=" + xValue + " не найдена для функции " + functionId + "\"}");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат functionId или xValue: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный формат functionId или xValue\"}");
            } catch (SQLException e) {
                logger.severe("Ошибка при поиске точки по X: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при поиске точки по X\"}");
            }
        } else if (pathParts.length == 2 && pathParts[1].equals("function")) { // ['', 'function'] -> GET /api/tabulated-points?functionId=1&fromX=0&toX=10
            String functionIdParam = req.getParameter("functionId");
            String fromXParam = req.getParameter("fromX");
            String toXParam = req.getParameter("toX");

            if (functionIdParam == null || functionIdParam.isEmpty()) {
                logger.warning("Параметр functionId обязателен для GET запроса с диапазоном");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Параметр functionId обязателен\"}");
                return;
            }

            try {
                Long functionId = Long.parseLong(functionIdParam);
                Double fromX = (fromXParam != null && !fromXParam.isEmpty()) ? Double.parseDouble(fromXParam) : null;
                Double toX = (toXParam != null && !toXParam.isEmpty()) ? Double.parseDouble(toXParam) : null;

                if (fromX == null || toX == null) {
                    logger.warning("Параметры fromX и toX обязательны для GET запроса с диапазоном");
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Параметры fromX и toX обязательны\"}");
                    return;
                }

                logger.info("GET /api/tabulated-points с диапазоном X вызван для functionId: " + functionId + ", диапазон: " + fromX + " - " + toX);

                List<TabulatedFunctionResponseDTO> points = tabulatedFunctionService.getTabulatedFunctionsBetweenXValues(functionId, fromX, toX);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                PrintWriter out = resp.getWriter();
                out.print(objectMapper.writeValueAsString(points));
                out.flush();
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат functionId, fromX или toX: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный формат числовых параметров\"}");
            } catch (SQLException e) {
                logger.severe("Ошибка при поиске точек в диапазоне X: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при поиске точек в диапазоне X\"}");
            }
        } else {
            // Попробуем обработать как GET /api/tabulated-points/{id}
            if (pathParts.length == 2) { // ['', 'id']
                try {
                    Long pointId = Long.parseLong(pathParts[1]);
                    logger.info("GET /api/tabulated-points/" + pointId + " вызван");

                    Optional<TabulatedFunctionResponseDTO> point = tabulatedFunctionService.getTabulatedFunctionById(pointId);

                    if (point.isPresent()) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.setContentType("application/json");
                        PrintWriter out = resp.getWriter();
                        out.print(objectMapper.writeValueAsString(point.get()));
                        out.flush();
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"error\":\"Точка с ID=" + pointId + " не найдена\"}");
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Неверный формат ID точки: " + pathParts[1]);
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Неверный формат ID точки\"}");
                } catch (SQLException e) {
                    logger.severe("Ошибка при получении точки по ID: " + e.getMessage());
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("{\"error\":\"Ошибка при получении точки по ID\"}");
                }
            } else {
                logger.warning("Неверный путь для GET: " + pathInfo);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный путь для GET\"}");
            }
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /api/tabulated-points вызван");

        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        try {
            TabulatedFunctionRequestDTO tabulatedFunctionRequest = objectMapper.readValue(jsonBuffer.toString(), TabulatedFunctionRequestDTO.class);
            TabulatedFunctionResponseDTO response = tabulatedFunctionService.createTabulatedFunction(tabulatedFunctionRequest);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.print(objectMapper.writeValueAsString(response));
            out.flush();
        } catch (IOException e) {
            logger.warning("Неверный формат JSON в теле запроса: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат JSON\"}");
        } catch (SQLException e) {
            logger.severe("Ошибка при создании точки: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при создании точки\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            logger.warning("Путь для PUT запроса отсутствует");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Путь для PUT запроса отсутствует\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 2) { // ['', 'id']
            logger.warning("Неверный путь для PUT: " + pathInfo);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь для PUT\"}");
            return;
        }

        try {
            Long id = Long.parseLong(pathParts[1]);

            StringBuilder jsonBuffer = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }

            TabulatedFunction tabulatedFunction = objectMapper.readValue(jsonBuffer.toString(), TabulatedFunction.class);
            // Убедимся, что ID в сущности совпадает с URL

            logger.info("PUT /api/tabulated-points/" + id + " вызван");

            boolean updated = tabulatedFunctionService.updateTabulatedFunction(tabulatedFunction);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\":\"Точка обновлена\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Точка не найдена для обновления\"}");
            }
        } catch (NumberFormatException e) {
            logger.warning("Неверный формат ID точки: " + pathParts[1]);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат ID точки\"}");
        } catch (IOException e) {
            logger.warning("Неверный формат JSON в теле PUT запроса: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат JSON\"}");
        } catch (SQLException e) {
            logger.severe("Ошибка при обновлении точки: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при обновлении точки\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            logger.warning("Путь для DELETE запроса отсутствует");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Путь для DELETE запроса отсутствует\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length == 2) { // ['', 'id'] -> DELETE /api/tabulated-points/{id}
            try {
                Long id = Long.parseLong(pathParts[1]);
                logger.info("DELETE /api/tabulated-points/" + id + " вызван");

                boolean deleted = tabulatedFunctionService.deleteTabulatedFunction(id);
                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\":\"Точка удалена\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Точка не найдена для удаления\"}");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат ID точки: " + pathParts[1]);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный формат ID точки\"}");
            } catch (SQLException e) {
                logger.severe("Ошибка при удалении точки: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при удалении точки\"}");
            }
        } else if (pathParts.length == 3 && pathParts[1].equals("function")) { // ['', 'function', 'functionId'] -> DELETE /api/tabulated-points/function/{id}
            try {
                Long functionId = Long.parseLong(pathParts[2]);
                logger.info("DELETE /api/tabulated-points/function/" + functionId + " вызван");

                boolean deleted = tabulatedFunctionService.deleteAllTabulatedFunctions(functionId);
                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\":\"Все точки функции удалены\"}");
                } else {
                    // deleteAllTabulatedFunctions может вернуть true даже если записей не было
                    // В данном случае, если логика возвращает false, можно интерпретировать как ошибку
                    // или просто как "ничего не удалено". Здесь мы интерпретируем как успех.
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\":\"Запрос обработан, возможно, точек не было\"}");
                }
            } catch (NumberFormatException e) {
                logger.warning("Неверный формат functionId: " + pathParts[2]);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Неверный формат functionId\"}");
            } catch (SQLException e) {
                logger.severe("Ошибка при удалении всех точек функции: " + e.getMessage());
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Ошибка при удалении всех точек функции\"}");
            }
        } else {
            logger.warning("Неверный путь для DELETE: " + pathInfo);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный путь для DELETE\"}");
        }
    }
}