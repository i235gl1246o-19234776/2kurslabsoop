package model.service;

import model.dto.response.CompositeFunctionResponseDTO;
import model.entity.Function;
import repository.dao.FunctionRepository;

import java.sql.SQLException;


public class CompositeFunctionService {
    private final FunctionRepository repo = new FunctionRepository();

    public CompositeFunctionResponseDTO createCompositeFunction(
            String functionName,
            String functionExpression,
            String typeFunction, // всегда "analytic"
            Long userId
    ) throws SQLException {
        Function entity = new Function();
        entity.setFunctionName(functionName);
        entity.setFunctionExpression(functionExpression);
        entity.setTypeFunction(typeFunction);
        entity.setUserId(userId);

        Long id = repo.createFunction(entity);
        return new CompositeFunctionResponseDTO(id, functionName);
    }
}