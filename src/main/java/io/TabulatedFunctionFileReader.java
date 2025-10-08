package io;

import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;

import java.io.*;

public class TabulatedFunctionFileReader {

    public static void main(String[] args) {
        String filePath = "input/function.txt";

        try (
                BufferedReader reader1 = new BufferedReader(new FileReader(filePath));
                BufferedReader reader2 = new BufferedReader(new FileReader(filePath))
        ) {
            TabulatedFunction arrayFunction = FunctionsIO.readTabulatedFunction(
                    reader1, new ArrayTabulatedFunctionFactory()
            );
            TabulatedFunction linkedListFunction = FunctionsIO.readTabulatedFunction(
                    reader2, new LinkedListTabulatedFunctionFactory()
            );

            System.out.println("ArrayTabulatedFunction:");
            System.out.println(arrayFunction);

            System.out.println("\nLinkedListTabulatedFunction:");
            System.out.println(linkedListFunction);

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}