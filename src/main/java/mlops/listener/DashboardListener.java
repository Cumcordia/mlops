package mlops.listener;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import mlops.event.EventType;
import mlops.event.MetricEvent;
import mlops.observer.Observer;
import mlops.observer.Subject;

public final class DashboardListener implements Observer<MetricEvent> {

    private record SeriesKey(String sourceId, EventType type) {
    }

    private final Map<SeriesKey, MetricEvent> latest = new ConcurrentHashMap<>();

    public void attachTo(Subject<MetricEvent> source) {
        source.subscribe(EventType.metricTypes(), this);
    }

    @Override
    public void onEvent(MetricEvent event) {
        latest.merge(new SeriesKey(event.sourceId(), event.type()), event, DashboardListener::newer);
    }

    public Optional<MetricEvent> latest(String sourceId, EventType type) {
        return Optional.ofNullable(latest.get(new SeriesKey(sourceId, type)));
    }

    public int trackedSeries() {
        return latest.size();
    }

    private static MetricEvent newer(MetricEvent current, MetricEvent candidate) {
        return candidate.timestamp().isBefore(current.timestamp()) ? current : candidate;
    }
}
