package concurrent;

import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class MultiplyingTaskTest {

    @Test
    @DisplayName("Однопоточное выполнение: все значения функции удваиваются корректно")
    public void testSingleThreadMultiplication() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {1.0, 2.0, 3.0};
        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
        MultiplyingTask task = new MultiplyingTask(function);

        task.run();

        assertTrue(task.isCompleted(), "Флаг завершения должен быть установлен");
        assertArrayEquals(new double[]{2.0, 4.0, 6.0}, getFunctionYValues(function), 1e-10,
                "Все значения Y должны быть умножены на 2");
    }

    @Test
    @DisplayName("Многопоточное выполнение: три потока корректно удваивают значения поочерёдно (итог — умножение на 8)")
    public void testMultipleThreadsMultiplication() throws InterruptedException {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {1.0, 1.0, 1.0, 1.0}; // начальные значения
        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        MultiplyingTask task1 = new MultiplyingTask(function);
        MultiplyingTask task2 = new MultiplyingTask(function);
        MultiplyingTask task3 = new MultiplyingTask(function);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(task1);
        executor.submit(task2);
        executor.submit(task3);

        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(finished, "Пул потоков не завершил работу за отведённое время");

        double[] expected = {8.0, 8.0, 8.0, 8.0};
        assertArrayEquals(expected, getFunctionYValues(function), 1e-10,
                "После трёх последовательных удвоений каждое значение должно быть равно 8");

        assertTrue(task1.isCompleted(), "Задача 1 должна быть завершена");
        assertTrue(task2.isCompleted(), "Задача 2 должна быть завершена");
        assertTrue(task3.isCompleted(), "Задача 3 должна быть завершена");
    }

    private double[] getFunctionYValues(TabulatedFunction f) {
        double[] result = new double[f.getCount()];
        for (int i = 0; i < f.getCount(); i++) {
            result[i] = f.getY(i);
        }
        return result;
    }
}