package source;

import static mlops.event.EventType.ACCURACY_REPORTED;
import static mlops.event.EventType.DRIFT_REPORTED;
import static mlops.event.EventType.LATENCY_REPORTED;
import static testsupport.TestEvents.CLOCK;
import static testsupport.TestEvents.T0;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import mlops.event.MetricEvent;
import mlops.source.DataDriftSource;

import mlops.source.ModelMetricsSource;

class SourcesTest {

    private final List<MetricEvent> received = new ArrayList<>();
    private final ModelMetricsSource modelMetrics = new ModelMetricsSource(CLOCK);
    private final DataDriftSource dataDrift = new DataDriftSource(CLOCK);

    @Test
    void modelSourcePublishesAccuracyStampedByTheClock() {
        modelMetrics.subscribe(ACCURACY_REPORTED, received::add);

        modelMetrics.reportAccuracy("fraud", "serving", 0.93);

        assertEquals(List.of(new MetricEvent(ACCURACY_REPORTED, T0, "fraud", "serving", 0.93)), received);
    }

    @Test
    void modelSourcePublishesLatency() {
        modelMetrics.subscribe(LATENCY_REPORTED, received::add);

        modelMetrics.reportLatency("fraud", "serving", 12.5);

        assertEquals(List.of(new MetricEvent(LATENCY_REPORTED, T0, "fraud", "serving", 12.5)), received);
    }

    @Test
    void modelSourceRejectsInvalidValuesWithoutPublishing() {
        modelMetrics.subscribe(ACCURACY_REPORTED, received::add);
        modelMetrics.subscribe(LATENCY_REPORTED, received::add);

        assertThrows(IllegalArgumentException.class, () -> modelMetrics.reportAccuracy("m", "s", 1.01));
        assertThrows(IllegalArgumentException.class, () -> modelMetrics.reportAccuracy("m", "s", -0.1));
        assertThrows(IllegalArgumentException.class, () -> modelMetrics.reportAccuracy("m", "s", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> modelMetrics.reportLatency("m", "s", -1.0));
        assertTrue(received.isEmpty());
    }

    @Test
    void driftSourcePublishesDriftScore() {
        dataDrift.subscribe(DRIFT_REPORTED, received::add);

        dataDrift.reportDrift("transactions", "ingest", 0.31);

        assertEquals(List.of(new MetricEvent(DRIFT_REPORTED, T0, "transactions", "ingest", 0.31)), received);
    }

    @Test
    void driftSourceRejectsNegativeScore() {
        assertThrows(IllegalArgumentException.class, () -> dataDrift.reportDrift("transactions", "ingest", -0.01));
    }
}
