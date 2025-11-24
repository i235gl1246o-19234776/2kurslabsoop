package model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.dto.response.CompositeFunctionResponseDTO;
import model.entity.Function;
import model.servlet.CompositeFunctionStructure;
import repository.dao.FunctionRepository;

import java.sql.SQLException;

public class CompositeFunctionService {
    private FunctionRepository functionRepository = null;

    public CompositeFunctionService() {
        this.functionRepository = functionRepository;
    }

    public CompositeFunctionResponseDTO createCompositeFunction(String displayName, String technicalName,
                                                                String description, String structureJson,
                                                                Long userId) throws SQLException, JsonProcessingException {
        // Парсим структуру
        CompositeFunctionStructure structure = parseStructure(structureJson);

        // Валидация структуры
        if (!validateStructure(structure)) {
            throw new IllegalArgumentException("Invalid composite function structure");
        }

        // Создаем функцию в БД
        Function function = new Function();
        function.setFunctionName(displayName);
        function.setTypeFunction("composite");
        function.setFunctionExpression(structureJson);
        function.setUserId(userId);

        Long functionId = functionRepository.createFunction(function);
        function.setId(functionId);

        // Возвращаем результат
        return new CompositeFunctionResponseDTO(
                functionId,
                displayName,
                technicalName,
                description,
                structure
        );
    }

    private CompositeFunctionStructure parseStructure(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CompositeFunctionStructure.class);
    }

    public boolean validateStructure(String structureJson) {
        try {
            CompositeFunctionStructure structure = parseStructure(structureJson);
            return validateStructure(structure);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateStructure(CompositeFunctionStructure structure) {
        // Реализация валидации структуры составной функции
        return structure != null && structure.getBlocks() != null && structure.getBlocks().size() >= 3;
    }
}
