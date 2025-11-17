package model.servlet;

import model.service.AuthenticationService;
import model.entity.User;
import model.entity.UserRole;
import repository.DatabaseConnection;
import repository.dao.UserRepository;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AuthFilter implements Filter {

    private AuthenticationService authenticationService;
    private UserRepository userRepository;
    private static final Logger logger = Logger.getLogger(AuthFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");

            DatabaseConnection connection = new DatabaseConnection();
            this.userRepository = new UserRepository(connection);
            this.authenticationService = new AuthenticationService(userRepository);

        } catch (ClassNotFoundException error) {
            logger.log(Level.SEVERE, "Ошибка инициализации AuthFilter", error);
            throw new ServletException("Failed to initialize AuthenticationService or database connection", error);
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
            response.getWriter().write("{\"error\":\"Authorization header required\"}");
            return;
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials;
        try {
            credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid Base64 encoding\"}");
            return;
        }

        String[] values = credentials.split(":", 2);

        if (values.length != 2) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid credentials format\"}");
            return;
        }

        String username = values[0].trim();
        String password = values[1].trim();

        if (username.isEmpty() || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Username and password required\"}");
            return;
        }

        try {
            boolean isAuthenticated = authenticationService.authenticateUser(username, password);

            if (!isAuthenticated) {
                logger.warning("Authentication failed for user: " + username);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Invalid username or password\"}");
                return;
            }

            Optional<User> userOpt = userRepository.findByName(username);
            if (userOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"User not found\"}");
                return;
            }

            User user = userOpt.get();

            // Проверяем валидность роли пользователя
            if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.USER) {
                logger.warning("User has invalid role: " + user.getRole());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Access denied: invalid role\"}");
                return;
            }

            // Сохраняем пользователя и роль в атрибуты запроса
            request.setAttribute("authenticatedUser", user);
            request.setAttribute("userRole", user.getRole());

            logger.info("User authenticated: " + username + " with role: " + user.getRole());
            chain.doFilter(req, res);

        } catch (SQLException error) {
            logger.log(Level.SEVERE, "Database error in authentication for user: " + username, error);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error\"}");
        } catch (Exception error) {
            logger.log(Level.SEVERE, "Unexpected error in authentication for user: " + username, error);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }
}