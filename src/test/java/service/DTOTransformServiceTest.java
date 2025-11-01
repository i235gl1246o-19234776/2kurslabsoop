package service;

import model.dto.request.*;
import model.dto.response.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DTOTransformServiceTest {

    private DTOTransformService dtoTransformService;

    private CreateUserRequest userRequestDTO;
    private FunctionRequestDTO functionRequestDTO;
    private TabulatedFunctionRequestDTO tabulatedFunctionRequestDTO;
    private OperationRequestDTO operationRequestDTO;

    @BeforeEach
    void setUp() {
        dtoTransformService = new DTOTransformService();
        userRequestDTO = new CreateUserRequest("testuser", "password123");
        functionRequestDTO = new FunctionRequestDTO(1L, "analytic", "test_function", "x^2");
        tabulatedFunctionRequestDTO = new TabulatedFunctionRequestDTO(1L, 1.0, 2.0);
        operationRequestDTO = new OperationRequestDTO(1L, 1);
    }

    @Test
    void testUserRequestToEntity() {
        // When
        User user = dtoTransformService.toEntity(userRequestDTO);

        // Then
        assertNotNull(user);
        assertEquals("testuser", user.getName());
        assertEquals("password123", user.getPasswordHash());
        assertNull(user.getId()); // ID должен быть null для новой entity
    }

    @Test
    void testUserEntityToResponseDTO() {
        // Given
        User user = new User("testuser", "password123");
        user.setId(1L);

        // When
        UserResponseDTO responseDTO = dtoTransformService.toResponseDTO(user);

        // Then
        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("testuser", responseDTO.getName());
    }

    @Test
    void testFunctionRequestToEntity() {
        // When
        Function function = dtoTransformService.toEntity(functionRequestDTO);

        // Then
        assertNotNull(function);
        assertEquals(1L, function.getUserId());
        assertEquals("analytic", function.getTypeFunction());
        assertEquals("test_function", function.getFunctionName());
        assertEquals("x^2", function.getFunctionExpression());
    }

    @Test
    void testFunctionEntityToResponseDTO() {
        // Given
        Function function = new Function(1L, "analytic", "test_function", "x^2");
        function.setId(1L);

        // When
        FunctionResponseDTO responseDTO = dtoTransformService.toResponseDTO(function);

        // Then
        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals(1L, responseDTO.getUserId());
        assertEquals("analytic", responseDTO.getTypeFunction());
        assertEquals("test_function", responseDTO.getFunctionName());
        assertEquals("x^2", responseDTO.getFunctionExpression());
    }

    @Test
    void testTabulatedFunctionRequestToEntity() {
        // When
        TabulatedFunction tabulatedFunction = dtoTransformService.toEntity(tabulatedFunctionRequestDTO);

        // Then
        assertNotNull(tabulatedFunction);
        assertEquals(1L, tabulatedFunction.getFunctionId());
        assertEquals(1.0, tabulatedFunction.getXVal());
        assertEquals(2.0, tabulatedFunction.getYVal());
    }

    @Test
    void testTabulatedFunctionEntityToResponseDTO() {
        // Given
        TabulatedFunction tabulatedFunction = new TabulatedFunction(1L, 1.0, 2.0);
        tabulatedFunction.setId(1L);

        // When
        TabulatedFunctionResponseDTO responseDTO = dtoTransformService.toResponseDTO(tabulatedFunction);

        // Then
        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals(1L, responseDTO.getFunctionId());
        assertEquals(1.0, responseDTO.getXVal());
        assertEquals(2.0, responseDTO.getYVal());
    }

    @Test
    void testOperationRequestToEntity() {
        // When
        Operation operation = dtoTransformService.toEntity(operationRequestDTO);

        // Then
        assertNotNull(operation);
        assertEquals(1L, operation.getFunctionId());
        assertEquals(1, operation.getOperationsTypeId());
    }

    @Test
    void testOperationEntityToResponseDTO() {
        // Given
        Operation operation = new Operation(1L, 1);
        operation.setId(1L);

        // When
        OperationResponseDTO responseDTO = dtoTransformService.toResponseDTO(operation);

        // Then
        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals(1L, responseDTO.getFunctionId());
        assertEquals(1, responseDTO.getOperationsTypeId());
    }

    @Test
    void testBatchTabulatedFunctionTransformations() {
        // Given
        List<TabulatedFunctionRequestDTO> requestDTOs = Arrays.asList(
                new TabulatedFunctionRequestDTO(1L, 1.0, 2.0),
                new TabulatedFunctionRequestDTO(1L, 2.0, 4.0),
                new TabulatedFunctionRequestDTO(1L, 3.0, 6.0)
        );

        // When
        List<TabulatedFunction> entities = dtoTransformService.toEntities(requestDTOs);

        // Then
        assertNotNull(entities);
        assertEquals(3, entities.size());
        assertEquals(1L, entities.get(0).getFunctionId());
        assertEquals(4.0, entities.get(1).getYVal());

        // When преобразуем обратно в ResponseDTO
        List<TabulatedFunctionResponseDTO> responseDTOs = dtoTransformService.toResponseDTOs(entities);

        // Then
        assertNotNull(responseDTOs);
        assertEquals(3, responseDTOs.size());
    }

    @Test
    void testBatchUserTransformations() {
        // Given
        List<User> users = Arrays.asList(
                new User("user1", "pass1"),
                new User("user2", "pass2"),
                new User("user3", "pass3")
        );
        users.get(0).setId(1L);
        users.get(1).setId(2L);
        users.get(2).setId(3L);

        // When
        List<UserResponseDTO> responseDTOs = dtoTransformService.toUserResponseDTOs(users);

        // Then
        assertNotNull(responseDTOs);
        assertEquals(3, responseDTOs.size());
        assertEquals("user1", responseDTOs.get(0).getName());
        assertEquals("user2", responseDTOs.get(1).getName());
        assertEquals("user3", responseDTOs.get(2).getName());
    }

    @Test
    void testBatchFunctionTransformations() {
        // Given
        List<Function> functions = Arrays.asList(
                new Function(1L, "analytic", "func1", "x^2"),
                new Function(1L, "tabular", "func2", null),
                new Function(2L, "analytic", "func3", "sin(x)")
        );
        functions.get(0).setId(1L);
        functions.get(1).setId(2L);
        functions.get(2).setId(3L);

        // When
        List<FunctionResponseDTO> responseDTOs = dtoTransformService.toFunctionResponseDTOs(functions);

        // Then
        assertNotNull(responseDTOs);
        assertEquals(3, responseDTOs.size());
        assertEquals("func1", responseDTOs.get(0).getFunctionName());
        assertEquals("tabular", responseDTOs.get(1).getTypeFunction());
        assertEquals("sin(x)", responseDTOs.get(2).getFunctionExpression());
    }


    @Test
    void testDTOToString() {
        // When
        String toString = userRequestDTO.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("UserRequestDTO"));
        assertTrue(toString.contains("testuser"));
    }

    @Test
    void testUserResponseDTOWithoutCreatedAt() {
        // Given
        UserResponseDTO responseDTO = new UserResponseDTO(1L, "testuser");

        // Then
        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("testuser", responseDTO.getName());
    }
}