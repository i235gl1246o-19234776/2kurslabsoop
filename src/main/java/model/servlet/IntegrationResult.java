package model.servlet;

public class IntegrationResult {
    private double value;
    private long duration;
    private int threadCount;

    public double getValue() {
        return value;
    }

    public long getDuration() {
        return duration;
    }

    public int getThreadCount() {
        return threadCount;
    }


    public void setValue(double value) {
        this.value = value;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
