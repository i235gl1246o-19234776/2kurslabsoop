package io;

import static org.junit.jupiter.api.Assertions.*;
import functions.ArrayTabulatedFunction;
import org.junit.jupiter.api.Test;

import java.io.*;


public class ArrayTabulatedFunctionXmlTest {

    @Test
    public void testXmlSerializationDeserialization() throws Exception {
        double[] x = {0, 1, 2};
        double[] y = {0, 1, 4};
        ArrayTabulatedFunction original = new ArrayTabulatedFunction(x, y);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));

        FunctionsIO.serializeXml(writer, original);
        writer.close();

        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
        ArrayTabulatedFunction restored = FunctionsIO.deserializeXml(reader);

        assertEquals(original.getCount(), restored.getCount());
        for (int i = 0; i < original.getCount(); i++) {
            assertEquals(original.getX(i), restored.getX(i), 1e-9);
            assertEquals(original.getY(i), restored.getY(i), 1e-9);
        }
    }
}