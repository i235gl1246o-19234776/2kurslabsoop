package service;

import model.dto.request.FunctionRequestDTO;
import model.dto.response.FunctionResponseDTO;
import model.entity.Function;
import repository.FunctionRepository;
import model.dto.DTOTransformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FunctionServiceEdgeCasesTest {

    private FunctionRepository functionRepository = mock(FunctionRepository.class);
    private DTOTransformService dtoTransformService = mock(DTOTransformService.class);
    private FunctionService functionService = new FunctionService(functionRepository, dtoTransformService);

    @Test
    @DisplayName("Should handle empty function list for user")
    void getFunctionsByUserId_EmptyList() throws SQLException {
        Long userId = 1L;
        when(functionRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(dtoTransformService.toFunctionResponseDTOs(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        var result = functionService.getFunctionsByUserId(userId);

        assertTrue(result.isEmpty());
        verify(functionRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should handle null function name pattern")
    void getFunctionsByName_NullPattern() throws SQLException {
        Long userId = 1L;
        String nullPattern = null;
        when(functionRepository.findByName(userId, nullPattern)).thenReturn(Collections.emptyList());
        when(dtoTransformService.toFunctionResponseDTOs(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        var result = functionService.getFunctionsByName(userId, nullPattern);

        assertTrue(result.isEmpty());
        verify(functionRepository).findByName(userId, nullPattern);
    }

    @Test
    @DisplayName("Should handle null function type")
    void getFunctionsByType_NullType() throws SQLException {
        Long userId = 1L;
        String nullType = null;
        when(functionRepository.findByType(userId, nullType)).thenReturn(Collections.emptyList());
        when(dtoTransformService.toFunctionResponseDTOs(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        var result = functionService.getFunctionsByType(userId, nullType);

        assertTrue(result.isEmpty());
        verify(functionRepository).findByType(userId, nullType);
    }

    @Test
    @DisplayName("Should handle function with null properties")
    void createFunction_WithNullProperties() throws SQLException {
        FunctionRequestDTO requestDTO = new FunctionRequestDTO();
        requestDTO.setUserId(null);
        requestDTO.setFunctionName(null);
        requestDTO.setTypeFunction(null);
        requestDTO.setFunctionExpression(null);

        Function functionEntity = new Function();
        when(dtoTransformService.toEntity(requestDTO)).thenReturn(functionEntity);
        when(functionRepository.createFunction(functionEntity)).thenReturn(1L);
        when(functionRepository.findById(1L, null)).thenReturn(Optional.of(functionEntity));
        when(dtoTransformService.toResponseDTO(functionEntity)).thenReturn(new FunctionResponseDTO());

        assertDoesNotThrow(() -> functionService.createFunction(requestDTO));
        verify(functionRepository).createFunction(functionEntity);
    }

    @Test
    @DisplayName("Should handle very long function names")
    void createFunction_VeryLongName() throws SQLException {
        String longName = "A".repeat(1000);
        FunctionRequestDTO requestDTO = new FunctionRequestDTO();
        requestDTO.setUserId(1L);
        requestDTO.setFunctionName(longName);

        Function functionEntity = new Function();
        functionEntity.setFunctionName(longName);

        when(dtoTransformService.toEntity(requestDTO)).thenReturn(functionEntity);
        when(functionRepository.createFunction(functionEntity)).thenReturn(1L);
        when(functionRepository.findById(1L, 1L)).thenReturn(Optional.of(functionEntity));
        when(dtoTransformService.toResponseDTO(functionEntity)).thenReturn(new FunctionResponseDTO());

        var result = functionService.createFunction(requestDTO);

        assertNotNull(result);
        verify(functionRepository).createFunction(argThat(func ->
                longName.equals(func.getFunctionName())
        ));
    }
}