package listener;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import static mlops.event.EventType.ACCURACY_REPORTED;
import static mlops.event.EventType.DRIFT_REPORTED;
import static mlops.event.EventType.LATENCY_REPORTED;
import mlops.listener.MetricLogListener;
import static testsupport.TestEvents.metric;
import testsupport.TestSubject;

class MetricLogListenerTest {

    private final TestSubject subject = new TestSubject();
    private final MetricLogListener log = new MetricLogListener();

    @Test
    void writesOneFormattedLinePerEvent() {
        log.attachTo(subject);

        subject.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.93));

        assertEquals(List.of("2026-01-01T00:00:00Z ACCURACY_REPORTED source=fraud stage=serving value=0.9300"),
                log.lines());
    }

    @Test
    void logsEveryMetricType() {
        log.attachTo(subject);

        subject.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.9));
        subject.emit(metric(LATENCY_REPORTED, "fraud", "serving", 10.0));
        subject.emit(metric(DRIFT_REPORTED, "transactions", "ingest", 0.1));

        assertEquals(3, log.size());
    }
}
