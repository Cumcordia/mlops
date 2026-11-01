package listener;

import static mlops.event.EventType.ACCURACY_REPORTED;
import static mlops.event.EventType.DRIFT_REPORTED;
import static testsupport.TestEvents.metric;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import testsupport.TestSubject;
import org.junit.jupiter.api.Test;

import mlops.listener.DriftAlert;
import mlops.listener.DriftAlertListener;

class DriftAlertListenerTest {

    private final TestSubject subject = new TestSubject();
    private final DriftAlertListener listener = new DriftAlertListener(0.20);

    @Test
    void alertsAtOrAboveTheThresholdOnly() {
        listener.attachTo(subject);

        subject.emit(metric(DRIFT_REPORTED, "transactions", "ingest", 0.19));
        subject.emit(metric(DRIFT_REPORTED, "transactions", "ingest", 0.20));
        subject.emit(metric(DRIFT_REPORTED, "clicks", "ingest", 0.35));
        subject.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.99));

        List<DriftAlert> alerts = listener.alerts();
        assertEquals(2, alerts.size());
        assertEquals(0.20, alerts.get(0).drift());
        assertEquals("clicks", alerts.get(1).sourceId());
        assertEquals("ingest", alerts.get(1).stageId());
    }
}
