package core.config;

import core.entity.UserEntity;
import core.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Component
public class BasicAuthFilter implements Filter {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public BasicAuthFilter(@Lazy UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // Разрешаем доступ к публичным путям без аутентификации
        if ((uri.matches(".*/api/register") && method.equalsIgnoreCase("POST")) ||
                (uri.matches(".*/api/users/name/.*") && method.equalsIgnoreCase("GET"))) {
            chain.doFilter(request, response);
            return;
        }

        // Проверка заголовка Authorization
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"Restricted\"");
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Authorization header required\"}");
            return;
        }

        // Декодирование учетных данных
        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        String credentials;
        try {
            credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Invalid Base64 encoding\"}");
            return;
        }

        // Разделение на имя пользователя и пароль
        String[] values = credentials.split(":", 2);
        if (values.length != 2) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Invalid credentials format\"}");
            return;
        }

        String username = values[0].trim();
        String password = values[1].trim();

        if (username.isEmpty() || password.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Username and password required\"}");
            return;
        }

        // Аутентификация
        UserEntity userEntity = userRepository.findByName(username);
        Optional<UserEntity> userOpt = Optional.ofNullable(userEntity);
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPasswordHash())) {
            log.warn("Authentication failed for user: {}", username);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Invalid username or password\"}");
            return;
        }

        UserEntity user = userOpt.get();
        if (!"ADMIN".equals(user.getRole()) && !"USER".equals(user.getRole())) {
            log.warn("User {} has invalid role: {}", username, user.getRole());
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Access denied: invalid role\"}");
            return;
        }

        // Установка атрибутов для последующего использования в контроллерах
        httpRequest.setAttribute("authenticatedUser", user);
        httpRequest.setAttribute("userRole", user.getRole());

        log.info("User authenticated: {} with role: {}", username, user.getRole());
        chain.doFilter(request, response);
    }
}