package core.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "performance_statistics")
public class PerformanceStatisticEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_timestamp", nullable = false)
    private Timestamp testTimestamp;

    @Column(name = "metric_category", nullable = false, length = 50)
    private String metricCategory;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(name = "metric_value", nullable = false, length = 255)
    private String metricValue;

    @Column(name = "numeric_value")
    private Double numericValue;

    @Column(name = "records_count")
    private Integer recordsCount;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    public PerformanceStatisticEntity() {}

    public PerformanceStatisticEntity(Timestamp testTimestamp, String metricCategory,
                                      String metricName, String metricValue,
                                      Double numericValue, Integer recordsCount,
                                      Long executionTimeMs) {
        this.testTimestamp = testTimestamp;
        this.metricCategory = metricCategory;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.numericValue = numericValue;
        this.recordsCount = recordsCount;
        this.executionTimeMs = executionTimeMs;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Timestamp getTestTimestamp() { return testTimestamp; }
    public void setTestTimestamp(Timestamp testTimestamp) { this.testTimestamp = testTimestamp; }
    public String getMetricCategory() { return metricCategory; }
    public void setMetricCategory(String metricCategory) { this.metricCategory = metricCategory; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getMetricValue() { return metricValue; }
    public void setMetricValue(String metricValue) { this.metricValue = metricValue; }
    public Double getNumericValue() { return numericValue; }
    public void setNumericValue(Double numericValue) { this.numericValue = numericValue; }
    public Integer getRecordsCount() { return recordsCount; }
    public void setRecordsCount(Integer recordsCount) { this.recordsCount = recordsCount; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
