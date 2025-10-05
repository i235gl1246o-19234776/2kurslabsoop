package concurrent;

import functions.TabulatedFunction;

public class WriteTask implements Runnable{

    private final TabulatedFunction function;
    private final double value;
    private final Object lock;

    public WriteTask(TabulatedFunction function, double value, Object lock){
        this.function = function;
        this.value = value;
        this.lock = lock;
    }

    @Override
    public void run(){
        try{
            for (int i = 0; i < function.getCount(); i++){
                synchronized (lock){
                    function.setY(i, value);
                    System.out.printf("Writing for index %d complete\n", i);
                    System.out.flush();

                    lock.notify();

                    if (i < function.getCount() - 1){
                        lock.wait();
                    }
                }
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

}
