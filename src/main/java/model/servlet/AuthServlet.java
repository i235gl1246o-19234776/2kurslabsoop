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

public class AuthServlet implements Filter {

    private AuthenticationService authenticationService;
    private UserRepository userRepository;

    // Используем стандартный Java Logger
    private static final Logger logger = Logger.getLogger(AuthServlet.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");

            DatabaseConnection connection = new DatabaseConnection();
            this.userRepository = new UserRepository(connection);
            this.authenticationService = new AuthenticationService(userRepository);

        } catch (ClassNotFoundException error) {
            logger.log(Level.SEVERE, "Ошибка инициализации AuthServlet", error);
            throw new ServletException("Failed to initialize AuthenticationService or database connection", error);
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
            return;
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);

        if (values.length != 2) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String username = values[0];
        String password = values[1];

        try {
            boolean isAuthenticated = authenticationService.authenticateUser(username, password);

            if (!isAuthenticated) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Optional<User> userOpt = userRepository.findByName(username);
            if (userOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            User user = userOpt.get();

            if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.USER) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\":\"Access denied: invalid role\"}");
                return;
            }

            request.setAttribute("authenticatedUser", user);
            request.setAttribute("userRole", user.getRole());

            chain.doFilter(req, res);

        } catch (SQLException error) {
            logger.log(Level.SEVERE, "Ошибка аутентификации", error);
            throw new ServletException("Database error in authentication", error);
        }
    }

}