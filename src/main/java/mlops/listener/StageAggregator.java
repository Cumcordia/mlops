package mlops.listener;

import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import mlops.event.EventType;
import mlops.event.MetricEvent;
import mlops.event.StageSummaryEvent;
import mlops.observer.AbstractSubject;
import mlops.observer.Observer;
import mlops.observer.Subject;

public final class StageAggregator extends AbstractSubject<StageSummaryEvent> implements Observer<MetricEvent> {

    private record StageStats(long eventCount, long accuracySamples, double accuracySum, double maxDrift) {

        static StageStats from(MetricEvent event) {
            boolean accuracy = event.type() == EventType.ACCURACY_REPORTED;
            boolean drift = event.type() == EventType.DRIFT_REPORTED;
            return new StageStats(1, accuracy ? 1 : 0, accuracy ? event.value() : 0.0, drift ? event.value() : 0.0);
        }

        StageStats plus(StageStats other) {
            return new StageStats(eventCount + other.eventCount, accuracySamples + other.accuracySamples,
                    accuracySum + other.accuracySum, Math.max(maxDrift, other.maxDrift));
        }
    }

    private final Clock clock;
    private final Map<String, StageStats> statsByStage = new ConcurrentHashMap<>();

    public StageAggregator(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public void attachTo(Subject<MetricEvent> source) {
        source.subscribe(EventType.metricTypes(), this);
    }

    @Override
    public void onEvent(MetricEvent event) {
        StageStats updated = statsByStage.merge(event.stageId(), StageStats.from(event), StageStats::plus);
        publish(new StageSummaryEvent(clock.instant(), event.stageId(), updated.eventCount(),
                updated.accuracySamples(), updated.accuracySum(), updated.maxDrift()));
    }
}
