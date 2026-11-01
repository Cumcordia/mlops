package mlops.event;

import java.util.Arrays;
import java.util.List;

public enum EventType {
    ACCURACY_REPORTED(true),
    LATENCY_REPORTED(true),
    DRIFT_REPORTED(true),
    STAGE_SUMMARY(false),
    RETRAIN_PLANNED(false);

    private final boolean metric;

    EventType(boolean metric) {
        this.metric = metric;
    }

    public boolean isMetric() {
        return metric;
    }

    public static List<EventType> metricTypes() {
        return Arrays.stream(values()).filter(EventType::isMetric).toList();
    }
}
