package concurrent;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.UnitFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiplyingTaskExecutor {
    public static void main(String[] args) {
        TabulatedFunction function = new LinkedListTabulatedFunction(
                new UnitFunction(), 1.0, 1000.0, 1000
        );

        List<Thread> threads = new ArrayList<>();
        // потокобезопасный лист
        List<MultiplyingTask> tasks = new CopyOnWriteArrayList<>();

        for (int i = 0; i < 10; i++) {
            MultiplyingTask task = new MultiplyingTask(function);
            tasks.add(task);
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start(); //запуск потока
        }

        for (Thread thread : threads) {
            try {
                thread.join(); // ожидание завершения потока
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); //Восстановление флага прерывания
            }
        }

        System.out.println(function);
    }
}
