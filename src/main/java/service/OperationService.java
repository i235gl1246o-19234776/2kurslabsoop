    package service;

    import model.FunctionOperation;
    import model.OperationType;
    import repository.FunctionOperationRepository;
    import repository.OperationTypeRepository;
    import java.util.List;
    import java.util.logging.Logger;

    public class OperationService {
        private static final Logger logger = Logger.getLogger(OperationService.class.getName());
        private final FunctionOperationRepository operationRepository;
        private final OperationTypeRepository typeRepository;

        public OperationService() {
            this.operationRepository = new FunctionOperationRepository();
            this.typeRepository = new OperationTypeRepository();
        }

        public OperationService(FunctionOperationRepository operationRepository, OperationTypeRepository typeRepository) {
            this.operationRepository = operationRepository;
            this.typeRepository = typeRepository;
        }

        // Инициализация системы
        public void initializeSystem() {
            try {
                typeRepository.initializePredefinedTypes();
                logger.info("Operation system initialized successfully");
            } catch (Exception e) {
                logger.severe("Error initializing operation system: " + e.getMessage());
                throw new RuntimeException("System initialization failed", e);
            }
        }

        // CREATE - выполнение операции с числовым результатом
        public FunctionOperation executeNumericOperation(Long functionId, OperationType operationType,
                                                         String parameters, Double result) {
            validateOperationParameters(functionId, operationType, parameters);

            try {
                if (!operationType.isNumericResult()) {
                    throw new IllegalArgumentException("Operation type does not produce numeric result");
                }

                FunctionOperation operation = new FunctionOperation(functionId, operationType, parameters);
                operation.setResultValue(result);

                Long operationId = operationRepository.createFunctionOperation(operation);
                FunctionOperation createdOperation = operationRepository.findById(operationId);

                logger.info("Numeric operation executed: " + operationType.getName() +
                        " on function: " + functionId + " with result: " + result);
                return createdOperation;

            } catch (Exception e) {
                logger.severe("Error executing numeric operation: " + e.getMessage());
                throw new RuntimeException("Operation execution failed", e);
            }
        }

        // CREATE - выполнение операции с функциональным результатом
        public FunctionOperation executeFunctionalOperation(Long functionId, OperationType operationType,
                                                            String parameters, Long resultFunctionId) {
            validateOperationParameters(functionId, operationType, parameters);

            try {
                if (!operationType.isFunctionResult()) {
                    throw new IllegalArgumentException("Operation type does not produce function result");
                }

                FunctionOperation operation = new FunctionOperation(functionId, operationType, parameters);
                operation.setResultFunctionId(resultFunctionId);

                Long operationId = operationRepository.createFunctionOperation(operation);
                FunctionOperation createdOperation = operationRepository.findById(operationId);

                logger.info("Functional operation executed: " + operationType.getName() +
                        " on function: " + functionId + " producing function: " + resultFunctionId);
                return createdOperation;

            } catch (Exception e) {
                logger.severe("Error executing functional operation: " + e.getMessage());
                throw new RuntimeException("Operation execution failed", e);
            }
        }

        // READ - получение операции по ID
        public FunctionOperation getOperationById(Long operationId) {
            validateOperationId(operationId);

            try {
                FunctionOperation operation = operationRepository.findById(operationId);
                if (operation != null) {
                    // Загружаем тип операции для удобства
                    OperationType opType = typeRepository.findById(operation.getOperationTypeId());
                    operation.setOperationType(opType);
                }
                return operation;
            } catch (Exception e) {
                logger.severe("Error getting operation by ID: " + e.getMessage());
                throw new RuntimeException("Failed to get operation", e);
            }
        }

        // READ - операции функции
        public List<FunctionOperation> getFunctionOperations(Long functionId) {
            validateFunctionId(functionId);

            try {
                List<FunctionOperation> operations = operationRepository.findByFunctionId(functionId);

                // Загружаем типы операций для каждого результата
                for (FunctionOperation operation : operations) {
                    OperationType opType = typeRepository.findById(operation.getOperationTypeId());
                    operation.setOperationType(opType);
                }

                logger.info("Retrieved " + operations.size() + " operations for function: " + functionId);
                return operations;
            } catch (Exception e) {
                logger.severe("Error getting function operations: " + e.getMessage());
                throw new RuntimeException("Failed to get function operations", e);
            }
        }

        // READ - операции по результирующей функции
        public List<FunctionOperation> getOperationsByResultFunction(Long resultFunctionId) {
            validateFunctionId(resultFunctionId);

            try {
                return operationRepository.findByResultFunctionId(resultFunctionId);
            } catch (Exception e) {
                logger.severe("Error getting operations by result function: " + e.getMessage());
                throw new RuntimeException("Failed to get operations", e);
            }
        }

        // READ - все типы операций
        public List<OperationType> getAllOperationTypes() {
            try {
                return typeRepository.findAll();
            } catch (Exception e) {
                logger.severe("Error getting operation types: " + e.getMessage());
                throw new RuntimeException("Failed to get operation types", e);
            }
        }

        // READ - тип операции по ID
        public OperationType getOperationTypeById(Integer typeId) {
            if (typeId == null || typeId <= 0) {
                throw new IllegalArgumentException("Invalid operation type ID");
            }

            try {
                OperationType opType = typeRepository.findById(typeId);
                if (opType == null) {
                    logger.warning("Operation type not found with ID: " + typeId);
                }
                return opType;
            } catch (Exception e) {
                logger.severe("Error getting operation type: " + e.getMessage());
                throw new RuntimeException("Failed to get operation type", e);
            }
        }

        // UPDATE - обновление результата операции
        public boolean updateOperationResult(FunctionOperation operation) {
            validateOperation(operation);

            try {
                boolean success = operationRepository.updateResult(operation);
                if (success) {
                    logger.info("Operation result updated: " + operation.getId());
                }
                return success;
            } catch (Exception e) {
                logger.severe("Error updating operation result: " + e.getMessage());
                throw new RuntimeException("Failed to update operation result", e);
            }
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

        // Бизнес-логика
        public List<FunctionOperation> getRecentOperations(int limit) {
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be positive");
            }

            try {
                // Получаем все операции и сортируем по дате выполнения
                // В реальном приложении добавьте метод в репозиторий для эффективной выборки
                List<FunctionOperation> allOperations = getAllOperations();
                return allOperations.stream()
                        .sorted((o1, o2) -> o2.getExecutedAt().compareTo(o1.getExecutedAt()))
                        .limit(limit)
                        .toList();
            } catch (Exception e) {
                logger.severe("Error getting recent operations: " + e.getMessage());
                throw new RuntimeException("Failed to get recent operations", e);
            }
        }

        public List<FunctionOperation> getOperationsByType(Integer operationTypeId) {
            try {
                List<FunctionOperation> allOperations = getAllOperations();
                return allOperations.stream()
                        .filter(op -> operationTypeId.equals(op.getOperationTypeId()))
                        .toList();
            } catch (Exception e) {
                logger.severe("Error getting operations by type: " + e.getMessage());
                throw new RuntimeException("Failed to get operations by type", e);
            }
        }

        // Вспомогательные методы
        private List<FunctionOperation> getAllOperations() {
            // В реальном приложении добавьте метод в репозиторий
            // Временная реализация через агрегацию по всем функциям
            try {
                // Это упрощенная реализация - в реальном приложении
                // добавьте метод getAllOperations в репозиторий
                return List.of(); // Заглушка
            } catch (Exception e) {
                throw new RuntimeException("Failed to get all operations", e);
            }
        }

        // Валидация
        private void validateOperation(FunctionOperation operation) {
            if (operation == null) {
                throw new IllegalArgumentException("Operation cannot be null");
            }
            validateOperationId(operation.getId());
        }

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

        private void validateOperationParameters(Long functionId, OperationType operationType, String parameters) {
            validateFunctionId(functionId);

            if (operationType == null) {
                throw new IllegalArgumentException("Operation type cannot be null");
            }

            if (parameters == null || parameters.trim().isEmpty()) {
                throw new IllegalArgumentException("Operation parameters cannot be empty");
            }
        }

        // Статистика
        public int getOperationCount() {
            try {
                // Временная реализация
                return getRecentOperations(1000).size();
            } catch (Exception e) {
                logger.severe("Error getting operation count: " + e.getMessage());
                return 0;
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
    }