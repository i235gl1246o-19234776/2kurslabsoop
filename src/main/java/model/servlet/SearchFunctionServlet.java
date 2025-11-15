package model.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.request.SearchFunctionRequestDTO;
import model.dto.response.SearchFunctionResponseDTO;
import model.service.FunctionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet("/api/search/functions")
public class SearchFunctionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(SearchFunctionServlet.class.getName());
    private final FunctionService functionService;
    private final ObjectMapper objectMapper;

    public SearchFunctionServlet() {
        this.functionService = new FunctionService();
        this.objectMapper = new ObjectMapper();
    }

    // Конструктор для тестирования
    public SearchFunctionServlet(FunctionService functionService) {
        this.functionService = functionService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("GET /api/search/functions вызван");

        // Считываем параметры из строки запроса
        String userIdParam = req.getParameter("userId");
        String userName = req.getParameter("userName");
        String functionName = req.getParameter("functionName");
        String typeFunction = req.getParameter("typeFunction");
        String xValParam = req.getParameter("xVal");
        String yValParam = req.getParameter("yVal");
        String operationsTypeIdParam = req.getParameter("operationsTypeId");
        String sortBy = req.getParameter("sortBy");
        String sortOrder = req.getParameter("sortOrder");

        // Параметры пагинации
        String pageParam = req.getParameter("page");
        String sizeParam = req.getParameter("size");

        // Пытаемся преобразовать строки в нужные типы
        Long userId = (userIdParam != null && !userIdParam.isEmpty()) ? Long.parseLong(userIdParam) : null;
        Double xVal = (xValParam != null && !xValParam.isEmpty()) ? Double.parseDouble(xValParam) : null;
        Double yVal = (yValParam != null && !yValParam.isEmpty()) ? Double.parseDouble(yValParam) : null;
        Long operationsTypeId = (operationsTypeIdParam != null && !operationsTypeIdParam.isEmpty()) ? Long.parseLong(operationsTypeIdParam) : null;
        Integer page = (pageParam != null && !pageParam.isEmpty()) ? Integer.parseInt(pageParam) : null;
        Integer size = (sizeParam != null && !sizeParam.isEmpty()) ? Integer.parseInt(sizeParam) : null;

        // Создаем DTO с параметрами из запроса
        SearchFunctionRequestDTO searchRequest = new SearchFunctionRequestDTO();
        searchRequest.setUserName(userName);
        searchRequest.setFunctionName(functionName);
        searchRequest.setTypeFunction(typeFunction);
        searchRequest.setXVal(xVal);
        searchRequest.setYVal(yVal);
        searchRequest.setOperationsTypeId(operationsTypeId);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortOrder(sortOrder);
        searchRequest.setPage(page);
        searchRequest.setSize(size);

        try {
            SearchFunctionResponseDTO result = functionService.searchFunctions(searchRequest);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.print(objectMapper.writeValueAsString(result));
            out.flush();
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функций: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при поиске функций\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /api/search/functions вызван");

        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        try {
            SearchFunctionRequestDTO searchRequest = objectMapper.readValue(jsonBuffer.toString(), SearchFunctionRequestDTO.class);
            SearchFunctionResponseDTO result = functionService.searchFunctions(searchRequest);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.print(objectMapper.writeValueAsString(result));
            out.flush();
        } catch (IOException e) {
            logger.warning("Неверный формат JSON в теле запроса: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Неверный формат JSON\"}");
        } catch (SQLException e) {
            logger.severe("Ошибка при поиске функций: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Ошибка при поиске функций\"}");
        }
    }
}