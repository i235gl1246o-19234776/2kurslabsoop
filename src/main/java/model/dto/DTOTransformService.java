package model.dto;

import model.dto.request.*;
import model.dto.response.*;
import model.entity.Function;
import model.entity.Operation;
import model.entity.TabulatedFunction;
import model.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DTOTransformService {
    private static final Logger logger = Logger.getLogger(DTOTransformService.class.getName());

    public User toEntity(CreateUserRequest userRequestDTO) {
        logger.info("Преобразование UserRequestDTO в User entity: " + userRequestDTO.getName());
        User user = new User(userRequestDTO.getName(), userRequestDTO.getPassword());
        logger.fine("User entity создан: " + user.getName());
        return user;
    }

    public UserResponseDTO toResponseDTO(User user) {
        logger.info("Преобразование User entity в UserResponseDTO: " + user.getName());
        UserResponseDTO responseDTO = new UserResponseDTO(
                user.getId(),
                user.getName()
        );
        logger.fine("UserResponseDTO создан для пользователя: " + user.getName());
        return responseDTO;
    }

    public Function toEntity(FunctionRequestDTO functionRequestDTO) {
        logger.info("Преобразование FunctionRequestDTO в Function entity: " + functionRequestDTO.getFunctionName());
        Function function = new Function(
                functionRequestDTO.getUserId(),
                functionRequestDTO.getTypeFunction(),
                functionRequestDTO.getFunctionName(),
                functionRequestDTO.getFunctionExpression()
        );
        logger.fine("Function entity создан: " + function.getFunctionName());
        return function;
    }

    public FunctionResponseDTO toResponseDTO(Function function) {
        logger.info("Преобразование Function entity в FunctionResponseDTO: " + function.getFunctionName());

        FunctionResponseDTO responseDTO = new FunctionResponseDTO();
        responseDTO.setFunctionId(function.getId());
        responseDTO.setFunctionName(function.getFunctionName());
        responseDTO.setTypeFunction(function.getTypeFunction());

        logger.fine("FunctionResponseDTO создан для функции: " + function.getFunctionName());
        return responseDTO;
    }

    public TabulatedFunction toEntity(TabulatedFunctionRequestDTO tabulatedFunctionRequestDTO) {
        logger.info("Преобразование TabulatedFunctionRequestDTO в TabulatedFunction entity");
        TabulatedFunction tabulatedFunction = new TabulatedFunction(
                tabulatedFunctionRequestDTO.getFunctionId(),
                tabulatedFunctionRequestDTO.getXVal(),
                tabulatedFunctionRequestDTO.getYVal()
        );
        logger.fine("TabulatedFunction entity создан для functionId: " + tabulatedFunction.getFunctionId());
        return tabulatedFunction;
    }

    public TabulatedFunctionResponseDTO toResponseDTO(TabulatedFunction tabulatedFunction) {
        logger.info("Преобразование TabulatedFunction entity в TabulatedFunctionResponseDTO");
        TabulatedFunctionResponseDTO responseDTO = new TabulatedFunctionResponseDTO(
                tabulatedFunction.getId(),
                tabulatedFunction.getFunctionId(),
                tabulatedFunction.getXVal(),
                tabulatedFunction.getYVal()
        );
        logger.fine("TabulatedFunctionResponseDTO создан для id: " + tabulatedFunction.getId());
        return responseDTO;
    }

    public Operation toEntity(OperationRequestDTO operationRequestDTO) {
        logger.info("Преобразование OperationRequestDTO в Operation entity");
        Operation operation = new Operation(
                operationRequestDTO.getFunctionId(),
                operationRequestDTO.getOperationsTypeId()
        );
        logger.fine("Operation entity создан для functionId: " + operation.getFunctionId());
        return operation;
    }

    public OperationResponseDTO toResponseDTO(Operation operation) {
        logger.info("Преобразование Operation entity в OperationResponseDTO");
        OperationResponseDTO responseDTO = new OperationResponseDTO(
                operation.getId(),
                operation.getFunctionId(),
                operation.getOperationsTypeId()
        );
        logger.fine("OperationResponseDTO создан для id: " + operation.getId());
        return responseDTO;
    }

    public List<TabulatedFunction> toEntities(List<TabulatedFunctionRequestDTO> dtos) {
        logger.info("Пакетное преобразование " + dtos.size() + " TabulatedFunctionRequestDTO в entities");
        List<TabulatedFunction> entities = new ArrayList<>();
        for (TabulatedFunctionRequestDTO dto : dtos) {
            entities.add(toEntity(dto));
        }
        logger.info("Пакетное преобразование завершено, создано " + entities.size() + " entities");
        return entities;
    }

    public List<TabulatedFunctionResponseDTO> toResponseDTOs(List<TabulatedFunction> entities) {
        logger.info("Пакетное преобразование " + entities.size() + " TabulatedFunction entities в ResponseDTO");
        List<TabulatedFunctionResponseDTO> dtos = new ArrayList<>();
        for (TabulatedFunction entity : entities) {
            dtos.add(toResponseDTO(entity));
        }
        logger.info("Пакетное преобразование завершено, создано " + dtos.size() + " ResponseDTO");
        return dtos;
    }

    public List<UserResponseDTO> toUserResponseDTOs(List<User> users) {
        logger.info("Пакетное преобразование " + users.size() + " User entities в ResponseDTO");
        List<UserResponseDTO> dtos = new ArrayList<>();
        for (User user : users) {
            dtos.add(toResponseDTO(user));
        }
        logger.info("Пакетное преобразование завершено, создано " + dtos.size() + " UserResponseDTO");
        return dtos;
    }

    public List<FunctionResponseDTO> toFunctionResponseDTOs(List<Function> functions) {
        logger.info("Пакетное преобразование " + functions.size() + " Function entities в ResponseDTO");
        List<FunctionResponseDTO> dtos = new ArrayList<>();
        for (Function function : functions) {
            dtos.add(toResponseDTO(function));
        }
        logger.info("Пакетное преобразование завершено, создано " + dtos.size() + " FunctionResponseDTO");
        return dtos;
    }
}