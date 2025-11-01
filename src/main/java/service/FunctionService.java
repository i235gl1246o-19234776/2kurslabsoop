package service;

import model.SearchFunctionResult;
import model.dto.request.SearchFunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import model.dto.response.SearchFunctionResponseDTO;
import repository.FunctionRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionService {

    private final FunctionRepository functionRepository;

    public FunctionService() {
        this.functionRepository = new FunctionRepository();
    }

    private List<SearchFunctionResult> doSearch(
            Long userIdFilter,
            String userName,
            String functionName,
            String type,
            Double x,
            Double y,
            Long operationsTypeId,
            String sortBy,
            String sortOrder) throws SQLException {

        if (!"function_id".equals(sortBy) &&
                !"function_name".equals(sortBy) &&
                !"type_function".equals(sortBy) &&
                !"user_name".equals(sortBy)) {
            sortBy = "function_id";
        }
        if (!"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) {
            sortOrder = "asc";
        }

        return functionRepository.search(
                userIdFilter,
                userName,
                functionName,
                type,
                x,
                y,
                operationsTypeId,
                sortBy.toLowerCase(),
                sortOrder.toLowerCase()
        );
    }

    public List<SearchFunctionResult> searchFunctions(
            Long currentUserId,
            String userName,
            String functionName,
            String type,
            Double x,
            Double y) throws SQLException {

        return doSearch(currentUserId, userName, functionName, type, x, y, null, "function_id", "asc");
    }

    public SearchFunctionResponseDTO searchFunctions(SearchFunctionRequestDTO request) throws SQLException {
        List<SearchFunctionResult> results = doSearch(
                null, // ⚠️ Замените на реальный ID авторизованного пользователя!
                request.getUserName(),
                request.getFunctionName(),
                request.getTypeFunction(),
                request.getXVal(),
                request.getYVal(),
                request.getOperationsTypeId(),
                request.getSortBy(),
                request.getSortOrder()
        );

        List<FunctionResponseDTO> dtos = results.stream()
                .map(r -> new FunctionResponseDTO(
                        r.getFunctionId(),
                        r.getFunctionName(),
                        r.getFunctionExpression(),
                        r.getTypeFunction(),
                        r.getUserId(),
                        r.getUserName()
                ))
                .collect(Collectors.toList());

        return new SearchFunctionResponseDTO(dtos, dtos.size());
    }
}