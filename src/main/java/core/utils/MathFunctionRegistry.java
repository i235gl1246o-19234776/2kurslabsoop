// src/main/java/core/utils/MathFunctionRegistry.java
package core.utils;

import functions.MathFunction;
import functions.*;

import java.util.*;

public class MathFunctionRegistry {

    // Маппинг имени функции на её класс
    private static final Map<String, Class<? extends MathFunction>> FUNCTION_CLASS_MAP = new LinkedHashMap<>();

    static {
        // Сохраняем классы, а не экземпляры
        FUNCTION_CLASS_MAP.put("Квадратичная функция", SqrFunction.class);
        FUNCTION_CLASS_MAP.put("Тождественная функция", IdentityFunction.class);
        FUNCTION_CLASS_MAP.put("Нулевая функция", ZeroFunction.class);
        // Добавь другие функции
    }

    public static MathFunction getFunctionByName(String name) {
        Class<? extends MathFunction> functionClass = FUNCTION_CLASS_MAP.get(name);
        if (functionClass == null) {
            throw new IllegalArgumentException("Функция с именем '" + name + "' не найдена.");
        }
        try {
            // Создаём экземпляр через конструктор по умолчанию
            return functionClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать экземпляр функции '" + name + "'", e);
        }
    }

    public static List<String> getAvailableFunctionNames() {
        List<String> names = new ArrayList<>(FUNCTION_CLASS_MAP.keySet());
        Collections.sort(names);
        return names;
    }

    public static boolean isTabulatedFunction(MathFunction function) {
        return function instanceof functions.TabulatedFunction;
    }
}