package model;

public class OperationType {
    private Integer id;
    private String name;
    private String resultType;

    public static final OperationType AND_THEN = new OperationType(1, "AndThen", "function");
    public static final OperationType NEWTON_METHOD = new OperationType(2, "NewtonMethod", "num");
    public static final OperationType RUNGE_METHOD = new OperationType(3, "RungeMethod", "num");
    public static final OperationType DERIVATIVE = new OperationType(4, "Derivative", "function");
    public static final OperationType DEFINITE_INTEGRAL = new OperationType(5, "DefiniteIntegral", "num");
    public static final OperationType EVALUATE = new OperationType(6, "Evaluate", "num");

    public OperationType() {}

    public OperationType(Integer id, String name, String resultType) {
        this.id = id;
        this.name = name;
        this.resultType = resultType;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getResultType() { return resultType; }
    public void setResultType(String resultType) {
        if (!resultType.equals("num") && !resultType.equals("function")) {
            throw new IllegalArgumentException("Result type must be 'num' or 'function'");
        }
        this.resultType = resultType;
    }

    public boolean isNumericResult() {
        return "num".equals(resultType);
    }

    public boolean isFunctionResult() {
        return "function".equals(resultType);
    }

    public static OperationType getById(Integer id) {
        switch (id) {
            case 1: return AND_THEN;
            case 2: return NEWTON_METHOD;
            case 3: return RUNGE_METHOD;
            case 4: return DERIVATIVE;
            case 5: return DEFINITE_INTEGRAL;
            case 6: return EVALUATE;
            default: throw new IllegalArgumentException("Unknown operation type ID: " + id);
        }
    }

    public static OperationType[] getAllPredefinedTypes() {
        return new OperationType[]{
                AND_THEN, NEWTON_METHOD, RUNGE_METHOD,
                DERIVATIVE, DEFINITE_INTEGRAL, EVALUATE
        };
    }

    @Override
    public String toString() {
        return String.format("OperationType{id=%d, name='%s', resultType='%s'}",
                id, name, resultType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationType that = (OperationType) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}