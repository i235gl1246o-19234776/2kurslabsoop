package io;

import functions.*;
import java.io.*;

public class TabulatedFunctionFileOutputStream {

    public static void main(String[] args) {
        double[] xValues = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] yValues = {0.0, 0.25, 1.0, 2.25, 4.0}; // f(x) = x^2

        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);

        try (FileOutputStream arrayFileStream = new FileOutputStream("output/array_function.bin");
             BufferedOutputStream arrayBufferedStream = new BufferedOutputStream(arrayFileStream);

             FileOutputStream linkedListFileStream = new FileOutputStream("output/linked_list function.bin");
             BufferedOutputStream linkedListBufferedStream = new BufferedOutputStream(linkedListFileStream)) {

            FunctionsIO.writeTabulatedFunction(arrayBufferedStream, arrayFunction);
            FunctionsIO.writeTabulatedFunction(linkedListBufferedStream, linkedListFunction);

            System.out.println("Бинарные файлы успешно созданы:");
            System.out.println("- output/array function.bin");
            System.out.println("- output/linked list function.bin");
            System.out.println("Файлы содержат бинарные данные и не читаются как текст.");

        } catch (IOException e) {
            System.err.println("Ошибка при записи бинарных файлов:");
            e.printStackTrace();
        }
    }
}
