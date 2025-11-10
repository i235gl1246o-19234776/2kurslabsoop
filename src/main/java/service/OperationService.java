package service;

import model.entity.Operation;
import model.dto.request.OperationRequestDTO;
import model.dto.response.OperationResponseDTO;
import model.dto.DTOTransformService;
import repository.OperationRepository;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public class OperationService {
    private static final Logger logger = Logger.getLogger(OperationService.class.getName());
    private final OperationRepository operationRepository;
    private final DTOTransformService dtoTransformService;

    public OperationService() {
        this.operationRepository = new OperationRepository();
        this.dtoTransformService = new DTOTransformService();
    }

    public OperationService(OperationRepository operationRepository, DTOTransformService dtoTransformService) {
        this.operationRepository = operationRepository;
        this.dtoTransformService = dtoTransformService;
    }

    public OperationResponseDTO createOperation(OperationRequestDTO operationRequest) throws SQLException {
        logger.info("Создание новой операции для функции: " + operationRequest.getFunctionId());

        Operation operation = dtoTransformService.toEntity(operationRequest);
        Long operationId = operationRepository.createOperation(operation);

        Optional<Operation> createdOperation = operationRepository.findById(operationId, operationRequest.getFunctionId());
        if (createdOperation.isPresent()) {
            return dtoTransformService.toResponseDTO(createdOperation.get());
        } else {
            throw new SQLException("Не удалось получить созданную операцию с ID: " + operationId);
        }
    }

    public Optional<OperationResponseDTO> getOperationById(Long id, Long functionId) throws SQLException {
        logger.info("Получение операции по ID: " + id + " для функции: " + functionId);
        Optional<Operation> operation = operationRepository.findById(id, functionId);
        return operation.map(dtoTransformService::toResponseDTO);
    }

    public boolean updateOperation(Operation operation) throws SQLException {
        logger.info("Обновление операции с ID: " + operation.getId());
        return operationRepository.updateOperation(operation);
    }

    public boolean deleteOperation(Long id, Long functionId) throws SQLException {
        logger.info("Удаление операции с ID: " + id + " для функции: " + functionId);
        return operationRepository.deleteOperation(id, functionId);
    }

    public boolean deleteAllOperations(Long functionId) throws SQLException {
        logger.info("Удаление всех операций для функции: " + functionId);
        return operationRepository.deleteAllOperations(functionId);
    }

    public Optional<Operation> getOperationEntityById(Long id, Long functionId) throws SQLException {
        return operationRepository.findById(id, functionId);
    }
}