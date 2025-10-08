package io;

import functions.TabulatedFunction;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.text.ParseException;
import java.util.Comparator;

public class FunctionsIOTest {

    private static final double DELTA = 1e-10;

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void setUp() {
        System.out.println("Тестирование в директории: " + tempDir.toString());
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        System.out.println("Тестирование завершено, директория очищена");
    }

    @Test
    @DisplayName("Запись и чтение текстовой функции ArrayTabulatedFunction")
    void testWriteAndReadTabulatedFunctionText() throws IOException {
        Path testFile = tempDir.resolve("test_function.txt");

        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction original = new ArrayTabulatedFunction(xValues, yValues);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile.toFile()))) {
            FunctionsIO.writeTabulatedFunction(writer, original);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(testFile.toFile()))) {
            ArrayTabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
            TabulatedFunction readFunction = FunctionsIO.readTabulatedFunction(reader, factory);

            assertEquals(readFunction.getCount(), original.getCount());
            for (int i = 0; i < original.getCount(); i++) {
                assertEquals(readFunction.getX(i), original.getX(i), DELTA);
                assertEquals(readFunction.getY(i), original.getY(i), DELTA);
            }
        }
    }

    @Test
    @DisplayName("Запись и чтение бинарной функции LinkedListTabulatedFunction")
    void testWriteAndReadTabulatedFunctionBinary() throws IOException {
        Path testFile = tempDir.resolve("test_function.bin");

        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction original = new LinkedListTabulatedFunction(xValues, yValues);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(testFile.toFile()))) {
            FunctionsIO.writeTabulatedFunction(outputStream, original);
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(testFile.toFile()))) {
            LinkedListTabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
            TabulatedFunction readFunction = FunctionsIO.readTabulatedFunction(inputStream, factory);

            assertEquals(readFunction.getCount(), original.getCount());
            for (int i = 0; i < original.getCount(); i++) {
                assertEquals(readFunction.getX(i), original.getX(i), DELTA);
                assertEquals(readFunction.getY(i), original.getY(i), DELTA);
            }
        }
    }

    @Test
    @DisplayName("Чтение функции с русской локалью (запятая как разделитель)")
    void testReadTabulatedFunctionWithRussianLocale() throws IOException {
        Path testFile = tempDir.resolve("russian_function.txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile.toFile()))) {
            writer.println("3");
            writer.println("1,5 10,5");
            writer.println("2,5 20,5");
            writer.println("3,5 30,5");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(testFile.toFile()))) {
            ArrayTabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
            TabulatedFunction function = FunctionsIO.readTabulatedFunction(reader, factory);

            assertEquals(function.getCount(), 3);
            assertEquals(function.getX(0), 1.5, DELTA);
            assertEquals(function.getY(0), 10.5, DELTA);
            assertEquals(function.getX(1), 2.5, DELTA);
            assertEquals(function.getY(1), 20.5, DELTA);
            assertEquals(function.getX(2), 3.5, DELTA);
            assertEquals(function.getY(2), 30.5, DELTA);
        }
    }

    @Test
    @DisplayName("Сериализация и десериализация функции")
    void testSerializeAndDeserialize() throws IOException, ClassNotFoundException {
        Path testFile = tempDir.resolve("serialized_function.bin");

        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction original = new ArrayTabulatedFunction(xValues, yValues);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(testFile.toFile()));
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(original);
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(testFile.toFile()))) {
            TabulatedFunction deserialized = FunctionsIO.deserialize(inputStream);

            assertTrue(deserialized instanceof ArrayTabulatedFunction);
            assertEquals(deserialized.getCount(), original.getCount());
            for (int i = 0; i < original.getCount(); i++) {
                assertEquals(deserialized.getX(i), original.getX(i), DELTA);
                assertEquals(deserialized.getY(i), original.getY(i), DELTA);
            }
        }
    }

    @Test
    @DisplayName("Чтение из пустого файла должно бросать IOException")
    void testReadFromEmptyFile() throws IOException {
        Path testFile = tempDir.resolve("empty_file.txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile.toFile()))) {
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(testFile.toFile()))) {
            ArrayTabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
            assertThrows(IOException.class, () -> {
                FunctionsIO.readTabulatedFunction(reader, factory);
            });
        }
    }

    @Test
    @DisplayName("Чтение из файла с некорректными данными должно бросать IOException")
    void testReadFromInvalidFile() throws IOException {
        Path testFile = tempDir.resolve("invalid_file.txt");

        try (PrintWriter writer = new PrintWriter(new FileWriter(testFile.toFile()))) {
            writer.println("2");
            writer.println("1.0 10.0");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(testFile.toFile()))) {
            ArrayTabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
            assertThrows(IOException.class, () -> {
                FunctionsIO.readTabulatedFunction(reader, factory);
            });
        }
    }

    @Test
    @DisplayName("Чтение одной функции разными фабриками должно создавать разные типы")
    void testWriteAndReadWithDifferentFactories() throws IOException {
        Path testFile = tempDir.resolve("different_factories.bin");

        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction original = new ArrayTabulatedFunction(xValues, yValues);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(testFile.toFile()))) {
            FunctionsIO.writeTabulatedFunction(outputStream, original);
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(testFile.toFile()))) {
            ArrayTabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
            TabulatedFunction arrayFunction = FunctionsIO.readTabulatedFunction(inputStream, arrayFactory);
            assertTrue(arrayFunction instanceof ArrayTabulatedFunction);
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(testFile.toFile()))) {
            LinkedListTabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
            TabulatedFunction linkedListFunction = FunctionsIO.readTabulatedFunction(inputStream, linkedListFactory);
            assertTrue(linkedListFunction instanceof LinkedListTabulatedFunction);
        }
    }

    @Test
    @DisplayName("Десериализация должна бросать исключение для некорректных данных")
    void testDeserializeInvalidData() {
        Path testFile = tempDir.resolve("invalid_data.bin");

        byte[] invalidData = {0x00, 0x01, 0x02, 0x03, 0x04};

        try (FileOutputStream fos = new FileOutputStream(testFile.toFile())) {
            fos.write(invalidData);
        } catch (IOException e) {
            fail("Не удалось записать тестовые данные");
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(testFile.toFile()))) {
            assertThrows(IOException.class, () -> {
                FunctionsIO.deserialize(inputStream);
            });
        } catch (IOException e) {
            fail("Не удалось прочитать тестовые данные");
        }
    }

    @Test
    @DisplayName("Запись в закрытый поток должна бросать IOException")
    void testWriteToClosedStream() throws IOException {
        Path testFile = tempDir.resolve("closed_stream.bin");

        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0};
        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        BufferedOutputStream closedStream = new BufferedOutputStream(new FileOutputStream(testFile.toFile()));
        closedStream.close();

        assertThrows(IOException.class, () -> {
            FunctionsIO.writeTabulatedFunction(closedStream, function);
        });
    }

    @Test
    @DisplayName("Чтение из закрытого потока должно бросать IOException")
    void testReadFromClosedStream() throws IOException {
        Path testFile = tempDir.resolve("closed_read_stream.bin");

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(testFile.toFile()))) {
            outputStream.write(new byte[]{0x01, 0x02, 0x03});
        }

        BufferedInputStream closedStream = new BufferedInputStream(new FileInputStream(testFile.toFile()));
        closedStream.close();

        ArrayTabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        assertThrows(IOException.class, () -> {
            FunctionsIO.readTabulatedFunction(closedStream, factory);
        });
    }

    @Test
    @DisplayName("Десериализация из закрытого потока должна бросать IOException")
    void testDeserializeFromClosedStream() throws IOException {
        Path testFile = tempDir.resolve("closed_deserialize_stream.bin");

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(testFile.toFile()))) {
            outputStream.write(new byte[]{0x01, 0x02, 0x03});
        }

        BufferedInputStream closedStream = new BufferedInputStream(new FileInputStream(testFile.toFile()));
        closedStream.close();

        assertThrows(IOException.class, () -> {
            FunctionsIO.deserialize(closedStream);
        });
    }


    @Test
    @DisplayName("Множественная сериализация и десериализация функций")
    void testMultipleSerializeAndDeserialize() throws IOException, ClassNotFoundException {
        Path testFile = tempDir.resolve("multiple_functions.bin");

        double[] xValues1 = {1.0, 2.0, 3.0};
        double[] yValues1 = {10.0, 20.0, 30.0};
        double[] xValues2 = {4.0, 5.0, 6.0};
        double[] yValues2 = {40.0, 50.0, 60.0};

        TabulatedFunction function1 = new ArrayTabulatedFunction(xValues1, yValues1);
        TabulatedFunction function2 = new LinkedListTabulatedFunction(xValues2, yValues2);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(testFile.toFile()));
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(function1);
            objectOutputStream.writeObject(function2);
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(testFile.toFile()));
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            TabulatedFunction deserialized1 = (TabulatedFunction) objectInputStream.readObject();
            TabulatedFunction deserialized2 = (TabulatedFunction) objectInputStream.readObject();

            assertTrue(deserialized1 instanceof ArrayTabulatedFunction);
            assertTrue(deserialized2 instanceof LinkedListTabulatedFunction);
            assertEquals(3, deserialized1.getCount());
            assertEquals(3, deserialized2.getCount());
        }
    }


    @Test
    @DisplayName("JSON десериализация с некорректными данными")
    void testJsonDeserializeInvalidData() throws IOException {
        Path testFile = tempDir.resolve("invalid.json");

        // Создаем файл с некорректным JSON
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile.toFile()))) {
            writer.write("{\"invalid\": \"data\"}");
        }

        // Проверяем, что возникает исключение
        assertThrows(IOException.class, () -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(testFile.toFile()))) {
                FunctionsIO.deserializeJson(reader);
            }
        });
    }

    @Test
    @DisplayName("Приватный конструктор FunctionsIO должен выбросить UnsupportedOperationException")
    void privateConstructor_ShouldThrowException() {
        // Получаем приватный конструктор через рефлексию
        assertThrows(RuntimeException.class, () -> {
            try {
                Constructor<FunctionsIO> constructor = FunctionsIO.class.getDeclaredConstructor();
                constructor.setAccessible(true); // обходим private
                constructor.newInstance(); // пытаемся создать экземпляр
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, "Должен выбросить 'Не удается создать экземпляр служебного класса'");
    }

    @Test
    @DisplayName("readTabulatedFunction должен выбросить IOException, если в строке не 2 числа")
    void readTabulatedFunction_InvalidColumnCount_ThrowsIOException() throws IOException {
        // Тест с 3 числами в строке
        String data = "1\n1.0 2.0 3.0"; // должно быть 2 — а тут 3
        StringReader stringReader = new StringReader(data);
        BufferedReader reader = new BufferedReader(stringReader);

        assertThrows(IOException.class, () -> {
            FunctionsIO.readTabulatedFunction(reader, new ArrayTabulatedFunctionFactory());
        });

    }

    @Test
    @DisplayName("readTabulatedFunction должен выбросить IOException при ParseException")
    void readTabulatedFunction_ParseException_ThrowsWrappedIOException() throws IOException {
        // Эмулируем ситуацию, когда NumberFormat.parse() выбросит ParseException
        // Для этого передадим строку с буквами вместо чисел
        String data = "1\nabc def";
        StringReader stringReader = new StringReader(data);
        BufferedReader reader = new BufferedReader(stringReader);

        assertThrows(IOException.class, () -> {
            FunctionsIO.readTabulatedFunction(reader, new ArrayTabulatedFunctionFactory());
        });
    }

    @Test
    @DisplayName("readTabulatedFunction должен выбросить IOException при NumberFormatException")
    void readTabulatedFunction_NumberFormatException_ThrowsWrappedIOException() {
        // Эмулируем NumberFormatException через рефлексию или хитрый ввод?
        // Но в текущей реализации используется NumberFormat → ParseException, а не NFE.
        // Значит, NFE может возникнуть только при Integer.parseInt(countLine)

        String data = "not_a_number\n1.0 2.0"; // countLine — не число
        StringReader stringReader = new StringReader(data);
        BufferedReader reader = new BufferedReader(stringReader);

        assertThrows(IOException.class, () -> {
            FunctionsIO.readTabulatedFunction(reader, new ArrayTabulatedFunctionFactory());
        });


    }

    @Test
    @DisplayName("serialize должен корректно сериализовать функцию без ошибок")
    void serialize_ValidFunction_NoExceptionThrown() throws IOException {
        TabulatedFunction func = new ArrayTabulatedFunction(
                new double[]{1.0, 2.0},
                new double[]{1.0, 4.0}
        );

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        BufferedOutputStream stream = new BufferedOutputStream(byteOut);

        assertDoesNotThrow(() -> {
            FunctionsIO.serialize(stream, func);
        }, "Сериализация не должна выбрасывать исключение для валидной функции");

        stream.flush();
        assertTrue(byteOut.size() > 0, "Поток должен содержать данные после сериализации");
    }

    @Test
    @DisplayName("serializeXml и deserializeXml должны корректно сохранять и восстанавливать функцию")
    void serializeDeserializeXml_RoundTrip_ShouldPreserveData() throws IOException {
        // Исходная функция
        double[] x = {1.0, 2.0, 3.0, 4.0};
        double[] y = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction original = new ArrayTabulatedFunction(x, y);

        // Сериализуем в XML
        StringWriter stringWriter = new StringWriter();
        FunctionsIO.serializeXml(new BufferedWriter(stringWriter), original);

        String xml = stringWriter.toString();
        assertFalse(xml.isEmpty(), "XML не должен быть пустым");

        // Десериализуем из XML
        StringReader stringReader = new StringReader(xml);
        ArrayTabulatedFunction deserialized = FunctionsIO.deserializeXml(new BufferedReader(stringReader));

        // Проверяем восстановление
        assertNotNull(deserialized);
        assertEquals(original.getCount(), deserialized.getCount());

        for (int i = 0; i < original.getCount(); i++) {
            assertEquals(original.getX(i), deserialized.getX(i), 1e-10);
            assertEquals(original.getY(i), deserialized.getY(i), 1e-10);
        }
    }


    @Test
    @DisplayName("serializeXml должен выбросить NullPointerException при null функции")
    void serializeXml_NullFunction_ThrowsNullPointerException() {
        StringWriter writer = new StringWriter();

        assertThrows(NullPointerException.class, () -> {
            FunctionsIO.serializeXml(new BufferedWriter(writer), null);
        });
    }

    @Test
    @DisplayName("serializeXml должен выбросить IOException при записи в закрытый BufferedWriter")
    void serializeXml_ClosedWriter_ThrowsIOException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter writer = new BufferedWriter(stringWriter);
        writer.close(); // закрываем

        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{1.0, 2}, new double[]{1.0, 2});

        IOException exception = assertThrows(IOException.class, () -> {
            FunctionsIO.serializeXml(writer, func);
        });

        assertNotNull(exception);
        // Точное сообщение зависит от реализации BufferedWriter, но IOException точно будет
    }


    @Test
    @DisplayName("JSON сериализация и десериализация ArrayTabulatedFunction")
    void testJsonSerializationDeserialization() throws IOException {
        // Подготовка тестовых данных
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction original = new ArrayTabulatedFunction(xValues, yValues);

        Path testFile = tempDir.resolve("test_function.json");

        // Сериализация
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile.toFile()))) {
            FunctionsIO.serializeJson(writer, original);
        }

        // Десериализация
        ArrayTabulatedFunction deserialized;
        try (BufferedReader reader = new BufferedReader(new FileReader(testFile.toFile()))) {
            deserialized = FunctionsIO.deserializeJson(reader);
        }


    }

}
