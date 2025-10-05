package io;

import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import operations.TabulatedDifferentialOperator;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class TabulatedFunctionFileInputStream {

    public static void main(String[] args) {
        try (FileInputStream fileInputStream = new FileInputStream("input/array_function.bin");
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            ArrayTabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
            TabulatedFunction arrayFunction = FunctionsIO.readTabulatedFunction(bufferedInputStream, arrayFactory);

            System.out.println("Функция из бинарного файла:");
            System.out.println(arrayFunction.toString());

        } catch (IOException e) {
            System.err.println("Ошибка при чтении бинарного файла:");
            e.printStackTrace();
        }

        System.out.println("\nВведите размер и значения функции:");

        BufferedReader consoleReader = null;
        try {
            consoleReader = new BufferedReader(new InputStreamReader(System.in));

            LinkedListTabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
            TabulatedFunction consoleFunction = FunctionsIO.readTabulatedFunction(
                    new BufferedInputStream(System.in), linkedListFactory);

            System.out.println("Введенная функция:");
            System.out.println(consoleFunction.toString());

            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
            TabulatedFunction derivative = differentialOperator.derive(consoleFunction);

            System.out.println("Производная функции:");
            System.out.println(derivative.toString());

        } catch (IOException e) {
            System.err.println("Ошибка при чтении из консоли:");
            e.printStackTrace();
        } finally {
            if (consoleReader != null) {
                try {
                    consoleReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}