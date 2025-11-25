package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.entity.User;
import model.service.CompositeFunctionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet("/api/composite-functions")
public class CompositeFunctionServlet extends AuthServlet {
    private static final Logger logger = Logger.getLogger(CompositeFunctionServlet.class.getName());
    private final CompositeFunctionService service = new CompositeFunctionService();
    private final ObjectMapper mapper = new ObjectMapper();

    // Вспомогательный DTO, аналогичный CompositeFunctionDto из Spring
    public static class CompositeRequest {
        public Long userId;
        public String baseFunctionName;
        public String outerFunctionName;
        public String customName;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        if (!isAuthenticated(req)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Требуется авторизация\"}");
            return;
        }

        try {
            User user = getAuthenticatedUser(req);
            String body = req.getReader().lines().collect(java.util.stream.Collectors.joining());
            CompositeRequest request = mapper.readValue(body, CompositeRequest.class);

            // Валидация
            if (request.baseFunctionName == null || request.baseFunctionName.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"baseFunctionName обязательно\"}");
                return;
            }
            if (request.outerFunctionName == null || request.outerFunctionName.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"outerFunctionName обязательно\"}");
                return;
            }
            if (request.userId == null || !request.userId.equals(user.getId())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"userId должен совпадать с авторизованным пользователем\"}");
                return;
            }

            // Конструируем выражение, как в контроллере
            String expression = request.outerFunctionName + "(" + request.baseFunctionName + "(x))";
            String functionName = (request.customName != null && !request.customName.trim().isEmpty())
                    ? request.customName.trim()
                    : "Composite_" + request.baseFunctionName.replaceAll("\\s+", "_") + "_" + request.outerFunctionName.replaceAll("\\s+", "_");

            // Сохраняем
            var result = service.createCompositeFunction(functionName, expression, "analytic", user.getId());

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(mapper.writeValueAsString(result));

        } catch (SQLException e) {
            logger.severe("DB error: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка базы данных\"}");
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Некорректные данные: " + e.getMessage() + "\"}");
        }
    }
}