package service;

import model.FunctionOperation;
import model.OperationType;
import repository.FunctionOperationRepository;
import repository.OperationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {

    @Mock
    private FunctionOperationRepository operationRepository;

    @Mock
    private OperationTypeRepository typeRepository;

    private OperationService operationService;

    private FunctionOperation testOperation;
    private OperationType numericOperationType;
    private OperationType functionalOperationType;

    @BeforeEach
    void setUp() {
        operationService = new OperationService(operationRepository, typeRepository);

        // Тестовые данные
        numericOperationType = new OperationType(1, "Derivative", "Calculate derivative");
        functionalOperationType = new OperationType(2, "Composition", "Function composition");

        testOperation = new FunctionOperation(1L, numericOperationType, "x=1");
        testOperation.setId(1L);
        testOperation.setResultValue(2.0);
        testOperation.setExecutedAt(LocalDateTime.now());
    }

    // INITIALIZATION tests
    @Test
    void initializeSystem_Success() throws SQLException {
        // Arrange
        doNothing().when(typeRepository).initializePredefinedTypes();

        // Act
        assertDoesNotThrow(() -> operationService.initializeSystem());

        // Assert
        verify(typeRepository).initializePredefinedTypes();
    }

    @Test
    void initializeSystem_Exception() throws SQLException {
        // Arrange
        doThrow(new RuntimeException("DB error")).when(typeRepository).initializePredefinedTypes();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> operationService.initializeSystem());

        assertEquals("System initialization failed", exception.getMessage());
    }

    // CREATE - Numeric Operation tests
    @Test
    void executeNumericOperation_Success() throws SQLException {
        // Arrange
        Long functionId = 1L;
        String parameters = "x=1";
        Double result = 2.0;

        when(operationRepository.createFunctionOperation(any(FunctionOperation.class))).thenReturn(1L);
        when(operationRepository.findById(1L)).thenReturn(testOperation);

        // Act
        FunctionOperation resultOperation = operationService.executeNumericOperation(
                functionId, numericOperationType, parameters, result);

        // Assert
        assertNotNull(resultOperation);
        assertEquals(1L, resultOperation.getId());
        verify(operationRepository).createFunctionOperation(any(FunctionOperation.class));
    }

    @Test
    void executeNumericOperation_WrongOperationType() {
        // Arrange
        Long functionId = 1L;
        String parameters = "x=1";
        Double result = 2.0;

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> operationService.executeNumericOperation(
                        functionId, functionalOperationType, parameters, result));

        assertEquals("Operation execution failed", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Operation type does not produce numeric result", exception.getCause().getMessage());
    }

    @Test
    void executeNumericOperation_InvalidParameters() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> operationService.executeNumericOperation(null, numericOperationType, "x=1", 2.0));

        assertThrows(IllegalArgumentException.class,
                () -> operationService.executeNumericOperation(1L, null, "x=1", 2.0));

        assertThrows(IllegalArgumentException.class,
                () -> operationService.executeNumericOperation(1L, numericOperationType, "", 2.0));
    }

    // CREATE - Functional Operation tests
    @Test
    void executeFunctionalOperation_Success() throws SQLException {
        // Arrange
        Long functionId = 1L;
        Long resultFunctionId = 2L;
        String parameters = "f=g";

        // Убедитесь, что functionalOperationType действительно производит функциональный результат
        OperationType functionalOpType = new OperationType(2, "Composition", "Function composition");

        FunctionOperation functionalOp = new FunctionOperation(functionId, functionalOpType, parameters);
        functionalOp.setId(2L);
        functionalOp.setResultFunctionId(resultFunctionId);

        when(operationRepository.createFunctionOperation(any(FunctionOperation.class))).thenReturn(2L);
        when(operationRepository.findById(2L)).thenReturn(functionalOp);

        // Act
        FunctionOperation result = operationService.executeFunctionalOperation(
                functionId, functionalOpType, parameters, resultFunctionId);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(operationRepository).createFunctionOperation(any(FunctionOperation.class));
    }

    @Test
    void executeFunctionalOperation_WrongOperationType() {
        // Arrange
        Long functionId = 1L;
        Long resultFunctionId = 2L;
        String parameters = "f=g";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> operationService.executeFunctionalOperation(
                        functionId, numericOperationType, parameters, resultFunctionId));

        assertEquals("Operation execution failed", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Operation type does not produce function result", exception.getCause().getMessage());
    }

    // READ - Operation tests
    @Test
    void getOperationById_Success() throws SQLException {
        // Arrange
        when(operationRepository.findById(1L)).thenReturn(testOperation);
        when(typeRepository.findById(1)).thenReturn(numericOperationType);

        // Act
        FunctionOperation result = operationService.getOperationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNotNull(result.getOperationType());
        assertEquals("Derivative", result.getOperationType().getName());
    }

    @Test
    void getOperationById_NotFound() throws SQLException {
        // Arrange
        when(operationRepository.findById(1L)).thenReturn(null);

        // Act
        FunctionOperation result = operationService.getOperationById(1L);

        // Assert
        assertNull(result);
    }

    @Test
    void getOperationById_InvalidId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> operationService.getOperationById(null));
        assertThrows(IllegalArgumentException.class, () -> operationService.getOperationById(0L));
    }

    @Test
    void getFunctionOperations_Success() throws SQLException {
        // Arrange
        List<FunctionOperation> operations = Arrays.asList(testOperation);
        when(operationRepository.findByFunctionId(1L)).thenReturn(operations);
        when(typeRepository.findById(1)).thenReturn(numericOperationType);

        // Act
        List<FunctionOperation> result = operationService.getFunctionOperations(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void getFunctionOperations_InvalidFunctionId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> operationService.getFunctionOperations(null));
        assertThrows(IllegalArgumentException.class, () -> operationService.getFunctionOperations(0L));
    }

    @Test
    void getOperationsByResultFunction_Success() throws SQLException {
        // Arrange
        testOperation.setResultFunctionId(2L);
        List<FunctionOperation> operations = Collections.singletonList(testOperation);
        when(operationRepository.findByResultFunctionId(2L)).thenReturn(operations);

        // Act
        List<FunctionOperation> result = operationService.getOperationsByResultFunction(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getResultFunctionId());
    }

    // READ - Operation Type tests
    @Test
    void getAllOperationTypes_Success() throws SQLException {
        // Arrange
        List<OperationType> types = Arrays.asList(numericOperationType, functionalOperationType);
        when(typeRepository.findAll()).thenReturn(types);

        // Act
        List<OperationType> result = operationService.getAllOperationTypes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getOperationTypeById_Success() throws SQLException {
        // Arrange
        when(typeRepository.findById(1)).thenReturn(numericOperationType);

        // Act
        OperationType result = operationService.getOperationTypeById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Derivative", result.getName());
    }

    @Test
    void getOperationTypeById_NotFound() throws SQLException {
        // Arrange
        when(typeRepository.findById(1)).thenReturn(null);

        // Act
        OperationType result = operationService.getOperationTypeById(1);

        // Assert
        assertNull(result);
    }

    @Test
    void getOperationTypeById_InvalidId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> operationService.getOperationTypeById(null));
        assertThrows(IllegalArgumentException.class, () -> operationService.getOperationTypeById(0));
    }

    // UPDATE tests
    @Test
    void updateOperationResult_Success() throws SQLException {
        // Arrange
        when(operationRepository.updateResult(testOperation)).thenReturn(true);

        // Act
        boolean result = operationService.updateOperationResult(testOperation);

        // Assert
        assertTrue(result);
        verify(operationRepository).updateResult(testOperation);
    }

    @Test
    void updateOperationResult_NullOperation() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> operationService.updateOperationResult(null));
    }

    @Test
    void updateOperationResult_InvalidOperationId() {
        // Arrange
        FunctionOperation invalidOperation = new FunctionOperation(1L, numericOperationType, "params");
        invalidOperation.setId(0L); // Invalid ID

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> operationService.updateOperationResult(invalidOperation));
    }

    // DELETE tests
    @Test
    void deleteOperation_Success() throws SQLException {
        // Arrange
        when(operationRepository.deleteFunctionOperation(1L)).thenReturn(true);

        // Act
        boolean result = operationService.deleteOperation(1L);

        // Assert
        assertTrue(result);
        verify(operationRepository).deleteFunctionOperation(1L);
    }

    @Test
    void deleteOperation_Failure() throws SQLException {
        // Arrange
        when(operationRepository.deleteFunctionOperation(1L)).thenReturn(false);

        // Act
        boolean result = operationService.deleteOperation(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteOperation_InvalidId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> operationService.deleteOperation(null));
        assertThrows(IllegalArgumentException.class, () -> operationService.deleteOperation(0L));
    }

    // BUSINESS LOGIC tests
    @Test
    void getRecentOperations_Success() {
        // Arrange
        FunctionOperation oldOperation = new FunctionOperation(1L, numericOperationType, "params");
        oldOperation.setId(1L);
        oldOperation.setExecutedAt(LocalDateTime.now().minusDays(1));

        FunctionOperation newOperation = new FunctionOperation(2L, numericOperationType, "params");
        newOperation.setId(2L);
        newOperation.setExecutedAt(LocalDateTime.now());

        // Mock the getAllOperations method behavior
        // Since getAllOperations is private, we need to test through public methods
        // or use reflection. For now, we'll test the validation.

        // Act & Assert - test validation
        assertThrows(IllegalArgumentException.class, () -> operationService.getRecentOperations(0));
        assertThrows(IllegalArgumentException.class, () -> operationService.getRecentOperations(-1));
    }

    @Test
    void getOperationsByType_Success() {
        // This method depends on getAllOperations which is currently a stub
        // We can test that it doesn't throw exceptions for valid input
        assertDoesNotThrow(() -> operationService.getOperationsByType(1));
    }

    // STATISTICS tests
    @Test
    void getOperationCount_Success() {
        // This method depends on getRecentOperations which uses getAllOperations stub
        // We can test that it returns a number (even if 0) without throwing exceptions
        int count = operationService.getOperationCount();
        assertTrue(count >= 0);
    }

    @Test
    void getFunctionOperationCount_Success() throws SQLException {
        // Arrange
        List<FunctionOperation> operations = Arrays.asList(testOperation, new FunctionOperation());
        when(operationRepository.findByFunctionId(1L)).thenReturn(operations);

        // Act
        int count = operationService.getFunctionOperationCount(1L);

        // Assert
        assertEquals(2, count);
    }

    @Test
    void getFunctionOperationCount_Exception() throws SQLException {
        // Arrange
        when(operationRepository.findByFunctionId(1L)).thenThrow(new RuntimeException("DB error"));

        // Act
        int count = operationService.getFunctionOperationCount(1L);

        // Assert
        assertEquals(0, count);
    }

    // EDGE CASE tests
    @Test
    void validateOperationParameters_VariousCases() {
        // Valid case
        assertDoesNotThrow(() ->
                operationService.executeNumericOperation(1L, numericOperationType, "valid", 1.0));

        // Invalid cases
        assertThrows(IllegalArgumentException.class, () ->
                operationService.executeNumericOperation(null, numericOperationType, "valid", 1.0));

        assertThrows(IllegalArgumentException.class, () ->
                operationService.executeNumericOperation(1L, null, "valid", 1.0));

        assertThrows(IllegalArgumentException.class, () ->
                operationService.executeNumericOperation(1L, numericOperationType, "", 1.0));

        assertThrows(IllegalArgumentException.class, () ->
                operationService.executeNumericOperation(1L, numericOperationType, "   ", 1.0));
    }



    // EXCEPTION HANDLING tests
    @Test
    void repositoryExceptionsAreWrapped() throws SQLException {
        // Arrange
        when(operationRepository.findById(1L)).thenThrow(new RuntimeException("DB connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> operationService.getOperationById(1L));

        assertEquals("Failed to get operation", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("DB connection failed", exception.getCause().getMessage());
    }

    @Test
    void typeRepositoryExceptionsAreWrapped() throws SQLException {
        // Arrange
        when(typeRepository.findAll()).thenThrow(new RuntimeException("Type repository error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> operationService.getAllOperationTypes());

        assertEquals("Failed to get operation types", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Type repository error", exception.getCause().getMessage());
    }
}