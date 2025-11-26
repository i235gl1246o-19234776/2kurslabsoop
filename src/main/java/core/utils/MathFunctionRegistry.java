package core.utils;

import functions.MathFunction;
import functions.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MathFunctionRegistry {
    private static final Map<String, Class<? extends MathFunction>> FUNCTION_CLASS_MAP = new LinkedHashMap<>();
    private static final Map<String, Integer> FUNCTION_PRIORITY_MAP = new HashMap<>();
    private static final Map<String, String> FUNCTION_EXPRESSION_MAP = new HashMap<>();

    static {
        // Регистрируем встроенные функции
        registerFunction("Квадратичная функция", SqrFunction.class, 1, "x²");
        registerFunction("Тождественная функция", IdentityFunction.class, 2, "x");
        registerFunction("Нулевая функция", ZeroFunction.class, 3, "0");
        registerFunction("Постоянная функция", ConstantFunction.class, 4, "1");

        // Регистрируем составные функции
        registerCompositeFunction("sin(x²)");
        registerCompositeFunction("cos(sin(x))");
        registerCompositeFunction("e^(-x²)");
        registerCompositeFunction("ln(1+x²)");
        registerCompositeFunction("1/(1+x²)");
    }

    private static void registerFunction(String displayName, Class<? extends MathFunction> functionClass,
                                         int priority, String expression) {
        FUNCTION_CLASS_MAP.put(displayName, functionClass);
        FUNCTION_PRIORITY_MAP.put(displayName, priority);
        FUNCTION_EXPRESSION_MAP.put(displayName, expression);
    }

    private static void registerCompositeFunction(String displayName) {
        FUNCTION_CLASS_MAP.put(displayName, CompositeFunction.class);
        FUNCTION_PRIORITY_MAP.put(displayName, 100 + FUNCTION_CLASS_MAP.size());
        // Для составных функций выражение совпадает с названием
        FUNCTION_EXPRESSION_MAP.put(displayName, displayName);
    }

    public static MathFunction getFunctionByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя функции не может быть пустым");
        }

        Class<? extends MathFunction> functionClass = FUNCTION_CLASS_MAP.get(name);
        if (functionClass == null) {
            throw new IllegalArgumentException("Функция с именем '" + name + "' не найдена.");
        }

        try {
            // Для составных функций нужна особая обработка
            if (CompositeFunction.class.isAssignableFrom(functionClass)) {
                // Это заглушка для примера, в реальном приложении здесь нужно создавать конкретные составные функции
                log.warn("Попытка создания составной функции '{}'. В текущей реализации это не поддерживается.", name);
                // Для примера создаем квадратичную функцию
                return new SqrFunction();
            }

            // Пытаемся найти конструктор без параметров
            try {
                return functionClass.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                // Если нет конструктора без параметров, пытаемся найти конструктор с параметрами
                if (ConstantFunction.class.isAssignableFrom(functionClass)) {
                    return new ConstantFunction(1.0);
                } else if (UnitFunction.class.isAssignableFrom(functionClass)) {
                    return new UnitFunction();
                }
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать экземпляр функции '" + name + "'", e);
        }
    }

    public static List<String> getAvailableFunctionNames() {
        return getSortedFunctionNames();
    }

    public static List<String> getAvailableCompositeFunctionNames() {
        return FUNCTION_CLASS_MAP.keySet().stream()
                .filter(name -> CompositeFunction.class.isAssignableFrom(FUNCTION_CLASS_MAP.get(name)))
                .sorted()
                .collect(Collectors.toList());
    }

    private static List<String> getSortedFunctionNames() {
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(FUNCTION_PRIORITY_MAP.entrySet());
        sortedEntries.sort((e1, e2) -> {
            int priorityCompare = e1.getValue().compareTo(e2.getValue());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            return e1.getKey().compareTo(e2.getKey());
        });

        return sortedEntries.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static String getFunctionExpression(String functionName) {
        return FUNCTION_EXPRESSION_MAP.getOrDefault(functionName, "f(x)");
    }

    public static boolean isTabulatedFunction(MathFunction function) {
        return function instanceof functions.TabulatedFunction;
    }

    public static boolean isCompositeFunction(String functionName) {
        Class<? extends MathFunction> clazz = FUNCTION_CLASS_MAP.get(functionName);
        return clazz != null && CompositeFunction.class.isAssignableFrom(clazz);
    }
}