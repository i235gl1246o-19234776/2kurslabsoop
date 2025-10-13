import functions.ArrayTabulatedFunction;
import functions.MathFunction;
import functions.NewtonMethod;
import functions.RungeKuttaFunction;
import io.*;
import operations.ParallelIntegrator;

import java.util.concurrent.ForkJoinPool;

public class Main {

    public static void main(String[] args) {

        System.out.println("ОНО ЗАПУСТИЛОСЬ НАКОНЕЦ-ТО УРА f");
        ArrayTabulatedFunctionSerialization.main(args);
        ArrayTabulatedFunctionXmlSerialization.main(args);
        LinkedListTabulatedFunctionSerialization.main(args);
        //TabulatedFunctionFileInputStream.main(args);
        //TabulatedFunctionFileOutputStream.main(args);
        //TabulatedFunctionFileReader.main(args);
        //TabulatedFunctionFileWriter.main(args);
        //TabulatedFunctionJsonIO.main(args);
        //ForkJoinPool

    }

}
