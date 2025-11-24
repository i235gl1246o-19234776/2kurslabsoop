package core.utils;

import functions.MathFunction;
import functions.*;
import java.util.*;
import java.lang.reflect.*;

public class MathFunctionRegistry {
    private static final Map<String, Class<? extends MathFunction>> FUNCTION_CLASS_MAP = new LinkedHashMap<>();
    private static final Map<String, Integer> FUNCTION_PRIORITY_MAP = new HashMap<>();

    static {
        // Регистрируем встроенные функции
        registerFunction(SqrFunction.class);
        registerFunction(IdentityFunction.class);
        registerFunction(ZeroFunction.class);
        registerFunction(ConstantFunction.class);
        registerFunction(SinFunction.class);
        registerFunction(CosFunction.class);
        registerFunction(ExpFunction.class);
        registerFunction(LogFunction.class);
    }

    private static void registerFunction(Class<? extends MathFunction> functionClass) {
        FunctionInfo annotation = functionClass.getAnnotation(FunctionInfo.class);
        if (annotation != null) {
            FUNCTION_CLASS_MAP.put(annotation.displayName(), functionClass);
            FUNCTION_PRIORITY_MAP.put(annotation.displayName(), annotation.priority());
        }
    }

    public static void scanPackage(String packageName) {
        try {
            // В реальной реализации здесь будет код для сканирования пакета
            // Для демонстрации оставим только встроенные функции
        } catch (Exception e) {
            System.err.println("Ошибка при сканировании пакета " + packageName + ": " + e.getMessage());
        }
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
        // Сортируем по приоритету и алфавиту
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(FUNCTION_PRIORITY_MAP.entrySet());
        sortedEntries.sort((e1, e2) -> {
            int priorityCompare = e1.getValue().compareTo(e2.getValue());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            return e1.getKey().compareTo(e2.getKey());
        });
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            names.add(entry.getKey());
        }
        return names;
    }

    public static boolean isTabulatedFunction(MathFunction function) {
        return function instanceof functions.TabulatedFunction;
    }
}