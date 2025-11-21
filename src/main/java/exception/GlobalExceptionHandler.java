package exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class GlobalExceptionHandler {
    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void handleException(Throwable ex, HttpServletResponse resp) throws IOException {
        logger.severe("Обработка исключения: " + ex.getMessage());

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        int statusCode;

        if (ex instanceof ValidationException) {
            statusCode = HttpServletResponse.SC_BAD_REQUEST;
            errorResponse.put("type", "validation");
        } else if (ex instanceof NumberFormatException) {
            statusCode = HttpServletResponse.SC_BAD_REQUEST;
            errorResponse.put("type", "number_format");
            errorResponse.put("message", "Пожалуйста, введите корректное числовое значение");
        } else if (ex instanceof IllegalArgumentException) {
            statusCode = HttpServletResponse.SC_BAD_REQUEST;
            errorResponse.put("type", "illegal_argument");
            errorResponse.put("message", ex.getMessage());
        } else {
            statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            errorResponse.put("type", "internal_error");
            errorResponse.put("message", "Произошла внутренняя ошибка сервера");
        }

        if (!errorResponse.containsKey("message")) {
            errorResponse.put("message", ex.getMessage());
        }

        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}