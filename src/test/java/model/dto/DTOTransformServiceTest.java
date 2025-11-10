package model.dto;

import model.dto.request.CreateUserRequest;
import model.dto.request.FunctionRequestDTO;
import model.entity.Function;
import model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class DTOTransformServiceEdgeCasesTest {

    private DTOTransformService dtoTransformService = new DTOTransformService();

    @Test
    @DisplayName("Should handle null CreateUserRequest")
    void toEntity_NullCreateUserRequest() {
        assertThrows(NullPointerException.class, () -> {
            dtoTransformService.toEntity((CreateUserRequest) null);
        });
    }

    @Test
    @DisplayName("Should handle null FunctionRequestDTO")
    void toEntity_NullFunctionRequestDTO() {
        assertThrows(NullPointerException.class, () -> {
            dtoTransformService.toEntity((FunctionRequestDTO) null);
        });
    }

    @Test
    @DisplayName("Should handle null User entity in toResponseDTO")
    void toResponseDTO_NullUser() {
        assertThrows(NullPointerException.class, () -> {
            dtoTransformService.toResponseDTO((User) null);
        });
    }

    @Test
    @DisplayName("Should handle null Function entity in toResponseDTO")
    void toResponseDTO_NullFunction() {
        assertThrows(NullPointerException.class, () -> {
            dtoTransformService.toResponseDTO((Function) null);
        });
    }

    @Test
    @DisplayName("Should handle null list in batch transformations")
    void batchTransformations_NullList() {
        assertThrows(NullPointerException.class, () -> {
            dtoTransformService.toEntities(null);
        });

        assertThrows(NullPointerException.class, () -> {
            dtoTransformService.toResponseDTOs(null);
        });

        assertThrows(NullPointerException.class, () -> {
            dtoTransformService.toUserResponseDTOs(null);
        });

        assertThrows(NullPointerException.class, () -> {
            dtoTransformService.toFunctionResponseDTOs(null);
        });
    }
}