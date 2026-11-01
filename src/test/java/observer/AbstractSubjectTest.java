package observer;

import static mlops.event.EventType.ACCURACY_REPORTED;
import static mlops.event.EventType.DRIFT_REPORTED;
import static testsupport.TestEvents.metric;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import mlops.event.MetricEvent;
import mlops.observer.Observer;
import testsupport.TestSubject;
import org.junit.jupiter.api.Test;

class AbstractSubjectTest {

    private final TestSubject subject = new TestSubject();
    private final List<MetricEvent> received = new ArrayList<>();

    @Test
    void deliversOnlyEventsOfTheSubscribedType() {
        subject.subscribe(DRIFT_REPORTED, received::add);

        subject.emit(metric(ACCURACY_REPORTED, "model", "stage", 0.9));
        subject.emit(metric(DRIFT_REPORTED, "data", "stage", 0.1));

        assertEquals(1, received.size());
        assertEquals(DRIFT_REPORTED, received.get(0).type());
    }

    @Test
    void stopsDeliveringAfterUnsubscribe() {
        Observer<MetricEvent> observer = received::add;
        subject.subscribe(DRIFT_REPORTED, observer);
        subject.emit(metric(DRIFT_REPORTED, "data", "stage", 0.1));

        subject.unsubscribe(DRIFT_REPORTED, observer);
        subject.emit(metric(DRIFT_REPORTED, "data", "stage", 0.2));

        assertEquals(1, received.size());
    }

    @Test
    void subscribingTheSameObserverTwiceDeliversOnce() {
        Observer<MetricEvent> observer = received::add;
        subject.subscribe(DRIFT_REPORTED, observer);
        subject.subscribe(DRIFT_REPORTED, observer);

        subject.emit(metric(DRIFT_REPORTED, "data", "stage", 0.1));

        assertEquals(1, received.size());
    }

    @Test
    void observerCanUnsubscribeItselfInsideItsHandler() {
        Observer<MetricEvent> oneShot = new Observer<>() {
            @Override
            public void onEvent(MetricEvent event) {
                received.add(event);
                subject.unsubscribe(DRIFT_REPORTED, this);
            }
        };
        subject.subscribe(DRIFT_REPORTED, oneShot);

        subject.emit(metric(DRIFT_REPORTED, "data", "stage", 0.1));
        subject.emit(metric(DRIFT_REPORTED, "data", "stage", 0.2));

        assertEquals(1, received.size());
    }

    @Test
    void observerSubscribedInsideAHandlerSeesOnlyLaterEvents() {
        List<String> order = new ArrayList<>();
        Observer<MetricEvent> late = event -> order.add("late");
        Observer<MetricEvent> first = event -> {
            order.add("first");
            subject.subscribe(DRIFT_REPORTED, late);
        };
        subject.subscribe(DRIFT_REPORTED, first);

        subject.emit(metric(DRIFT_REPORTED, "data", "stage", 0.1));
        subject.emit(metric(DRIFT_REPORTED, "data", "stage", 0.2));

        assertEquals(List.of("first", "first", "late"), order);
    }

    @Test
    void failingObserverDoesNotStopOthers() {
        subject.subscribe(DRIFT_REPORTED, event -> {
            throw new IllegalStateException("boom");
        });
        subject.subscribe(DRIFT_REPORTED, received::add);

        assertDoesNotThrow(() -> subject.emit(metric(DRIFT_REPORTED, "data", "stage", 0.1)));

        assertEquals(1, received.size());
    }

    @Test
    void publishingWithoutSubscribersIsHarmless() {
        assertDoesNotThrow(() -> subject.emit(metric(ACCURACY_REPORTED, "model", "stage", 0.9)));
    }
}
