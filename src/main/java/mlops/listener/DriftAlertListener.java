package mlops.listener;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import mlops.event.EventType;
import mlops.event.MetricEvent;
import mlops.observer.Observer;
import mlops.observer.Subject;

public final class DriftAlertListener implements Observer<MetricEvent> {

    private final double threshold;
    private final Queue<DriftAlert> alerts = new ConcurrentLinkedQueue<>();

    public DriftAlertListener(double threshold) {
        this.threshold = threshold;
    }

    public void attachTo(Subject<MetricEvent> source) {
        source.subscribe(EventType.DRIFT_REPORTED, this);
    }

    @Override
    public void onEvent(MetricEvent event) {
        if (event.value() >= threshold) {
            alerts.add(new DriftAlert(event.sourceId(), event.stageId(), event.value(), event.timestamp()));
        }
    }

    public List<DriftAlert> alerts() {
        return List.copyOf(alerts);
    }
}
