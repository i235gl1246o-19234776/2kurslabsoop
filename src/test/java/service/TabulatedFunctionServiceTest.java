package service;

import model.entity.TabulatedFunction;
import model.dto.request.TabulatedFunctionRequestDTO;
import model.dto.response.TabulatedFunctionResponseDTO;
import model.dto.DTOTransformService;
import model.service.TabulatedFunctionService;
import repository.dao.TabulatedFunctionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TabulatedFunctionServiceTest {

    @Mock
    private TabulatedFunctionRepository tabulatedFunctionRepository;

    @Mock
    private DTOTransformService dtoTransformService;

    private TabulatedFunctionService tabulatedFunctionService;

    @BeforeEach
    void setUp() {
        tabulatedFunctionService = new TabulatedFunctionService(tabulatedFunctionRepository, dtoTransformService);
    }

    @Test
    @DisplayName("Should create tabulated function successfully")
    void createTabulatedFunction_Success() throws SQLException {
        TabulatedFunctionRequestDTO requestDTO = new TabulatedFunctionRequestDTO();
        requestDTO.setFunctionId(1L);
        requestDTO.setXVal(2.5);
        requestDTO.setYVal(3.7);

        TabulatedFunction entity = createTabulatedFunction(1L, 1L, 2.5, 3.7);
        TabulatedFunctionResponseDTO responseDTO = createTabulatedFunctionResponseDTO(1L, 2.5, 3.7);

        when(dtoTransformService.toEntity(requestDTO)).thenReturn(entity);
        when(tabulatedFunctionRepository.createTabulatedFunction(entity)).thenReturn(1L);
        when(tabulatedFunctionRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(dtoTransformService.toResponseDTO(entity)).thenReturn(responseDTO);

        TabulatedFunctionResponseDTO result = tabulatedFunctionService.createTabulatedFunction(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2.5, result.getXVal());
        assertEquals(3.7, result.getYVal());

        verify(dtoTransformService).toEntity(requestDTO);
        verify(tabulatedFunctionRepository).createTabulatedFunction(entity);
        verify(tabulatedFunctionRepository).findById(1L);
        verify(dtoTransformService).toResponseDTO(entity);
    }

    @Test
    @DisplayName("Should throw exception when created point not found")
    void createTabulatedFunction_PointNotFoundAfterCreation() throws SQLException {
        TabulatedFunctionRequestDTO requestDTO = new TabulatedFunctionRequestDTO();
        requestDTO.setFunctionId(1L);
        requestDTO.setXVal(2.5);
        requestDTO.setYVal(3.7);

        TabulatedFunction entity = createTabulatedFunction(null, 1L, 2.5, 3.7);
        when(dtoTransformService.toEntity(requestDTO)).thenReturn(entity);
        when(tabulatedFunctionRepository.createTabulatedFunction(entity)).thenReturn(1L);
        when(tabulatedFunctionRepository.findById(1L)).thenReturn(Optional.empty());

        SQLException exception = assertThrows(SQLException.class, () ->
                tabulatedFunctionService.createTabulatedFunction(requestDTO)
        );
        assertTrue(exception.getMessage().contains("Не удалось получить созданную точку с ID: 1"));

        verify(dtoTransformService).toEntity(requestDTO);
        verify(tabulatedFunctionRepository).createTabulatedFunction(entity);
        verify(tabulatedFunctionRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get tabulated functions by function ID")
    void getTabulatedFunctionsByFunctionId_Success() throws SQLException {
        Long functionId = 1L;
        List<TabulatedFunction> entities = Arrays.asList(
                createTabulatedFunction(1L, functionId, 1.0, 2.0),
                createTabulatedFunction(2L, functionId, 2.0, 4.0)
        );
        List<TabulatedFunctionResponseDTO> responseDTOs = Arrays.asList(
                createTabulatedFunctionResponseDTO(1L, 1.0, 2.0),
                createTabulatedFunctionResponseDTO(2L, 2.0, 4.0)
        );

        when(tabulatedFunctionRepository.findAllByFunctionId(functionId)).thenReturn(entities);
        when(dtoTransformService.toResponseDTOs(entities)).thenReturn(responseDTOs);

        List<TabulatedFunctionResponseDTO> result = tabulatedFunctionService.getTabulatedFunctionsByFunctionId(functionId);

        assertEquals(2, result.size());
        assertEquals(1.0, result.get(0).getXVal());
        assertEquals(2.0, result.get(0).getYVal());
        assertEquals(2.0, result.get(1).getXVal());
        assertEquals(4.0, result.get(1).getYVal());

        verify(tabulatedFunctionRepository).findAllByFunctionId(functionId);
        verify(dtoTransformService).toResponseDTOs(entities);
    }

    @Test
    @DisplayName("Should get tabulated function by X value")
    void getTabulatedFunctionByXValue_Success() throws SQLException {
        Long functionId = 1L;
        Double xVal = 2.5;
        TabulatedFunction entity = createTabulatedFunction(1L, functionId, xVal, 3.7);
        TabulatedFunctionResponseDTO responseDTO = createTabulatedFunctionResponseDTO(1L, xVal, 3.7);

        when(tabulatedFunctionRepository.findByXValue(functionId, xVal)).thenReturn(Optional.of(entity));
        when(dtoTransformService.toResponseDTO(entity)).thenReturn(responseDTO);

        Optional<TabulatedFunctionResponseDTO> result = tabulatedFunctionService.getTabulatedFunctionByXValue(functionId, xVal);

        assertTrue(result.isPresent());
        assertEquals(xVal, result.get().getXVal());
        assertEquals(3.7, result.get().getYVal());

        verify(tabulatedFunctionRepository).findByXValue(functionId, xVal);
        verify(dtoTransformService).toResponseDTO(entity);
    }

    @Test
    @DisplayName("Should return empty when tabulated function not found by X value")
    void getTabulatedFunctionByXValue_NotFound() throws SQLException {
        Long functionId = 1L;
        Double xVal = 999.0;

        when(tabulatedFunctionRepository.findByXValue(functionId, xVal)).thenReturn(Optional.empty());

        Optional<TabulatedFunctionResponseDTO> result = tabulatedFunctionService.getTabulatedFunctionByXValue(functionId, xVal);

        assertFalse(result.isPresent());
        verify(tabulatedFunctionRepository).findByXValue(functionId, xVal);
    }

    @Test
    @DisplayName("Should get tabulated functions between X values")
    void getTabulatedFunctionsBetweenXValues_Success() throws SQLException {
        Long functionId = 1L;
        Double xMin = 1.0;
        Double xMax = 5.0;
        List<TabulatedFunction> entities = Arrays.asList(
                createTabulatedFunction(1L, functionId, 2.0, 4.0),
                createTabulatedFunction(2L, functionId, 3.0, 9.0),
                createTabulatedFunction(3L, functionId, 4.0, 16.0)
        );
        List<TabulatedFunctionResponseDTO> responseDTOs = Arrays.asList(
                createTabulatedFunctionResponseDTO(1L, 2.0, 4.0),
                createTabulatedFunctionResponseDTO(2L, 3.0, 9.0),
                createTabulatedFunctionResponseDTO(3L, 4.0, 16.0)
        );

        when(tabulatedFunctionRepository.findBetweenXValues(functionId, xMin, xMax)).thenReturn(entities);
        when(dtoTransformService.toResponseDTOs(entities)).thenReturn(responseDTOs);

        List<TabulatedFunctionResponseDTO> result = tabulatedFunctionService.getTabulatedFunctionsBetweenXValues(functionId, xMin, xMax);

        assertEquals(3, result.size());
        verify(tabulatedFunctionRepository).findBetweenXValues(functionId, xMin, xMax);
        verify(dtoTransformService).toResponseDTOs(entities);
    }

    @Test
    @DisplayName("Should update tabulated function successfully")
    void updateTabulatedFunction_Success() throws SQLException {
        TabulatedFunction entity = createTabulatedFunction(1L, 1L, 2.5, 3.7);
        when(tabulatedFunctionRepository.updateTabulatedFunction(entity)).thenReturn(true);

        boolean result = tabulatedFunctionService.updateTabulatedFunction(entity);

        assertTrue(result);
        verify(tabulatedFunctionRepository).updateTabulatedFunction(entity);
    }

    @Test
    @DisplayName("Should return false when update fails")
    void updateTabulatedFunction_Failure() throws SQLException {
        TabulatedFunction entity = createTabulatedFunction(1L, 1L, 2.5, 3.7);
        when(tabulatedFunctionRepository.updateTabulatedFunction(entity)).thenReturn(false);

        boolean result = tabulatedFunctionService.updateTabulatedFunction(entity);

        assertFalse(result);
        verify(tabulatedFunctionRepository).updateTabulatedFunction(entity);
    }

    @Test
    @DisplayName("Should delete tabulated function successfully")
    void deleteTabulatedFunction_Success() throws SQLException {
        Long pointId = 1L;
        when(tabulatedFunctionRepository.deleteTabulatedFunction(pointId)).thenReturn(true);

        boolean result = tabulatedFunctionService.deleteTabulatedFunction(pointId);

        assertTrue(result);
        verify(tabulatedFunctionRepository).deleteTabulatedFunction(pointId);
    }

    @Test
    @DisplayName("Should return false when delete fails")
    void deleteTabulatedFunction_Failure() throws SQLException {
        Long pointId = 1L;
        when(tabulatedFunctionRepository.deleteTabulatedFunction(pointId)).thenReturn(false);

        boolean result = tabulatedFunctionService.deleteTabulatedFunction(pointId);

        assertFalse(result);
        verify(tabulatedFunctionRepository).deleteTabulatedFunction(pointId);
    }

    @Test
    @DisplayName("Should delete all tabulated functions for function")
    void deleteAllTabulatedFunctions_Success() throws SQLException {
        Long functionId = 1L;
        when(tabulatedFunctionRepository.deleteAllTabulatedFunctions(functionId)).thenReturn(true);

        boolean result = tabulatedFunctionService.deleteAllTabulatedFunctions(functionId);

        assertTrue(result);
        verify(tabulatedFunctionRepository).deleteAllTabulatedFunctions(functionId);
    }

    @Test
    @DisplayName("Should get tabulated function by ID")
    void getTabulatedFunctionById_Success() throws SQLException {
        Long pointId = 1L;
        TabulatedFunction entity = createTabulatedFunction(pointId, 1L, 2.5, 3.7);
        TabulatedFunctionResponseDTO responseDTO = createTabulatedFunctionResponseDTO(pointId, 2.5, 3.7);

        when(tabulatedFunctionRepository.findById(pointId)).thenReturn(Optional.of(entity));
        when(dtoTransformService.toResponseDTO(entity)).thenReturn(responseDTO);

        Optional<TabulatedFunctionResponseDTO> result = tabulatedFunctionService.getTabulatedFunctionById(pointId);

        assertTrue(result.isPresent());
        assertEquals(pointId, result.get().getId());
        verify(tabulatedFunctionRepository).findById(pointId);
        verify(dtoTransformService).toResponseDTO(entity);
    }

    @Test
    @DisplayName("Should get tabulated function entity by ID")
    void getTabulatedFunctionEntityById_Success() throws SQLException {
        Long pointId = 1L;
        TabulatedFunction expectedEntity = createTabulatedFunction(pointId, 1L, 2.5, 3.7);

        when(tabulatedFunctionRepository.findById(pointId)).thenReturn(Optional.of(expectedEntity));

        Optional<TabulatedFunction> result = tabulatedFunctionService.getTabulatedFunctionEntityById(pointId);

        assertTrue(result.isPresent());
        assertEquals(pointId, result.get().getId());
        verify(tabulatedFunctionRepository).findById(pointId);
    }

    @Test
    @DisplayName("Should get tabulated function entities by function ID")
    void getTabulatedFunctionEntitiesByFunctionId_Success() throws SQLException {
        Long functionId = 1L;
        List<TabulatedFunction> expectedEntities = Arrays.asList(
                createTabulatedFunction(1L, functionId, 1.0, 1.0),
                createTabulatedFunction(2L, functionId, 2.0, 4.0)
        );

        when(tabulatedFunctionRepository.findAllByFunctionId(functionId)).thenReturn(expectedEntities);

        List<TabulatedFunction> result = tabulatedFunctionService.getTabulatedFunctionEntitiesByFunctionId(functionId);

        assertEquals(2, result.size());
        assertEquals(functionId, result.get(0).getFunctionId());
        assertEquals(functionId, result.get(1).getFunctionId());
        verify(tabulatedFunctionRepository).findAllByFunctionId(functionId);
    }

    @Test
    @DisplayName("Should handle SQLException from repository")
    void repositoryMethods_ThrowSQLException() throws SQLException {
        Long functionId = 1L;
        SQLException expectedException = new SQLException("Database error");

        when(tabulatedFunctionRepository.findAllByFunctionId(functionId)).thenThrow(expectedException);

        SQLException exception = assertThrows(SQLException.class, () ->
                tabulatedFunctionService.getTabulatedFunctionsByFunctionId(functionId)
        );
        assertEquals("Database error", exception.getMessage());
    }

    @Test
    @DisplayName("Should use default constructor")
    void defaultConstructor() {
        TabulatedFunctionService serviceWithDefaultConstructor = new TabulatedFunctionService();

        assertNotNull(serviceWithDefaultConstructor);
    }

    private TabulatedFunction createTabulatedFunction(Long id, Long functionId, Double xVal, Double yVal) {
        TabulatedFunction entity = new TabulatedFunction();
        entity.setId(id);
        entity.setFunctionId(functionId);
        entity.setXVal(xVal);
        entity.setYVal(yVal);
        return entity;
    }

    private TabulatedFunctionResponseDTO createTabulatedFunctionResponseDTO(Long id, Double xVal, Double yVal) {
        TabulatedFunctionResponseDTO dto = new TabulatedFunctionResponseDTO();
        dto.setId(id);
        dto.setFunctionId(1L);
        dto.setXVal(xVal);
        dto.setYVal(yVal);
        return dto;
    }
}