package mlops.source;

import java.time.Clock;
import java.util.Objects;

import mlops.event.EventType;
import mlops.event.MetricEvent;
import mlops.observer.AbstractSubject;

public final class ModelMetricsSource extends AbstractSubject<MetricEvent> {

    private final Clock clock;

    public ModelMetricsSource(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public void reportAccuracy(String modelId, String stageId, double accuracy) {
        if (!(accuracy >= 0.0 && accuracy <= 1.0)) {
            throw new IllegalArgumentException("accuracy must be in [0, 1]: " + accuracy);
        }
        publish(new MetricEvent(EventType.ACCURACY_REPORTED, clock.instant(), modelId, stageId, accuracy));
    }

    public void reportLatency(String modelId, String stageId, double millis) {
        if (!(millis >= 0.0)) {
            throw new IllegalArgumentException("latency must be >= 0: " + millis);
        }
        publish(new MetricEvent(EventType.LATENCY_REPORTED, clock.instant(), modelId, stageId, millis));
    }
}
