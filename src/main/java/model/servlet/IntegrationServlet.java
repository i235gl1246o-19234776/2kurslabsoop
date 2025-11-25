package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.IntegrationRequestDTO;
import model.dto.response.IntegrationResultDTO;
import model.entity.User;
import model.service.IntegrationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet("/api/integration")
public class IntegrationServlet extends AuthServlet {
    private static final Logger logger = Logger.getLogger(IntegrationServlet.class.getName());
    private final IntegrationService integrationService;
    private final ObjectMapper objectMapper;

    public IntegrationServlet() {
        this.integrationService = new IntegrationService();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAuthenticated(req)) {
            sendUnauthorized(resp);
            return;
        }

        try {
            User user = getAuthenticatedUser(req);
            IntegrationRequestDTO requestDTO = objectMapper.readValue(req.getReader(), IntegrationRequestDTO.class);

            // Вызываем метод сервиса
            IntegrationResultDTO result = integrationService.calculateIntegral(requestDTO, user.getId());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(result));

        } catch (SecurityException e) {
            logger.warning("Access denied: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Access denied to function\"}");
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid request: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (SQLException e) {
            logger.severe("Database error: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Database error during integration\"}");
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }
}