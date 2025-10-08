package concurrent;

import static org.junit.jupiter.api.Assertions.*;
import functions.*;
import functions.factory.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@DisplayName("Тесты для класса ReadTask")
class ReadTaskTest {

    private TabulatedFunction function;
    private Object lock;
    private ReadTask readTask;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    @DisplayName("Инициализация тестовых данных")
    void setUp(){
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        function = new LinkedListTabulatedFunctionFactory().create(xValues, yValues);
        lock = new Object();
        readTask = new ReadTask(function);

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
    void testConstructor(){
        assertNotNull(readTask, "Объект ReadTask должен быть создан");
    }

    @Test
    @DisplayName("ReadTask должен читать данные при совместной работе с WriteTask")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testReadWriteIntegration() throws InterruptedException {
        WriteTask writeTask = new WriteTask(function, 99.0);

        Thread readThread = new Thread(readTask);
        Thread writeThread = new Thread(writeTask);

        writeThread.start();
        Thread.sleep(50);
        readThread.start();

        writeThread.join(2000);
        readThread.join(1000);

        String output = outputStream.toString();
        System.out.println("DEBUG Output: " + output);

        assertTrue(output.contains("After read:") && output.contains("Writing for index"),
                "Должны быть операции и чтения, и записи. Output: " + output);
    }

    @Test
    @DisplayName("ReadTask должен корректно обрабатывать прерывание")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testInterruptionHandling() throws InterruptedException {
        Thread thread = new Thread(readTask);

        thread.start();
        Thread.sleep(100);
        thread.interrupt();
        thread.join(500);

        assertFalse(thread.isAlive(), "Поток должен завершиться после прерывания");
    }

    @Test
    @DisplayName("ReadTask должен создаваться с правильными параметрами")
    void testTaskCreation() {
        assertNotNull(readTask, "ReadTask должен быть создан");
    }

    @Test
    @DisplayName("ReadTask должен корректно работать в быстром режиме с уведомлениями")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testReadTaskWithManualNotification() throws InterruptedException {
        AtomicBoolean readCompleted = new AtomicBoolean(false);

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

        Thread readThread = new Thread(() -> {
            readTask.run();
            readCompleted.set(true);
        });

        notifierThread.start();
        readThread.start();

        readThread.join(2000);
        notifierThread.interrupt();
        notifierThread.join(500);

        String output = outputStream.toString();

        if (readCompleted.get()) {
            assertTrue(output.contains("After read:"),
                    "При завершении чтения должен быть вывод. Output: " + output);
        }
    }
}