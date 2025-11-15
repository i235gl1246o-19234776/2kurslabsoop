package service;

import model.entity.Operation;
import model.dto.request.OperationRequestDTO;
import model.dto.response.OperationResponseDTO;
import model.dto.DTOTransformService;
import model.service.OperationService;
import repository.dao.OperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private DTOTransformService dtoTransformService;

    private OperationService operationService;

    private Operation testOperation;
    private OperationRequestDTO testRequestDTO;
    private OperationResponseDTO testResponseDTO;

    @BeforeEach
    void setUp() {
        operationService = new OperationService(operationRepository, dtoTransformService);

        testOperation = new Operation();
        testOperation.setId(1L);
        testOperation.setFunctionId(100L);

        testRequestDTO = new OperationRequestDTO();
        testRequestDTO.setFunctionId(100L);

        testResponseDTO = new OperationResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setFunctionId(100L);
    }

    @Test
    void createOperation_Success() throws SQLException {
        when(dtoTransformService.toEntity(testRequestDTO)).thenReturn(testOperation);
        when(operationRepository.createOperation(testOperation)).thenReturn(1L);
        when(operationRepository.findById(1L, 100L)).thenReturn(Optional.of(testOperation));
        when(dtoTransformService.toResponseDTO(testOperation)).thenReturn(testResponseDTO);

        OperationResponseDTO result = operationService.createOperation(testRequestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getFunctionId());

        verify(dtoTransformService).toEntity(testRequestDTO);
        verify(operationRepository).createOperation(testOperation);
        verify(operationRepository).findById(1L, 100L);
        verify(dtoTransformService).toResponseDTO(testOperation);
    }

    @Test
    void createOperation_NotFoundAfterCreation() throws SQLException {
        when(dtoTransformService.toEntity(testRequestDTO)).thenReturn(testOperation);
        when(operationRepository.createOperation(testOperation)).thenReturn(1L);
        when(operationRepository.findById(1L, 100L)).thenReturn(Optional.empty());

        SQLException exception = assertThrows(SQLException.class, () -> {
            operationService.createOperation(testRequestDTO);
        });

        assertEquals("Не удалось получить созданную операцию с ID: 1", exception.getMessage());

        verify(dtoTransformService).toEntity(testRequestDTO);
        verify(operationRepository).createOperation(testOperation);
        verify(operationRepository).findById(1L, 100L);
    }

    @Test
    void createOperation_RepositoryThrowsException() throws SQLException {
        when(dtoTransformService.toEntity(testRequestDTO)).thenReturn(testOperation);
        when(operationRepository.createOperation(testOperation)).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> {
            operationService.createOperation(testRequestDTO);
        });

        verify(dtoTransformService).toEntity(testRequestDTO);
        verify(operationRepository).createOperation(testOperation);
        verify(operationRepository, never()).findById(anyLong(), anyLong());
    }

    @Test
    void getOperationById_Found() throws SQLException {
        when(operationRepository.findById(1L, 100L)).thenReturn(Optional.of(testOperation));
        when(dtoTransformService.toResponseDTO(testOperation)).thenReturn(testResponseDTO);

        Optional<OperationResponseDTO> result = operationService.getOperationById(1L, 100L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());

        verify(operationRepository).findById(1L, 100L);
        verify(dtoTransformService).toResponseDTO(testOperation);
    }

    @Test
    void getOperationById_NotFound() throws SQLException {
        when(operationRepository.findById(1L, 100L)).thenReturn(Optional.empty());

        Optional<OperationResponseDTO> result = operationService.getOperationById(1L, 100L);

        assertTrue(result.isEmpty());

        verify(operationRepository).findById(1L, 100L);
    }

    @Test
    void updateOperation_Success() throws SQLException {
        when(operationRepository.updateOperation(testOperation)).thenReturn(true);

        boolean result = operationService.updateOperation(testOperation);

        assertTrue(result);
        verify(operationRepository).updateOperation(testOperation);
    }

    @Test
    void updateOperation_Failure() throws SQLException {
        when(operationRepository.updateOperation(testOperation)).thenReturn(false);

        boolean result = operationService.updateOperation(testOperation);

        assertFalse(result);
        verify(operationRepository).updateOperation(testOperation);
    }

    @Test
    void deleteOperation_Success() throws SQLException {
        when(operationRepository.deleteOperation(1L, 100L)).thenReturn(true);

        boolean result = operationService.deleteOperation(1L, 100L);

        assertTrue(result);
        verify(operationRepository).deleteOperation(1L, 100L);
    }

    @Test
    void deleteOperation_Failure() throws SQLException {
        when(operationRepository.deleteOperation(1L, 100L)).thenReturn(false);

        boolean result = operationService.deleteOperation(1L, 100L);

        assertFalse(result);
        verify(operationRepository).deleteOperation(1L, 100L);
    }

    @Test
    void deleteAllOperations_Success() throws SQLException {
        when(operationRepository.deleteAllOperations(100L)).thenReturn(true);

        boolean result = operationService.deleteAllOperations(100L);

        assertTrue(result);
        verify(operationRepository).deleteAllOperations(100L);
    }

    @Test
    void deleteAllOperations_Failure() throws SQLException {
        when(operationRepository.deleteAllOperations(100L)).thenReturn(false);

        boolean result = operationService.deleteAllOperations(100L);

        assertFalse(result);
        verify(operationRepository).deleteAllOperations(100L);
    }

    @Test
    void getOperationEntityById_Found() throws SQLException {
        when(operationRepository.findById(1L, 100L)).thenReturn(Optional.of(testOperation));

        Optional<Operation> result = operationService.getOperationEntityById(1L, 100L);

        assertTrue(result.isPresent());
        assertEquals(testOperation, result.get());
        verify(operationRepository).findById(1L, 100L);
    }

    @Test
    void getOperationEntityById_NotFound() throws SQLException {
        when(operationRepository.findById(1L, 100L)).thenReturn(Optional.empty());

        Optional<Operation> result = operationService.getOperationEntityById(1L, 100L);

        assertTrue(result.isEmpty());
        verify(operationRepository).findById(1L, 100L);
    }

    @Test
    void defaultConstructor_ShouldCreateInstances() {
        OperationService serviceWithDefaultConstructor = new OperationService();

        assertNotNull(serviceWithDefaultConstructor);
    }
}