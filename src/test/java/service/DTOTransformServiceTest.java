package service;

import model.dto.DTOTransformService;
import model.dto.request.*;
import model.dto.response.*;
import model.entity.Function;
import model.entity.Operation;
import model.entity.TabulatedFunction;
import model.entity.User;
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
        User user = dtoTransformService.toEntity(userRequestDTO);

        assertNotNull(user);
        assertEquals("testuser", user.getName());
        assertEquals("password123", user.getPasswordHash());
        assertNull(user.getId());
    }

    @Test
    void testUserEntityToResponseDTO() {
        User user = new User("testuser", "password123");
        user.setId(1L);

        UserResponseDTO responseDTO = dtoTransformService.toResponseDTO(user);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("testuser", responseDTO.getName());
    }

    @Test
    void testFunctionRequestToEntity() {
        Function function = dtoTransformService.toEntity(functionRequestDTO);

        assertNotNull(function);
        assertEquals(1L, function.getUserId());
        assertEquals("analytic", function.getTypeFunction());
        assertEquals("test_function", function.getFunctionName());
        assertEquals("x^2", function.getFunctionExpression());
    }

    @Test
    void testFunctionEntityToResponseDTO() {
        Function function = new Function(1L, "analytic", "test_function", "x^2");
        function.setId(1L);

        FunctionResponseDTO responseDTO = dtoTransformService.toResponseDTO(function);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals(1L, responseDTO.getUserId());
        assertEquals("analytic", responseDTO.getTypeFunction());
        assertEquals("test_function", responseDTO.getFunctionName());
        assertEquals("x^2", responseDTO.getFunctionExpression());
    }

    @Test
    void testTabulatedFunctionRequestToEntity() {
        TabulatedFunction tabulatedFunction = dtoTransformService.toEntity(tabulatedFunctionRequestDTO);

        assertNotNull(tabulatedFunction);
        assertEquals(1L, tabulatedFunction.getFunctionId());
        assertEquals(1.0, tabulatedFunction.getXVal());
        assertEquals(2.0, tabulatedFunction.getYVal());
    }

    @Test
    void testTabulatedFunctionEntityToResponseDTO() {
        TabulatedFunction tabulatedFunction = new TabulatedFunction(1L, 1.0, 2.0);
        tabulatedFunction.setId(1L);

        TabulatedFunctionResponseDTO responseDTO = dtoTransformService.toResponseDTO(tabulatedFunction);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals(1L, responseDTO.getFunctionId());
        assertEquals(1.0, responseDTO.getXVal());
        assertEquals(2.0, responseDTO.getYVal());
    }

    @Test
    void testOperationRequestToEntity() {
        Operation operation = dtoTransformService.toEntity(operationRequestDTO);

        assertNotNull(operation);
        assertEquals(1L, operation.getFunctionId());
        assertEquals(1, operation.getOperationsTypeId());
    }

    @Test
    void testOperationEntityToResponseDTO() {
        Operation operation = new Operation(1L, 1);
        operation.setId(1L);

        OperationResponseDTO responseDTO = dtoTransformService.toResponseDTO(operation);

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals(1L, responseDTO.getFunctionId());
        assertEquals(1, responseDTO.getOperationsTypeId());
    }

    @Test
    void testBatchTabulatedFunctionTransformations() {
        List<TabulatedFunctionRequestDTO> requestDTOs = Arrays.asList(
                new TabulatedFunctionRequestDTO(1L, 1.0, 2.0),
                new TabulatedFunctionRequestDTO(1L, 2.0, 4.0),
                new TabulatedFunctionRequestDTO(1L, 3.0, 6.0)
        );

        List<TabulatedFunction> entities = dtoTransformService.toEntities(requestDTOs);

        assertNotNull(entities);
        assertEquals(3, entities.size());
        assertEquals(1L, entities.get(0).getFunctionId());
        assertEquals(4.0, entities.get(1).getYVal());

        List<TabulatedFunctionResponseDTO> responseDTOs = dtoTransformService.toResponseDTOs(entities);

        assertNotNull(responseDTOs);
        assertEquals(3, responseDTOs.size());
    }

    @Test
    void testBatchUserTransformations() {
        List<User> users = Arrays.asList(
                new User("user1", "pass1"),
                new User("user2", "pass2"),
                new User("user3", "pass3")
        );
        users.get(0).setId(1L);
        users.get(1).setId(2L);
        users.get(2).setId(3L);

        List<UserResponseDTO> responseDTOs = dtoTransformService.toUserResponseDTOs(users);

        assertNotNull(responseDTOs);
        assertEquals(3, responseDTOs.size());
        assertEquals("user1", responseDTOs.get(0).getName());
        assertEquals("user2", responseDTOs.get(1).getName());
        assertEquals("user3", responseDTOs.get(2).getName());
    }

    @Test
    void testBatchFunctionTransformations() {
        List<Function> functions = Arrays.asList(
                new Function(1L, "analytic", "func1", "x^2"),
                new Function(1L, "tabular", "func2", null),
                new Function(2L, "analytic", "func3", "sin(x)")
        );
        functions.get(0).setId(1L);
        functions.get(1).setId(2L);
        functions.get(2).setId(3L);

        List<FunctionResponseDTO> responseDTOs = dtoTransformService.toFunctionResponseDTOs(functions);

        assertNotNull(responseDTOs);
        assertEquals(3, responseDTOs.size());
        assertEquals("func1", responseDTOs.get(0).getFunctionName());
        assertEquals("tabular", responseDTOs.get(1).getTypeFunction());
        assertEquals("sin(x)", responseDTOs.get(2).getFunctionExpression());
    }


    @Test
    void testDTOToString() {
        String toString = userRequestDTO.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("UserRequestDTO"));
        assertTrue(toString.contains("testuser"));
    }

    @Test
    void testUserResponseDTOWithoutCreatedAt() {
        UserResponseDTO responseDTO = new UserResponseDTO(1L, "testuser");

        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals("testuser", responseDTO.getName());
    }
}