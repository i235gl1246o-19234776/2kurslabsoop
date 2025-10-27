package service;

import model.FunctionOperation;
import model.OperationType;
import repository.FunctionOperationRepository;
import repository.FunctionRepository;
import repository.OperationTypeRepository;
import java.util.List;
import java.util.logging.Logger;

public class FunctionOperationService {
    private static final Logger logger = Logger.getLogger(FunctionOperationService.class.getName());
    private final FunctionOperationRepository operationRepository;
    private final OperationTypeRepository typeRepository;
    private final FunctionRepository functionRepository;

    public FunctionOperationService() {
        this.operationRepository = new FunctionOperationRepository();
        this.typeRepository = new OperationTypeRepository();
        this.functionRepository = new FunctionRepository();
    }

    public FunctionOperationService(FunctionOperationRepository operationRepository,
                                    OperationTypeRepository typeRepository,
                                    FunctionRepository functionRepository) {
        this.operationRepository = operationRepository;
        this.typeRepository = typeRepository;
        this.functionRepository = functionRepository;
    }

    // CREATE - создание операции с числовым результатом
    public FunctionOperation createNumericOperation(Long functionId, Integer operationTypeId,
                                                    String parameters, Double resultValue) {
        validateOperationParameters(functionId, operationTypeId, parameters);

        try {
            // Проверяем существование функции
            if (functionRepository.findById(functionId) == null) {
                throw new IllegalArgumentException("Function not found with ID: " + functionId);
            }

            // Проверяем тип операции
            OperationType operationType = typeRepository.findById(operationTypeId);
            if (operationType == null) {
                throw new IllegalArgumentException("Operation type not found with ID: " + operationTypeId);
            }

            if (!operationType.isNumericResult()) {
                throw new IllegalArgumentException("Operation type does not produce numeric result");
            }

            FunctionOperation operation = new FunctionOperation(functionId, operationTypeId, parameters);
            operation.setResultValue(resultValue);

            Long operationId = operationRepository.createFunctionOperation(operation);
            FunctionOperation createdOperation = operationRepository.findById(operationId);

            logger.info("Numeric operation created: " + operationType.getName() +
                    " on function: " + functionId + " with result: " + resultValue);
            return createdOperation;

        } catch (Exception e) {
            logger.severe("Error creating numeric operation: " + e.getMessage());
            throw new RuntimeException("Failed to create numeric operation", e);
        }
    }

    // CREATE - создание операции с функциональным результатом
    public FunctionOperation createFunctionalOperation(Long functionId, Integer operationTypeId,
                                                       String parameters, Long resultFunctionId) {
        validateOperationParameters(functionId, operationTypeId, parameters);

        try {
            // Проверяем существование исходной функции
            if (functionRepository.findById(functionId) == null) {
                throw new IllegalArgumentException("Source function not found with ID: " + functionId);
            }

            // Проверяем существование результирующей функции
            if (functionRepository.findById(resultFunctionId) == null) {
                throw new IllegalArgumentException("Result function not found with ID: " + resultFunctionId);
            }

            // Проверяем тип операции
            OperationType operationType = typeRepository.findById(operationTypeId);
            if (operationType == null) {
                throw new IllegalArgumentException("Operation type not found with ID: " + operationTypeId);
            }

            if (!operationType.isFunctionResult()) {
                throw new IllegalArgumentException("Operation type does not produce function result");
            }

            FunctionOperation operation = new FunctionOperation(functionId, operationTypeId, parameters);
            operation.setResultFunctionId(resultFunctionId);

            Long operationId = operationRepository.createFunctionOperation(operation);
            FunctionOperation createdOperation = operationRepository.findById(operationId);

            logger.info("Functional operation created: " + operationType.getName() +
                    " on function: " + functionId + " producing function: " + resultFunctionId);
            return createdOperation;

        } catch (Exception e) {
            logger.severe("Error creating functional operation: " + e.getMessage());
            throw new RuntimeException("Failed to create functional operation", e);
        }
    }

    // READ - получение операции по ID
    public FunctionOperation getOperationById(Long operationId) {
        validateOperationId(operationId);

        try {
            FunctionOperation operation = operationRepository.findById(operationId);
            if (operation != null) {
                // Загружаем дополнительную информацию
                enrichOperationWithDetails(operation);
            }
            return operation;
        } catch (Exception e) {
            logger.severe("Error getting operation by ID: " + e.getMessage());
            throw new RuntimeException("Failed to get operation", e);
        }
    }

    // READ - операции функции
    public List<FunctionOperation> getOperationsByFunctionId(Long functionId) {
        validateFunctionId(functionId);

        try {
            List<FunctionOperation> operations = operationRepository.findByFunctionId(functionId);

            // Загружаем дополнительную информацию для каждой операции
            for (FunctionOperation operation : operations) {
                enrichOperationWithDetails(operation);
            }

            logger.info("Retrieved " + operations.size() + " operations for function: " + functionId);
            return operations;
        } catch (Exception e) {
            logger.severe("Error getting operations by function ID: " + e.getMessage());
            throw new RuntimeException("Failed to get operations", e);
        }
    }

    // READ - операции по результирующей функции
    public List<FunctionOperation> getOperationsByResultFunctionId(Long resultFunctionId) {
        validateFunctionId(resultFunctionId);

        try {
            List<FunctionOperation> operations = operationRepository.findByResultFunctionId(resultFunctionId);

            for (FunctionOperation operation : operations) {
                enrichOperationWithDetails(operation);
            }

            return operations;
        } catch (Exception e) {
            logger.severe("Error getting operations by result function ID: " + e.getMessage());
            throw new RuntimeException("Failed to get operations", e);
        }
    }



    // UPDATE - обновление результата операции
    public boolean updateOperationResult(Long operationId, Double resultValue, Long resultFunctionId) {
        validateOperationId(operationId);

        try {
            FunctionOperation operation = operationRepository.findById(operationId);
            if (operation == null) {
                throw new IllegalArgumentException("Operation not found with ID: " + operationId);
            }

            operation.setResultValue(resultValue);
            operation.setResultFunctionId(resultFunctionId);

            boolean success = operationRepository.updateResult(operation);
            if (success) {
                logger.info("Operation result updated: " + operationId);
            }
            return success;
        } catch (Exception e) {
            logger.severe("Error updating operation result: " + e.getMessage());
            throw new RuntimeException("Failed to update operation result", e);
        }
    }

    // UPDATE - обновление только числового результата
    public boolean updateNumericResult(Long operationId, Double resultValue) {
        return updateOperationResult(operationId, resultValue, null);
    }

    // UPDATE - обновление только функционального результата
    public boolean updateFunctionalResult(Long operationId, Long resultFunctionId) {
        return updateOperationResult(operationId, null, resultFunctionId);
    }

    // DELETE - удаление операции
    public boolean deleteOperation(Long operationId) {
        validateOperationId(operationId);

        try {
            boolean success = operationRepository.deleteFunctionOperation(operationId);
            if (success) {
                logger.info("Operation deleted: " + operationId);
            }
            return success;
        } catch (Exception e) {
            logger.severe("Error deleting operation: " + e.getMessage());
            throw new RuntimeException("Failed to delete operation", e);
        }
    }


    // Вспомогательный метод для обогащения операции деталями
    private void enrichOperationWithDetails(FunctionOperation operation) {
        try {
            // Загружаем тип операции
            OperationType opType = typeRepository.findById(operation.getOperationTypeId());
            operation.setOperationType(opType);

            // Загружаем исходную функцию (опционально)
            if (operation.getFunctionId() != null) {
                operation.setOriginalFunction(functionRepository.findById(operation.getFunctionId()));
            }

            // Загружаем результирующую функцию (опционально)
            if (operation.getResultFunctionId() != null) {
                operation.setResultFunction(functionRepository.findById(operation.getResultFunctionId()));
            }
        } catch (Exception e) {
            logger.warning("Error enriching operation with details: " + e.getMessage());
        }
    }

    // Валидация
    private void validateOperationId(Long operationId) {
        if (operationId == null || operationId <= 0) {
            throw new IllegalArgumentException("Invalid operation ID");
        }
    }

    private void validateFunctionId(Long functionId) {
        if (functionId == null || functionId <= 0) {
            throw new IllegalArgumentException("Invalid function ID");
        }
    }

    private void validateOperationParameters(Long functionId, Integer operationTypeId, String parameters) {
        validateFunctionId(functionId);

        if (operationTypeId == null || operationTypeId <= 0) {
            throw new IllegalArgumentException("Invalid operation type ID");
        }

        if (parameters == null || parameters.trim().isEmpty()) {
            throw new IllegalArgumentException("Operation parameters cannot be empty");
        }
    }


    public int getFunctionOperationCount(Long functionId) {
        try {
            return operationRepository.findByFunctionId(functionId).size();
        } catch (Exception e) {
            logger.severe("Error getting function operation count: " + e.getMessage());
            return 0;
        }
    }

    public boolean operationExists(Long operationId) {
        try {
            return operationRepository.findById(operationId) != null;
        } catch (Exception e) {
            logger.severe("Error checking operation existence: " + e.getMessage());
            return false;
        }
    }
}