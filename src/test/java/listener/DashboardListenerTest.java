package listener;

import static mlops.event.EventType.ACCURACY_REPORTED;
import static mlops.event.EventType.LATENCY_REPORTED;
import static testsupport.TestEvents.metricAt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mlops.listener.DashboardListener;
import testsupport.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DashboardListenerTest {

    private final TestSubject subject = new TestSubject();
    private final DashboardListener dashboard = new DashboardListener();

    @BeforeEach
    void wire() {
        dashboard.attachTo(subject);
    }

    @Test
    void keepsTheNewestValueEvenWhenAnOlderEventArrivesLate() {
        subject.emit(metricAt(10, ACCURACY_REPORTED, "fraud", "serving", 0.90));
        subject.emit(metricAt(5, ACCURACY_REPORTED, "fraud", "serving", 0.50));

        assertEquals(0.90, dashboard.latest("fraud", ACCURACY_REPORTED).orElseThrow().value());
    }

    @Test
    void tracksEachSourceAndMetricAsItsOwnSeries() {
        subject.emit(metricAt(1, ACCURACY_REPORTED, "fraud", "serving", 0.90));
        subject.emit(metricAt(1, LATENCY_REPORTED, "fraud", "serving", 12.0));
        subject.emit(metricAt(1, ACCURACY_REPORTED, "churn", "training", 0.85));

        assertEquals(3, dashboard.trackedSeries());
        assertEquals(12.0, dashboard.latest("fraud", LATENCY_REPORTED).orElseThrow().value());
    }

    @Test
    void unknownSeriesIsEmpty() {
        assertTrue(dashboard.latest("nobody", ACCURACY_REPORTED).isEmpty());
    }
}
