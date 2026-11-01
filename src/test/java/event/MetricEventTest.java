package event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import mlops.event.EventType;
import mlops.event.MetricEvent;
import static testsupport.TestEvents.T0;

class MetricEventTest {

    @Test
    void rejectsNonMetricEventType() {
        assertThrows(IllegalArgumentException.class,
                () -> new MetricEvent(EventType.STAGE_SUMMARY, T0, "model", "stage", 1.0));
    }

    @Test
    void rejectsBlankIds() {
        assertThrows(IllegalArgumentException.class,
                () -> new MetricEvent(EventType.ACCURACY_REPORTED, T0, " ", "stage", 0.9));
        assertThrows(IllegalArgumentException.class,
                () -> new MetricEvent(EventType.ACCURACY_REPORTED, T0, "model", null, 0.9));
    }

    @Test
    void rejectsNonFiniteValue() {
        assertThrows(IllegalArgumentException.class,
                () -> new MetricEvent(EventType.ACCURACY_REPORTED, T0, "model", "stage", Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new MetricEvent(EventType.DRIFT_REPORTED, T0, "data", "stage", Double.POSITIVE_INFINITY));
    }
}
