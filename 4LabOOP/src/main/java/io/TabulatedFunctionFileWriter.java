package io;

import functions.*;
import java.io.*;

public class TabulatedFunctionFileWriter {

    public static void main(String[] args) {
        try (
                BufferedWriter arrayWriter = new BufferedWriter(
                        new FileWriter("output/array_function.txt")
                );
                BufferedWriter linkedListWriter = new BufferedWriter(
                        new FileWriter("output/linked_list_function.txt")
                )
        ) {

            // Создаём табулированную функцию на основе массива
            double[] xValues = {1.0, 2.0, 3.0, 4.0};
            double[] yValues = {1.0, 4.0, 9.0, 16.0};
            TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);

            // Создаём табулированную функцию на основе связного списка
            TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);

            // Записываем в файлы
            FunctionsIO.writeTabulatedFunction(arrayWriter, arrayFunction);
            FunctionsIO.writeTabulatedFunction(linkedListWriter, linkedListFunction);

            System.out.println("Файлы успешно записаны в директорию output/");

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}