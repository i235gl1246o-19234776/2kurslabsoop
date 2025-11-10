package model.dto.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchFunctionResponseDTOTest {

    @Test
    void noArgsConstructor() {
        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO();

        assertNull(response.getFunctions());
        assertEquals(0, response.getTotal());
        assertNull(response.getOperationsTypeId());
    }

    @Test
    @DisplayName("Should create object with all-args constructor")
    void allArgsConstructor() {
        List<FunctionResponseDTO> functions = Arrays.asList(
                new FunctionResponseDTO(1L, "function1", "user1", "analytic", null, null),
                new FunctionResponseDTO(2L, "function2", "user2", "tabulated", null, null)
        );
        int total = 2;
        Long operationsTypeId = 5L;

        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(functions, total, operationsTypeId);

        assertEquals(functions, response.getFunctions());
        assertEquals(total, response.getTotal());
        assertEquals(operationsTypeId, response.getOperationsTypeId());
    }

    @Test
    @DisplayName("Should create object with partial constructor (functions and total)")
    void partialConstructor() {
        List<FunctionResponseDTO> functions = Arrays.asList(
                new FunctionResponseDTO(1L, "function1", "user1", "analytic", null, null)
        );
        int total = 1;

        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(functions, total);

        assertEquals(functions, response.getFunctions());
        assertEquals(total, response.getTotal());
        assertNull(response.getOperationsTypeId());
    }

    @Test
    @DisplayName("Should handle empty functions list")
    void emptyFunctionsList() {
        List<FunctionResponseDTO> emptyList = Collections.emptyList();
        int total = 0;
        Long operationsTypeId = 3L;

        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(emptyList, total, operationsTypeId);

        assertTrue(response.getFunctions().isEmpty());
        assertEquals(0, response.getTotal());
        assertEquals(operationsTypeId, response.getOperationsTypeId());
    }

    @Test
    @DisplayName("Should handle null functions list")
    void nullFunctionsList() {
        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(null, 0, 1L);

        assertNull(response.getFunctions());
        assertEquals(0, response.getTotal());
        assertEquals(1L, response.getOperationsTypeId());
    }

    @Test
    @DisplayName("Should handle null operationsTypeId")
    void nullOperationsTypeId() {
        List<FunctionResponseDTO> functions = Arrays.asList(
                new FunctionResponseDTO(1L, "function1", "user1", "analytic", null, null)
        );
        int total = 1;

        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(functions, total, null);


        assertEquals(functions, response.getFunctions());
        assertEquals(total, response.getTotal());
        assertNull(response.getOperationsTypeId());
    }

    @Test
    @DisplayName("Should set and get functions")
    void setAndGetFunctions() {
        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO();
        List<FunctionResponseDTO> functions = Arrays.asList(
                new FunctionResponseDTO(1L, "func1", "user1", "analytic", null, null),
                new FunctionResponseDTO(2L, "func2", "user2", "tabulated", null, null)
        );

        response.setFunctions(functions);

        assertEquals(functions, response.getFunctions());
        assertEquals(2, response.getFunctions().size());
    }

    @Test
    @DisplayName("Should set and get total")
    void setAndGetTotal() {
        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO();
        int total = 42;

        response.setTotal(total);

        assertEquals(total, response.getTotal());
    }

    @Test
    @DisplayName("Should set and get operationsTypeId")
    void setAndGetOperationsTypeId() {
        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO();
        Long operationsTypeId = 10L;

        response.setOperationsTypeId(operationsTypeId);

        assertEquals(operationsTypeId, response.getOperationsTypeId());
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        List<FunctionResponseDTO> functions = Arrays.asList(
                new FunctionResponseDTO(1L, "testFunction", "testUser", "analytic", null, null)
        );
        SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(functions, 1, 5L);

        String toStringResult = response.toString();

        assertNotNull(toStringResult);
    }

    @Test
    @DisplayName("Should test equals method")
    void testEquals() {
        List<FunctionResponseDTO> functions1 = Arrays.asList(
                new FunctionResponseDTO(1L, "func1", "user1", "analytic", null, null)
        );
        List<FunctionResponseDTO> functions2 = Arrays.asList(
                new FunctionResponseDTO(1L, "func1", "user1", "analytic", null, null)
        );
        List<FunctionResponseDTO> differentFunctions = Arrays.asList(
                new FunctionResponseDTO(2L, "func2", "user2", "tabulated", null, null)
        );

        SearchFunctionResponseDTO response1 = new SearchFunctionResponseDTO(functions1, 1, 5L);
        SearchFunctionResponseDTO response2 = new SearchFunctionResponseDTO(functions2, 1, 5L);

        assertEquals(response1, response2);

    }

    @Test
    @DisplayName("Should test hashCode method")
    void testHashCode() {
        List<FunctionResponseDTO> functions1 = Arrays.asList(
                new FunctionResponseDTO(1L, "func1", "user1", "analytic", null, null)
        );
        List<FunctionResponseDTO> functions2 = Arrays.asList(
                new FunctionResponseDTO(1L, "func1", "user1", "analytic", null, null)
        );

        SearchFunctionResponseDTO response1 = new SearchFunctionResponseDTO(functions1, 1, 5L);
        SearchFunctionResponseDTO response2 = new SearchFunctionResponseDTO(functions2, 1, 5L);
        SearchFunctionResponseDTO response3 = new SearchFunctionResponseDTO(functions1, 2, 5L);

        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle negative total")
        void negativeTotal() {
            SearchFunctionResponseDTO response = new SearchFunctionResponseDTO();

            response.setTotal(-5);

            assertEquals(-5, response.getTotal());
        }

        @Test
        @DisplayName("Should handle zero total with non-empty functions")
        void zeroTotalWithNonEmptyFunctions() {
            List<FunctionResponseDTO> functions = Arrays.asList(
                    new FunctionResponseDTO(1L, "func1", "user1", "analytic", null, null)
            );

            SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(functions, 0, 1L);

            assertEquals(1, response.getFunctions().size());
            assertEquals(0, response.getTotal());
        }

        @Test
        @DisplayName("Should handle large total value")
        void largeTotalValue() {
            SearchFunctionResponseDTO response = new SearchFunctionResponseDTO();
            int largeTotal = Integer.MAX_VALUE;

            response.setTotal(largeTotal);

            assertEquals(Integer.MAX_VALUE, response.getTotal());
        }

        @Test
        @DisplayName("Should handle large operationsTypeId")
        void largeOperationsTypeId() {
            SearchFunctionResponseDTO response = new SearchFunctionResponseDTO();
            Long largeId = Long.MAX_VALUE;

            response.setOperationsTypeId(largeId);

            assertEquals(Long.MAX_VALUE, response.getOperationsTypeId());
        }
    }

    @Nested
    @DisplayName("With FunctionResponseDTO Details")
    class WithFunctionResponseDetails {

        @Test
        @DisplayName("Should maintain function details in response")
        void maintainFunctionDetails() {
            FunctionResponseDTO function1 = new FunctionResponseDTO(1L, "sin(x)", "mathUser", "analytic", null, null);
            FunctionResponseDTO function2 = new FunctionResponseDTO(2L, "cos(x)", "physicsUser", "analytic", null, null);
            List<FunctionResponseDTO> functions = Arrays.asList(function1, function2);

            SearchFunctionResponseDTO response = new SearchFunctionResponseDTO(functions, 2, 3L);

            assertEquals(2, response.getFunctions().size());
        }

    }
}