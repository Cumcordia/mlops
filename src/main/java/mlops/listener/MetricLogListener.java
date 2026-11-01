package mlops.listener;

import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import mlops.event.EventType;
import mlops.event.MetricEvent;
import mlops.observer.Observer;
import mlops.observer.Subject;

public final class MetricLogListener implements Observer<MetricEvent> {

    private final Queue<String> lines = new ConcurrentLinkedQueue<>();

    public void attachTo(Subject<MetricEvent> source) {
        source.subscribe(EventType.metricTypes(), this);
    }

    @Override
    public void onEvent(MetricEvent event) {
        lines.add(String.format(Locale.ROOT, "%s %s source=%s stage=%s value=%.4f",
                event.timestamp(), event.type(), event.sourceId(), event.stageId(), event.value()));
    }

    public List<String> lines() {
        return List.copyOf(lines);
    }

    public int size() {
        return lines.size();
    }
}
