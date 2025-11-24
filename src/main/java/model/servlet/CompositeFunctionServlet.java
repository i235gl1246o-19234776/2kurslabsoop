package model.servlet;

import model.dto.request.CompositeFunctionRequestDTO;
import model.dto.response.CompositeFunctionResponseDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.FunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import model.entity.Function;
import model.entity.User;
import model.service.CompositeFunctionService;
import model.service.FunctionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.List;

@WebServlet("/api/composite-functions")
public class CompositeFunctionServlet extends AuthServlet {
    private static final Logger logger = Logger.getLogger(CompositeFunctionServlet.class.getName());
    private final CompositeFunctionService compositeFunctionService;
    private final ObjectMapper objectMapper;

    public CompositeFunctionServlet() {
        this.compositeFunctionService = new CompositeFunctionService();
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
            CompositeFunctionRequestDTO request = objectMapper.readValue(req.getReader(), CompositeFunctionRequestDTO.class);

            // Проверяем валидность структуры
            if (!compositeFunctionService.validateStructure(request.getFunctionExpression())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Invalid composite function structure\"}");
                return;
            }

            // Создаем составную функцию
            CompositeFunctionResponseDTO response = compositeFunctionService.createCompositeFunction(
                    request.getFunctionName(),
                    request.getTechnicalName(),
                    request.getDescription(),
                    request.getFunctionExpression(),
                    user.getId()
            );

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write(objectMapper.writeValueAsString(response));

        } catch (SQLException e) {
            logger.severe("Database error during composite function creation: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Database error during function creation\"}");
        } catch (Exception e) {
            logger.severe("Error during composite function creation: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid function parameters\"}");
        }
    }
}