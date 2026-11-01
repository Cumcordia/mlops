package mlops.source;

import java.time.Clock;
import java.util.Objects;

import mlops.event.EventType;
import mlops.event.MetricEvent;
import mlops.observer.AbstractSubject;

public final class DataDriftSource extends AbstractSubject<MetricEvent> {

    private final Clock clock;

    public DataDriftSource(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public void reportDrift(String datasetId, String stageId, double driftScore) {
        if (!(driftScore >= 0.0)) {
            throw new IllegalArgumentException("drift score must be >= 0: " + driftScore);
        }
        publish(new MetricEvent(EventType.DRIFT_REPORTED, clock.instant(), datasetId, stageId, driftScore));
    }
}
