package concurrent;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.UnitFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MultiplyingTaskExecutor {
    public static void main(String[] args) {
        TabulatedFunction function = new LinkedListTabulatedFunction(
                new UnitFunction(), 1.0, 10000.0, 10000
        );

        List<Thread> threads = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            MultiplyingTask task = new MultiplyingTask(function);

            Thread thread = new Thread(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
            threads.add(thread);
        }

        for (Thread thread : threads){
            thread.start();
        }

        try {
            latch.await(); //блокируемся, пока счетчик не достигнет 0
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Главный поток был прерван во время ожидания");
        }

        System.out.println(function);
    }
}
