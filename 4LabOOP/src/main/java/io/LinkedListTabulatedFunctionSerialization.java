package io;

import functions.TabulatedFunction;
import functions.LinkedListTabulatedFunction;
import operations.TabulatedDifferentialOperator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class LinkedListTabulatedFunctionSerialization {

    public static void main(String[] args) {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0, 25.0}; // f(x) = x^2

        LinkedListTabulatedFunction originalFunction = new LinkedListTabulatedFunction(xValues, yValues);

        TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
        TabulatedFunction firstDerivative = differentialOperator.derive(originalFunction);
        TabulatedFunction secondDerivative = differentialOperator.derive(firstDerivative);

        try (FileOutputStream fileOutputStream = new FileOutputStream("output/serialized linked list functions.bin");
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {

            objectOutputStream.writeObject(originalFunction);
            objectOutputStream.writeObject(firstDerivative);
            objectOutputStream.writeObject(secondDerivative);

            System.out.println("Функции успешно сериализованы в файл:");
            System.out.println("- Исходная функция");
            System.out.println("- Первая производная");
            System.out.println("- Вторая производная");

        } catch (IOException e) {
            System.err.println("Ошибка при сериализации функций:");
            e.printStackTrace();
        }

        try (FileInputStream fileInputStream = new FileInputStream("output/serialized linked list functions.bin");
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream)) {

            TabulatedFunction deserializedOriginal = (TabulatedFunction) objectInputStream.readObject();
            TabulatedFunction deserializedFirstDerivative = (TabulatedFunction) objectInputStream.readObject();
            TabulatedFunction deserializedSecondDerivative = (TabulatedFunction) objectInputStream.readObject();

            System.out.println("\nДесериализованные функции:");

            System.out.println("\nИсходная функция:");
            System.out.println(deserializedOriginal.toString());

            System.out.println("\nПервая производная:");
            System.out.println(deserializedFirstDerivative.toString());

            System.out.println("\nВторая производная:");
            System.out.println(deserializedSecondDerivative.toString());

            System.out.println("\nТипы функций:");
            System.out.println("Исходная: " + deserializedOriginal.getClass().getSimpleName());
            System.out.println("Первая производная: " + deserializedFirstDerivative.getClass().getSimpleName());
            System.out.println("Вторая производная: " + deserializedSecondDerivative.getClass().getSimpleName());

        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода при десериализации:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Класс функции не найден:");
            e.printStackTrace();
        }
    }
}