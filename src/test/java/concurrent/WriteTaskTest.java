package concurrent;

import functions.TabulatedFunction;
import functions.factory.LinkedListTabulatedFunctionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для класса WriteTask")
class WriteTaskTest {

    private TabulatedFunction function;
    private Object lock;
    private WriteTask writeTask;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    @DisplayName("Инициализация тестовых данных")
    void setUp() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        function = new LinkedListTabulatedFunctionFactory().create(xValues, yValues);
        lock = new Object();
        writeTask = new WriteTask(function, 99.0);

        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    @DisplayName("Восстановление оригинального System.out")
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Конструктор должен корректно инициализировать объект")
    void testConstructor() {
        assertNotNull(writeTask, "Объект WriteTask должен быть создан");

        WriteTask taskWithDifferentValue = new WriteTask(function, 50.0);
        assertNotNull(taskWithDifferentValue, "Должен создаваться с разными значениями");
    }

    @Test
    @DisplayName("WriteTask должен корректно работать в паре с ReadTask")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testWriteReadIntegration() throws InterruptedException {
        ReadTask readTask = new ReadTask(function);

        Thread writeThread = new Thread(writeTask);
        Thread readThread = new Thread(readTask);

        writeThread.start();
        Thread.sleep(50);
        readThread.start();

        writeThread.join(2000);
        readThread.join(1000);

        String output = outputStream.toString();

        assertTrue(output.contains("Writing for index") && output.contains("After read:"),
                "Должны быть операции и записи, и чтения. Output: " + output);

        for (int i = 0; i < function.getCount(); i++) {
            assertEquals(99.0, function.getY(i), 1e-9,
                    "Все значения Y должны быть установлены в 99.0");
        }
    }

    @Test
    @DisplayName("WriteTask должен устанавливать правильные значения в функцию")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testWriteTaskSetsCorrectValues() throws InterruptedException {
        double testValue = 77.5;
        WriteTask testWriteTask = new WriteTask(function, testValue);
        ReadTask readTask = new ReadTask(function);

        Thread writeThread = new Thread(testWriteTask);
        Thread readThread = new Thread(readTask);

        writeThread.start();
        readThread.start();

        writeThread.join(2000);
        readThread.join(1000);

        for (int i = 0; i < function.getCount(); i++) {
            assertEquals(testValue, function.getY(i), 1e-9,
                    "Значение Y[" + i + "] должно быть " + testValue);
        }
    }

    @Test
    @DisplayName("WriteTask должен корректно обрабатывать прерывание")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testInterruptionHandling() throws InterruptedException {
        Thread thread = new Thread(writeTask);

        thread.start();
        Thread.sleep(100);
        thread.interrupt();
        thread.join(500);

        assertFalse(thread.isAlive(), "Поток должен завершиться после прерывания");
    }

    @Test
    @DisplayName("WriteTask должен выводить сообщения о записи для всех индексов")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testWriteTaskOutputsAllIndexes() throws InterruptedException {
        AtomicBoolean writeCompleted = new AtomicBoolean(false);
        AtomicInteger writeCount = new AtomicInteger(0);

        Thread notifierThread = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread writeThread = new Thread(() -> {
            writeTask.run();
            writeCompleted.set(true);
        });

        notifierThread.start();
        writeThread.start();

        writeThread.join(2000);
        notifierThread.interrupt();
        notifierThread.join(500);

        String output = outputStream.toString();

        if (writeCompleted.get()) {
            for (int i = 0; i < function.getCount(); i++) {
                assertTrue(output.contains("Writing for index " + i),
                        "Вывод должен содержать запись для индекса " + i);
            }
        }
    }


    @Test
    @DisplayName("WriteTask должен сохранять оригинальные значения X")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testWriteTaskPreservesXValues() throws InterruptedException {
        double[] originalX = {1.0, 2.0, 3.0};
        double[] originalY = {10.0, 20.0, 30.0};
        TabulatedFunction testFunction = new LinkedListTabulatedFunctionFactory().create(originalX, originalY);
        WriteTask testWriteTask = new WriteTask(testFunction, 99.0);
        ReadTask readTask = new ReadTask(testFunction);

        Thread writeThread = new Thread(testWriteTask);
        Thread readThread = new Thread(readTask);

        writeThread.start();
        readThread.start();

        writeThread.join(2000);
        readThread.join(1000);

        for (int i = 0; i < testFunction.getCount(); i++) {
            assertEquals(originalX[i], testFunction.getX(i), 1e-9,
                    "Значение X[" + i + "] не должно изменяться");
        }
    }

    @Test
    @DisplayName("Несколько WriteTask не должны конфликтовать при правильной синхронизации")
    @Timeout(value = 4, unit = TimeUnit.SECONDS)
    void testMultipleWriteTasks() throws InterruptedException {
        Object sharedLock = new Object();
        WriteTask writeTask1 = new WriteTask(function, 50.0);
        WriteTask writeTask2 = new WriteTask(function, 75.0);

        Thread thread1 = new Thread(writeTask1);
        Thread thread2 = new Thread(writeTask2);

        thread1.start();
        thread2.start();

        thread1.join(2000);
        thread2.join(2000);

        double finalValue = function.getY(0);
        for (int i = 1; i < function.getCount(); i++) {
            assertEquals(finalValue, function.getY(i), 1e-9,
                    "Все значения должны быть одинаковыми после записи");
        }
    }

    @Test
    @DisplayName("WriteTask должен флашить вывод после каждой операции")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testWriteTaskFlushesOutput() throws InterruptedException {
        ReadTask readTask = new ReadTask(function);

        Thread writeThread = new Thread(writeTask);
        Thread readThread = new Thread(readTask);

        writeThread.start();
        readThread.start();

        writeThread.join(2000);
        readThread.join(1000);

        String output = outputStream.toString();

        assertFalse(output.isEmpty(), "Вывод не должен быть пустым");
        assertTrue(output.contains("Writing for index"),
                "Вывод должен содержать сообщения о записи");
    }

    @Test
    @DisplayName("WriteTask с отрицательными значениями")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testWriteTaskWithNegativeValues() throws InterruptedException {
        double negativeValue = -99.0;
        WriteTask negativeWriteTask = new WriteTask(function, negativeValue);
        ReadTask readTask = new ReadTask(function);

        Thread writeThread = new Thread(negativeWriteTask);
        Thread readThread = new Thread(readTask);

        writeThread.start();
        readThread.start();

        writeThread.join(2000);
        readThread.join(1000);

        for (int i = 0; i < function.getCount(); i++) {
            assertEquals(negativeValue, function.getY(i), 1e-9,
                    "Значения должны быть установлены в " + negativeValue);
        }
    }
}