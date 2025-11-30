package core.controller;

import core.dto.*;
import core.entity.*;
import core.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin/search")
public class SearchFunctionController {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private UserRepository userRepository;

    // Проверка прав администратора
    private boolean isAdmin() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        String role = (String) request.getAttribute("userRole");
        return "ADMIN".equals(role);
    }

    // POST /api/admin/search/functions - поиск функций
    @PostMapping("/functions")
    public ResponseEntity<SearchFunctionResponseDTO> searchFunctions(@RequestBody SearchFunctionRequestDTO request) {
        log.info("Запрос на расширенный поиск функций: {}", request);

        if (!isAdmin()) {
            log.warn("Попытка поиска функций без прав администратора");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // Получаем все функции из базы
            List<FunctionEntity> allFunctions = functionRepository.findAll();

            // Фильтрация по userName
            if (request.getUserName() != null && !request.getUserName().isEmpty()) {
                allFunctions = allFunctions.stream()
                        .filter(f -> f.getUser().getName().equals(request.getUserName()))
                        .collect(Collectors.toList());
            }

            // Фильтрация по functionName
            if (request.getFunctionName() != null && !request.getFunctionName().isEmpty()) {
                allFunctions = allFunctions.stream()
                        .filter(f -> f.getFunctionName().contains(request.getFunctionName()))
                        .collect(Collectors.toList());
            }

            // Фильтрация по typeFunction
            if (request.getTypeFunction() != null && !request.getTypeFunction().isEmpty()) {
                try {
                    FunctionEntity.FunctionType typeEnum = FunctionEntity.FunctionType.valueOf(request.getTypeFunction().toLowerCase());
                    allFunctions = allFunctions.stream()
                            .filter(f -> f.getTypeFunction() == typeEnum)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    log.warn("Неверный тип функции при поиске: {}", request.getTypeFunction());
                    return ResponseEntity.badRequest().build();
                }
            }

            // Фильтрация по xVal и yVal
            if (request.getXVal() != null || request.getYVal() != null) {
                List<FunctionEntity> filteredFunctions = new ArrayList<>();
                for (FunctionEntity function : allFunctions) {
                    List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(function.getId());
                    if (request.getXVal() != null && request.getYVal() != null) {
                        boolean match = points.stream().anyMatch(p ->
                                Math.abs(p.getXVal() - request.getXVal()) < 1e-9 &&
                                        Math.abs(p.getYVal() - request.getYVal()) < 1e-9);
                        if (match) filteredFunctions.add(function);
                    } else if (request.getXVal() != null) {
                        boolean match = points.stream().anyMatch(p ->
                                Math.abs(p.getXVal() - request.getXVal()) < 1e-9);
                        if (match) filteredFunctions.add(function);
                    } else if (request.getYVal() != null) {
                        boolean match = points.stream().anyMatch(p ->
                                Math.abs(p.getYVal() - request.getYVal()) < 1e-9);
                        if (match) filteredFunctions.add(function);
                    }
                }
                allFunctions = filteredFunctions;
            }

            // Фильтрация по operationsTypeId
            if (request.getOperationsTypeId() != null) {
                List<FunctionEntity> filteredFunctions = new ArrayList<>();
                for (FunctionEntity function : allFunctions) {
                    List<OperationEntity> operations = operationRepository.findAll().stream()
                            .filter(op -> op.getFunction().getId().equals(function.getId()))
                            .collect(Collectors.toList());
                    boolean match = operations.stream()
                            .anyMatch(op -> op.getOperationsTypeId().equals(request.getOperationsTypeId()));
                    if (match) filteredFunctions.add(function);
                }
                allFunctions = filteredFunctions;
            }

            // Сортировка
            if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
                switch (request.getSortBy().toLowerCase()) {
                    case "functionname":
                        if ("desc".equalsIgnoreCase(request.getSortOrder())) {
                            allFunctions.sort((f1, f2) -> f2.getFunctionName().compareTo(f1.getFunctionName()));
                        } else {
                            allFunctions.sort((f1, f2) -> f1.getFunctionName().compareTo(f2.getFunctionName()));
                        }
                        break;
                    case "typefunction":
                        if ("desc".equalsIgnoreCase(request.getSortOrder())) {
                            allFunctions.sort((f1, f2) -> f2.getTypeFunction().name().compareTo(f1.getTypeFunction().name()));
                        } else {
                            allFunctions.sort((f1, f2) -> f1.getTypeFunction().name().compareTo(f2.getTypeFunction().name()));
                        }
                        break;
                }
            }

            // Пагинация
            int total = allFunctions.size();
            List<FunctionEntity> paginatedFunctions = allFunctions;
            if (request.getPage() != null && request.getSize() != null) {
                int page = request.getPage();
                int size = request.getSize();
                int fromIndex = (page - 1) * size;
                int toIndex = Math.min(fromIndex + size, total);
                if (fromIndex < total) {
                    paginatedFunctions = allFunctions.subList(fromIndex, toIndex);
                } else {
                    paginatedFunctions = new ArrayList<>();
                }
            }

            // Конвертация в DTO
            List<FunctionResponseDTO> responseDtos = new ArrayList<>();
            for (FunctionEntity function : paginatedFunctions) {
                FunctionResponseDTO dto = new FunctionResponseDTO();
                dto.setFunctionId(function.getId());
                dto.setFunctionName(function.getFunctionName());
                dto.setTypeFunction(function.getTypeFunction().name());
                dto.setUserName(function.getUser().getName());

                // Получаем первую точку для xVal и yVal (если есть)
                List<TabulatedFunctionEntity> points = tabulatedFunctionRepository.findByFunction_Id(function.getId());
                if (!points.isEmpty()) {
                    TabulatedFunctionEntity firstPoint = points.get(0);
                    dto.setXVal(firstPoint.getXVal());
                    dto.setYVal(firstPoint.getYVal());
                }

                // Получаем первую операцию для operationsTypeId (если есть)
                List<OperationEntity> operations = operationRepository.findAll().stream()
                        .filter(op -> op.getFunction().getId().equals(function.getId()))
                        .collect(Collectors.toList());
                if (!operations.isEmpty()) {
                    dto.setOperationsTypeId(Long.valueOf(operations.get(0).getOperationsTypeId()));
                }

                responseDtos.add(dto);
            }

            SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(responseDtos, total);
            log.info("Поиск завершен, найдено {} функций из {}", responseDtos.size(), total);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка при поиске функций: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DTO для поиска
    public static class SearchFunctionRequestDTO {
        private String userName;
        private String functionName;
        private String typeFunction;
        private Double xVal;
        private Double yVal;
        private Long operationsTypeId;
        private String sortBy;
        private String sortOrder;
        private Integer page;
        private Integer size;

        // Геттеры и сеттеры
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getFunctionName() { return functionName; }
        public void setFunctionName(String functionName) { this.functionName = functionName; }
        public String getTypeFunction() { return typeFunction; }
        public void setTypeFunction(String typeFunction) { this.typeFunction = typeFunction; }
        public Double getXVal() { return xVal; }
        public void setXVal(Double xVal) { this.xVal = xVal; }
        public Double getYVal() { return yVal; }
        public void setYVal(Double yVal) { this.yVal = yVal; }
        public Long getOperationsTypeId() { return operationsTypeId; }
        public void setOperationsTypeId(Long operationsTypeId) { this.operationsTypeId = operationsTypeId; }
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
    }

    public static class FunctionResponseDTO {
        private Long functionId;
        private String functionName;
        private String typeFunction;
        private String userName;
        private Double xVal;
        private Double yVal;
        private Long operationsTypeId;

        // Геттеры и сеттеры
        public Long getFunctionId() { return functionId; }
        public void setFunctionId(Long functionId) { this.functionId = functionId; }
        public String getFunctionName() { return functionName; }
        public void setFunctionName(String functionName) { this.functionName = functionName; }
        public String getTypeFunction() { return typeFunction; }
        public void setTypeFunction(String typeFunction) { this.typeFunction = typeFunction; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public Double getXVal() { return xVal; }
        public void setXVal(Double xVal) { this.xVal = xVal; }
        public Double getYVal() { return yVal; }
        public void setYVal(Double yVal) { this.yVal = yVal; }
        public Long getOperationsTypeId() { return operationsTypeId; }
        public void setOperationsTypeId(Long operationsTypeId) { this.operationsTypeId = operationsTypeId; }
    }

    public static class SearchFunctionResponseDTO {
        private List<FunctionResponseDTO> content;
        private long totalElements;

        public SearchFunctionResponseDTO(List<FunctionResponseDTO> content, long totalElements) {
            this.content = content;
            this.totalElements = totalElements;
        }

        // Геттеры и сеттеры
        public List<FunctionResponseDTO> getContent() { return content; }
        public void setContent(List<FunctionResponseDTO> content) { this.content = content; }
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    }
}