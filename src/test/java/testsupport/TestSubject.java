package testsupport;

import mlops.event.MetricEvent;
import mlops.observer.AbstractSubject;

/** Subject with a public emit method so tests can push arbitrary events. */
public final class TestSubject extends AbstractSubject<MetricEvent> {

    public void emit(MetricEvent event) {
        publish(event);
    }
}
