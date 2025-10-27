package service;

import model.Function;
import model.FunctionPoint;
import repository.FunctionRepository;
import repository.FunctionPointRepository;
import java.util.List;
import java.util.logging.Logger;

public class FunctionService {
    private static final Logger logger = Logger.getLogger(FunctionService.class.getName());
    private final FunctionRepository functionRepository;
    private final FunctionPointRepository pointRepository;

    public FunctionService() {
        this.functionRepository = new FunctionRepository();
        this.pointRepository = new FunctionPointRepository();
    }

    public FunctionService(FunctionRepository functionRepository, FunctionPointRepository pointRepository) {
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    // CREATE - создание аналитической функции
    public Function createAnalyticalFunction(Long userId, String name, String expression) {
        validateFunctionName(name);
        validateExpression(expression);

        try {
            Function function = new Function(userId, name, "analytical", "base", expression);
            Long functionId = functionRepository.createFunction(function);

            Function createdFunction = functionRepository.findById(functionId);
            logger.info("Analytical function created: " + name + " for user: " + userId);
            return createdFunction;

        } catch (Exception e) {
            logger.severe("Error creating analytical function: " + e.getMessage());
            throw new RuntimeException("Failed to create function", e);
        }
    }

    // CREATE - создание табулированной функции
    public Function createTabulatedFunction(Long userId, String name, Long parentFunctionId) {
        validateFunctionName(name);

        try {
            Function function = new Function(userId, name, "tabulated", "derived", null);
            function.setParentFunctionId(parentFunctionId);

            Long functionId = functionRepository.createFunction(function);
            Function createdFunction = functionRepository.findById(functionId);

            logger.info("Tabulated function created: " + name + " for user: " + userId);
            return createdFunction;

        } catch (Exception e) {
            logger.severe("Error creating tabulated function: " + e.getMessage());
            throw new RuntimeException("Failed to create function", e);
        }
    }

    // CREATE - создание составной функции
    public Function createCompositeFunction(Long userId, String name, String expression, Long parentFunctionId) {
        validateFunctionName(name);
        validateExpression(expression);

        try {
            Function function = new Function(userId, name, "analytical", "composite", expression);
            function.setParentFunctionId(parentFunctionId);

            Long functionId = functionRepository.createFunction(function);
            Function createdFunction = functionRepository.findById(functionId);

            logger.info("Composite function created: " + name + " for user: " + userId);
            return createdFunction;

        } catch (Exception e) {
            logger.severe("Error creating composite function: " + e.getMessage());
            throw new RuntimeException("Failed to create function", e);
        }
    }

    // READ - получение функции по ID
    public Function getFunctionById(Long functionId) {
        validateFunctionId(functionId);

        try {
            Function function = functionRepository.findById(functionId);
            if (function == null) {
                logger.warning("Function not found with ID: " + functionId);
            }
            return function;
        } catch (Exception e) {
            logger.severe("Error getting function by ID: " + e.getMessage());
            throw new RuntimeException("Failed to get function", e);
        }
    }

    // READ - функции пользователя
    public List<Function> getUserFunctions(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        try {
            List<Function> functions = functionRepository.findByUserId(userId);
            logger.info("Retrieved " + functions.size() + " functions for user: " + userId);
            return functions;
        } catch (Exception e) {
            logger.severe("Error getting user functions: " + e.getMessage());
            throw new RuntimeException("Failed to get user functions", e);
        }
    }


    // UPDATE - обновление функции
    public boolean updateFunction(Function function) {
        validateFunction(function);

        try {
            boolean success = functionRepository.updateFunction(function);
            if (success) {
                logger.info("Function updated successfully: " + function.getId());
            }
            return success;
        } catch (Exception e) {
            logger.severe("Error updating function: " + e.getMessage());
            throw new RuntimeException("Failed to update function", e);
        }
    }

    // UPDATE - обновление выражения функции
    public boolean updateFunctionExpression(Long functionId, String newExpression) {
        validateFunctionId(functionId);
        validateExpression(newExpression);

        try {
            Function function = functionRepository.findById(functionId);
            if (function == null) {
                throw new IllegalArgumentException("Function not found with ID: " + functionId);
            }

            if (!"analytical".equals(function.getDataFormat())) {
                throw new IllegalArgumentException("Only analytical functions can have expressions");
            }

            function.setExpression(newExpression);
            return functionRepository.updateFunction(function);

        } catch (Exception e) {
            logger.severe("Error updating function expression: " + e.getMessage());
            throw new RuntimeException("Failed to update function expression", e);
        }
    }

    // DELETE - удаление функции
    public boolean deleteFunction(Long functionId) {
        validateFunctionId(functionId);

        try {
            // Сначала удаляем связанные точки (если есть)
            pointRepository.deleteByFunctionId(functionId);

            boolean success = functionRepository.deleteFunction(functionId);
            if (success) {
                logger.info("Function deleted successfully: " + functionId);
            }
            return success;
        } catch (Exception e) {
            logger.severe("Error deleting function: " + e.getMessage());
            throw new RuntimeException("Failed to delete function", e);
        }
    }

    // Управление точками функций
    public void addFunctionPoint(Long functionId, double x, double y) {
        validateFunctionId(functionId);

        try {
            FunctionPoint point = new FunctionPoint(functionId, x, y);
            pointRepository.createFunctionPoint(point);
            logger.fine("Point added to function " + functionId + ": x=" + x + ", y=" + y);
        } catch (Exception e) {
            logger.severe("Error adding function point: " + e.getMessage());
            throw new RuntimeException("Failed to add function point", e);
        }
    }

    public List<FunctionPoint> getFunctionPoints(Long functionId) {
        validateFunctionId(functionId);

        try {
            return pointRepository.findAllByFunctionId(functionId);
        } catch (Exception e) {
            logger.severe("Error getting function points: " + e.getMessage());
            throw new RuntimeException("Failed to get function points", e);
        }
    }

    public void clearFunctionPoints(Long functionId) {
        validateFunctionId(functionId);

        try {
            pointRepository.deleteByFunctionId(functionId);
            logger.info("Function points cleared for function: " + functionId);
        } catch (Exception e) {
            logger.severe("Error clearing function points: " + e.getMessage());
            throw new RuntimeException("Failed to clear function points", e);
        }
    }

    // Бизнес-логика
    public boolean isAnalyticalFunction(Function function) {
        return function != null && "analytical".equals(function.getDataFormat());
    }

    public boolean isTabulatedFunction(Function function) {
        return function != null && "tabulated".equals(function.getDataFormat());
    }

    public List<Function> getDerivedFunctions(Long parentFunctionId) {
        try {
            List<Function> allFunctions = functionRepository.findByUserId(getFunctionById(parentFunctionId).getUserId());
            return allFunctions.stream()
                    .filter(f -> parentFunctionId.equals(f.getParentFunctionId()))
                    .toList();
        } catch (Exception e) {
            logger.severe("Error getting derived functions: " + e.getMessage());
            throw new RuntimeException("Failed to get derived functions", e);
        }
    }

    // Валидация
    private void validateFunction(Function function) {
        if (function == null) {
            throw new IllegalArgumentException("Function cannot be null");
        }
        validateFunctionName(function.getName());
        validateFunctionId(function.getId());
    }

    private void validateFunctionId(Long functionId) {
        if (functionId == null || functionId <= 0) {
            throw new IllegalArgumentException("Invalid function ID");
        }
    }

    private void validateFunctionName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Function name too long");
        }
    }

    private void validateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Function expression cannot be empty");
        }
        // Можно добавить более сложную валидацию математических выражений
    }

    // Статистика
    public int getUserFunctionCount(Long userId) {
        try {
            return functionRepository.findByUserId(userId).size();
        } catch (Exception e) {
            logger.severe("Error getting user function count: " + e.getMessage());
            return 0;
        }
    }

    public int getFunctionPointCount(Long functionId) {
        try {
            return pointRepository.findAllByFunctionId(functionId).size();
        } catch (Exception e) {
            logger.severe("Error getting function point count: " + e.getMessage());
            return 0;
        }
    }
}