package io;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;

import java.io.*;

public class TabulatedFunctionJsonIO {

    public static void main(String[] args) {
        double[] xValues = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] yValues = {0.0, 0.25, 1.0, 2.25, 4.0}; // f(x) = x^2

        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Запись в JSON
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output/function.json"))) {
            FunctionsIO.serializeJson(writer, function);
            System.out.println("Функция успешно сериализована в JSON: output/function.json");
        } catch (IOException e) {
            System.err.println("Ошибка при записи JSON:");
            e.printStackTrace();
        }

        // Чтение из JSON
        try (BufferedReader reader = new BufferedReader(new FileReader("output/function.json"))) {
            ArrayTabulatedFunction loadedFunction = FunctionsIO.deserializeJson(reader);
            System.out.println("Функция успешно десериализована из JSON:");
            System.out.println("xValues: " + java.util.Arrays.toString(loadedFunction.getXVal()));
            System.out.println("yValues: " + java.util.Arrays.toString(loadedFunction.getYVal()));
        } catch (IOException e) {
            System.err.println("Ошибка при чтении JSON:");
            e.printStackTrace();
        }
    }
}