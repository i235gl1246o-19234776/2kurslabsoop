package operations;

import functions.TabulatedFunction;
import functions.Point;
import java.util.Iterator;
import functions.factory.*;
import exception.*;

public class TabulatedFunctionOperationService {

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            throw new IllegalArgumentException("TabulatedFunction cannot be null");
        }

        int count = tabulatedFunction.getCount();
        Point[] points = new Point[count];

        int i = 0;
        Iterator<Point> iterator = tabulatedFunction.iterator();
        while (iterator.hasNext()) {
            Point originalPoint = iterator.next();
            points[i] = new Point(originalPoint.x, originalPoint.y);
            i++;
        }

        return points;
    }

    /**
     * Валидация точек на наличие недопустимых значений (NaN, Infinity)
     * @param points массив точек для проверки
     * @param functionName имя функции для логирования ошибок
     */
    private void validatePoints(Point[] points, String functionName) {
        for (int i = 0; i < points.length; i++) {
            Point p = points[i];

            // Проверка X-координаты
            if (Double.isNaN(p.x) || !Double.isFinite(p.x)) {
                throw new IllegalArgumentException(
                        String.format("Функция '%s' содержит недопустимое значение X (NaN/Infinity) в точке %d: x=%f",
                                functionName, i, p.x)
                );
            }

            // Проверка Y-координаты
            if (Double.isNaN(p.y) || !Double.isFinite(p.y)) {
                throw new IllegalArgumentException(
                        String.format("Функция '%s' содержит недопустимое значение Y (NaN/Infinity) в точке %d: y=%f",
                                functionName, i, p.y)
                );
            }
        }
    }

    private TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }
        this.factory = factory;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }
        this.factory = factory;
    }

    private TabulatedFunction doOperation(
            TabulatedFunction a,
            TabulatedFunction b,
            String operationType // Передаём тип операции
    ) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("TabulatedFunction cannot be null");
        }

        int countA = a.getCount();
        int countB = b.getCount();

        if (countA != countB) {
            throw new InconsistentFunctionsException(
                    "Размеры не совпадают: " + countA + " и " + countB);
        }

        Point[] pointsA = asPoints(a);
        Point[] pointsB = asPoints(b);

        // Добавляем валидацию точек ПЕРЕД выполнением операции
        validatePoints(pointsA, "operand1");
        validatePoints(pointsB, "operand2");

        double[] xValues = new double[countA];
        double[] yValues = new double[countA];

        for (int i = 0; i < countA; i++) {
            double xA = pointsA[i].x;
            double xB = pointsB[i].x;

            if (Math.abs(xA - xB) > 1e-9) { // Используем точное сравнение с допуском
                throw new InconsistentFunctionsException(
                        String.format("X-значения не совпадают в точке %d: %f != %f", i, xA, xB)
                );
            }

            double yA = pointsA[i].y;
            double yB = pointsB[i].y;

            // Логирование для отладки
            System.out.printf("doOperation[%d]: x=%.4f, yA=%.4f, yB=%.4f, operation=%s%n",
                    i, xA, yA, yB, operationType);

            xValues[i] = xA;

            // Выполняем операцию в зависимости от типа
            switch (operationType) {
                case "add":
                    yValues[i] = yA + yB;
                    break;
                case "subtract":
                    yValues[i] = yA - yB;
                    break;
                case "multiply":
                    yValues[i] = yA * yB;
                    break;
                case "divide":
                    // Проверка деления на очень маленькое число
                    if (Math.abs(yB) < 1e-10) {
                        throw new ArithmeticException(
                                String.format("Деление на значение, близкое к нулю, невозможно в точке %d: yB=%.10f",
                                        i, yB)
                        );
                    }
                    yValues[i] = yA / yB;
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестная операция: " + operationType);
            }

            // Проверка результата на допустимость
            if (Double.isNaN(yValues[i]) || !Double.isFinite(yValues[i])) {
                throw new ArithmeticException(
                        String.format("Операция '%s' привела к недопустимому результату в точке %d: y=%.10f",
                                operationType, i, yValues[i])
                );
            }
        }

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, "add");
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, "subtract");
    }

    public TabulatedFunction multiply(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, "multiply");
    }

    public TabulatedFunction divide(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, "divide");
    }

    /**
     * Создает копию функции с использованием текущей фабрики
     * @param function исходная функция
     * @return копия функции
     */
    public TabulatedFunction copy(TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Function cannot be null");
        }

        Point[] points = asPoints(function);
        double[] xValues = new double[points.length];
        double[] yValues = new double[points.length];

        for (int i = 0; i < points.length; i++) {
            xValues[i] = points[i].x;
            yValues[i] = points[i].y;
        }

        return factory.create(xValues, yValues);
    }
}