package concurrent;

import functions.TabulatedFunction;

public class MultiplyingTask implements Runnable{

    private final TabulatedFunction function;


    public MultiplyingTask(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public void run() {
        for (int i = 0; i < function.getCount(); i++) {
            synchronized (function) {
                function.setY(i, function.getY(i) * 2);
            }
        }
        System.out.println(Thread.currentThread().getName() + " закончил выполнение задачи.");
    }
}

/**
        Время | Поток 1         | Поток 2         | Поток 3
        T0    | i=0 [работает]  | i=0 [ждет]      | i=0 [ждет]
        T1    | i=1 [ждет]      | i=0 [работает]  | i=0 [ждет]
        T2    | i=1 [ждет]      | i=1 [ждет]      | i=0 [работает]
        T3    | i=1 [работает]  | i=1 [ждет]      | i=1 [ждет]
        T4    | i=2 [ждет]      | i=1 [работает]  | i=1 [ждет]
        T5    | i=2 [ждет]      | i=2 [ждет]      | i=1 [работает]
 */
