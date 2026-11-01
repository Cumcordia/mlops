package testsupport;

import mlops.event.MetricEvent;
import mlops.observer.AbstractSubject;

public final class TestSubject extends AbstractSubject<MetricEvent> {

    public void emit(MetricEvent event) {
        publish(event);
    }
}
