// src/main/java/core/utils/MathFunctionRegistry.java
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
    }

    private static void registerFunction(Class<? extends MathFunction> functionClass) {
        if (functionClass.isAnnotationPresent(FunctionInfo.class)) {
            FunctionInfo annotation = functionClass.getAnnotation(FunctionInfo.class);
            FUNCTION_CLASS_MAP.put(annotation.displayName(), functionClass);
            FUNCTION_PRIORITY_MAP.put(annotation.displayName(), annotation.priority());
        }
    }

    public static void scanPackage(String packageName) {
        try {
            // Получаем все классы в пакете
            List<Class<?>> classes = getClasses(packageName);

            for (Class<?> clazz : classes) {
                // Проверяем, что класс реализует MathFunction и является конкретным классом
                if (MathFunction.class.isAssignableFrom(clazz) &&
                        !clazz.isInterface() &&
                        !Modifier.isAbstract(clazz.getModifiers())) {

                    // Проверяем наличие аннотации FunctionInfo
                    if (clazz.isAnnotationPresent(FunctionInfo.class)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends MathFunction> functionClass = (Class<? extends MathFunction>) clazz;
                        registerFunction(functionClass);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при сканировании пакета " + packageName + ": " + e.getMessage());
        }
    }

    private static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        // Это упрощенная реализация для демонстрации
        // В реальном приложении здесь был бы код для получения всех классов в пакете
        List<Class<?>> classes = new ArrayList<>();

        // Возвращаем встроенные функции для демонстрации
        classes.add(SqrFunction.class);
        classes.add(IdentityFunction.class);
        classes.add(ZeroFunction.class);
        classes.add(ConstantFunction.class);

        return classes;
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