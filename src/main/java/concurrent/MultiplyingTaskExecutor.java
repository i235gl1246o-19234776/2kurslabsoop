package concurrent;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.UnitFunction;

import java.util.ArrayList;
import java.util.List;

public class MultiplyingTaskExecutor {
    public static void main(String[] args) {
        TabulatedFunction function = new LinkedListTabulatedFunction(
                new UnitFunction(), 1.0, 1000.0, 1000
        );

        List<Thread> threads = new ArrayList<>();
        List<MultiplyingTask> tasks = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            MultiplyingTask task = new MultiplyingTask(function);
            tasks.add(task);
            Thread thread = new Thread(task);
            threads.add(thread);
        }
        for (Thread thread : threads){
            thread.start();
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
