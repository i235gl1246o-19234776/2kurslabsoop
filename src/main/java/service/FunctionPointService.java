package service;

import model.FunctionPoint;
import repository.FunctionPointRepository;
import repository.FunctionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

public class FunctionPointService {
    private static final Logger logger = Logger.getLogger(FunctionPointService.class.getName());
    private final FunctionPointRepository pointRepository;
    private final FunctionRepository functionRepository;

    public FunctionPointService() {
        this.pointRepository = new FunctionPointRepository();
        this.functionRepository = new FunctionRepository();
    }

    public FunctionPointService(FunctionPointRepository pointRepository, FunctionRepository functionRepository) {
        this.pointRepository = pointRepository;
        this.functionRepository = functionRepository;
    }

    // CREATE - добавление точки функции
    public FunctionPoint addFunctionPoint(Long functionId, double x, double y) {
        validateFunctionId(functionId);
        validateCoordinates(x, y);

        try {
            // Проверяем существование функции
            if (functionRepository.findById(functionId) == null) {
                throw new IllegalArgumentException("Function not found with ID: " + functionId);
            }

            FunctionPoint point = new FunctionPoint(functionId, x, y);
            Long pointId = pointRepository.createFunctionPoint(point);
            FunctionPoint createdPoint = pointRepository.findOne(functionId, BigDecimal.valueOf(x));

            logger.info("Function point added: function=" + functionId + ", x=" + x + ", y=" + y);
            return createdPoint;

        } catch (Exception e) {
            logger.severe("Error adding function point: " + e.getMessage());
            throw new RuntimeException("Failed to add function point", e);
        }
    }

    // CREATE - массовое добавление точек
    public void addFunctionPoints(Long functionId, List<FunctionPoint> points) {
        validateFunctionId(functionId);

        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be null or empty");
        }

        try {
            // Проверяем существование функции
            if (functionRepository.findById(functionId) == null) {
                throw new IllegalArgumentException("Function not found with ID: " + functionId);
            }

            // Валидируем все точки
            for (FunctionPoint point : points) {
                validateCoordinates(point.getXValueAsDouble(), point.getYVal());
            }

            pointRepository.createBatchPoints(functionId, points);
            logger.info("Batch added " + points.size() + " points to function: " + functionId);

        } catch (Exception e) {
            logger.severe("Error adding batch function points: " + e.getMessage());
            throw new RuntimeException("Failed to add batch function points", e);
        }
    }

    // READ - получение всех точек функции
    public List<FunctionPoint> getFunctionPoints(Long functionId) {
        validateFunctionId(functionId);

        try {
            List<FunctionPoint> points = pointRepository.findAllByFunctionId(functionId);
            logger.info("Retrieved " + points.size() + " points for function: " + functionId);
            return points;
        } catch (Exception e) {
            logger.severe("Error getting function points: " + e.getMessage());
            throw new RuntimeException("Failed to get function points", e);
        }
    }

    // READ - получение конкретной точки
    public FunctionPoint getFunctionPoint(Long functionId, double x) {
        validateFunctionId(functionId);
        validateXCoordinate(x);

        try {
            return pointRepository.findOne(functionId, BigDecimal.valueOf(x));
        } catch (Exception e) {
            logger.severe("Error getting function point: " + e.getMessage());
            throw new RuntimeException("Failed to get function point", e);
        }
    }

    // READ - проверка существования точки
    public boolean pointExists(Long functionId, double x) {
        validateFunctionId(functionId);
        validateXCoordinate(x);

        try {
            return pointRepository.findOne(functionId, BigDecimal.valueOf(x)) != null;
        } catch (Exception e) {
            logger.severe("Error checking point existence: " + e.getMessage());
            return false;
        }
    }

    // UPDATE - обновление значения Y в точке
    public boolean updatePointValue(Long functionId, double x, double newY) {
        validateFunctionId(functionId);
        validateCoordinates(x, newY);

        try {
            // Создаем новую точку (ON CONFLICT обновит существующую)
            FunctionPoint point = new FunctionPoint(functionId, x, newY);
            pointRepository.createFunctionPoint(point);

            logger.info("Function point updated: function=" + functionId + ", x=" + x + ", newY=" + newY);
            return true;

        } catch (Exception e) {
            logger.severe("Error updating function point: " + e.getMessage());
            throw new RuntimeException("Failed to update function point", e);
        }
    }

    // DELETE - удаление всех точек функции
    public boolean clearFunctionPoints(Long functionId) {
        validateFunctionId(functionId);

        try {
            boolean success = pointRepository.deleteByFunctionId(functionId);
            if (success) {
                logger.info("All function points cleared for function: " + functionId);
            }
            return success;
        } catch (Exception e) {
            logger.severe("Error clearing function points: " + e.getMessage());
            throw new RuntimeException("Failed to clear function points", e);
        }
    }

    // DELETE - удаление конкретной точки
    public boolean deleteFunctionPoint(Long functionId, double x) {
        validateFunctionId(functionId);
        validateXCoordinate(x);

        try {
            // В нашей текущей реализации нет отдельного метода удаления точки
            // Можно реализовать через создание "пустой" точки или добавить метод в репозиторий
            logger.warning("Individual point deletion not implemented for function: " + functionId + ", x: " + x);
            return false;
        } catch (Exception e) {
            logger.severe("Error deleting function point: " + e.getMessage());
            throw new RuntimeException("Failed to delete function point", e);
        }
    }

    // Бизнес-логика: получение диапазона X значений
    public double[] getXRange(Long functionId) {
        validateFunctionId(functionId);

        try {
            List<FunctionPoint> points = getFunctionPoints(functionId);
            if (points.isEmpty()) {
                return new double[]{0, 0};
            }

            double minX = points.stream()
                    .mapToDouble(FunctionPoint::getXValueAsDouble)
                    .min()
                    .orElse(0);

            double maxX = points.stream()
                    .mapToDouble(FunctionPoint::getXValueAsDouble)
                    .max()
                    .orElse(0);

            return new double[]{minX, maxX};
        } catch (Exception e) {
            logger.severe("Error getting X range: " + e.getMessage());
            throw new RuntimeException("Failed to get X range", e);
        }
    }

    // Бизнес-логика: получение диапазона Y значений
    public double[] getYRange(Long functionId) {
        validateFunctionId(functionId);

        try {
            List<FunctionPoint> points = getFunctionPoints(functionId);
            if (points.isEmpty()) {
                return new double[]{0, 0};
            }

            double minY = points.stream()
                    .mapToDouble(FunctionPoint::getYVal)
                    .min()
                    .orElse(0);

            double maxY = points.stream()
                    .mapToDouble(FunctionPoint::getYVal)
                    .max()
                    .orElse(0);

            return new double[]{minY, maxY};
        } catch (Exception e) {
            logger.severe("Error getting Y range: " + e.getMessage());
            throw new RuntimeException("Failed to get Y range", e);
        }
    }

    // Бизнес-логика: интерполяция значения
    public Double interpolateValue(Long functionId, double x) {
        validateFunctionId(functionId);
        validateXCoordinate(x);

        try {
            List<FunctionPoint> points = getFunctionPoints(functionId);
            if (points.isEmpty()) {
                return null;
            }

            // Простая линейная интерполяция
            for (int i = 0; i < points.size() - 1; i++) {
                FunctionPoint p1 = points.get(i);
                FunctionPoint p2 = points.get(i + 1);

                double x1 = p1.getXValueAsDouble();
                double x2 = p2.getXValueAsDouble();
                double y1 = p1.getYVal();
                double y2 = p2.getYVal();

                if (x >= x1 && x <= x2) {
                    // Линейная интерполяция: y = y1 + (y2 - y1) * (x - x1) / (x2 - x1)
                    return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
                }
            }

            return null; // x вне диапазона
        } catch (Exception e) {
            logger.severe("Error interpolating value: " + e.getMessage());
            throw new RuntimeException("Failed to interpolate value", e);
        }
    }

    // Валидация
    private void validateFunctionId(Long functionId) {
        if (functionId == null || functionId <= 0) {
            throw new IllegalArgumentException("Invalid function ID");
        }
    }

    private void validateCoordinates(double x, double y) {
        validateXCoordinate(x);

        if (Double.isNaN(y) || Double.isInfinite(y)) {
            throw new IllegalArgumentException("Y coordinate must be a finite number");
        }
    }

    private void validateXCoordinate(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            throw new IllegalArgumentException("X coordinate must be a finite number");
        }
    }

    // Статистика
    public int getPointCount(Long functionId) {
        try {
            return pointRepository.findAllByFunctionId(functionId).size();
        } catch (Exception e) {
            logger.severe("Error getting point count: " + e.getMessage());
            return 0;
        }
    }

    public boolean hasPoints(Long functionId) {
        try {
            return !pointRepository.findAllByFunctionId(functionId).isEmpty();
        } catch (Exception e) {
            logger.severe("Error checking if function has points: " + e.getMessage());
            return false;
        }
    }
}