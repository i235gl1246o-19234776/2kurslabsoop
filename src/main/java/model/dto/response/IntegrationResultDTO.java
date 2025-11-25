package model.dto.response;

public class IntegrationResultDTO {
    private double value;
    private double executionTimeMs;
    private int threadCount;
    private long steps;
    private double a;
    private double b;
    private String functionName;

    public IntegrationResultDTO(double value, double executionTimeMs, int threadCount, long steps,
                                double a, double b, String functionName) {
        this.value = value;
        this.executionTimeMs = executionTimeMs;
        this.threadCount = threadCount;
        this.steps = steps;
        this.a = a;
        this.b = b;
        this.functionName = functionName;
    }

    // Геттеры и сеттеры
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(double executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}