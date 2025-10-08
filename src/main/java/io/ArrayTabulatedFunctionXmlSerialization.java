package io;

import functions.ArrayTabulatedFunction;
import java.io.*;

public class ArrayTabulatedFunctionXmlSerialization {
    public static void main(String[] args) {
        System.out.println("Тест XML-сериализации ArrayTabulatedFunction...");

        // Создаём тестовую функцию
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        String filename = "function.xml";

        try {
            // ➡️ Сериализация в XML
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                FunctionsIO.serializeXml(writer, function);
                System.out.println("Функция успешно сериализована в " + filename);
            }

            // ➡️ Десериализация из XML
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                ArrayTabulatedFunction restored = FunctionsIO.deserializeXml(reader);
                System.out.println("Функция успешно десериализована!");

                // Проверим, что данные совпадают
                System.out.println("Оригинал: " + function.toString());
                System.out.println("Восстановлено: " + restored.toString());

                boolean equal = function.getCount() == restored.getCount();
                for (int i = 0; equal && i < function.getCount(); i++) {
                    equal = function.getX(i) == restored.getX(i) &&
                            function.getY(i) == restored.getY(i);
                }

                System.out.println(equal ? "Данные совпадают!" : "Данные НЕ совпадают!");
            }

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}