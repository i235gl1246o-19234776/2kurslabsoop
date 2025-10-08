package concurrent;

import functions.*;

public class ReadWriteTaskExecutor {

    public static void main(String[] args) {
        final Object lock = new Object();

        ConstantFunction constantFunction = new ConstantFunction(-1.0);

        TabulatedFunction tabulatedFunction = new LinkedListTabulatedFunction(constantFunction, 1, 1000, 1000);
        System.out.printf("Табулированная функция: %s на интервале [%f, %f]%n",
                constantFunction, 1.0, 1000.0);
        System.out.println("WriteTask будет устанавливать все Y = 0.5");
        System.out.println("===========================");
        System.out.flush();

        ReadTask readTask = new ReadTask(tabulatedFunction);
        WriteTask writeTask = new WriteTask(tabulatedFunction, 0.5);

        Thread readThread = new Thread(readTask, "ReadThread");
        Thread writeThread = new Thread(writeTask, "WriteThread");

        writeThread.start();
        readThread.start();

        try{
            writeThread.join();
            readThread.join();

        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }
}
