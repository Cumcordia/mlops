package listener;

import static mlops.event.EventType.ACCURACY_REPORTED;
import static mlops.event.EventType.DRIFT_REPORTED;
import static mlops.event.EventType.LATENCY_REPORTED;
import static mlops.event.EventType.STAGE_SUMMARY;
import static testsupport.TestEvents.CLOCK;
import static testsupport.TestEvents.metric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import mlops.event.StageSummaryEvent;
import mlops.listener.StageAggregator;
import testsupport.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StageAggregatorTest {

    private final TestSubject source = new TestSubject();
    private final StageAggregator aggregator = new StageAggregator(CLOCK);
    private final List<StageSummaryEvent> summaries = new ArrayList<>();

    @BeforeEach
    void wire() {
        aggregator.attachTo(source);
        aggregator.subscribe(STAGE_SUMMARY, summaries::add);
    }

    @Test
    void republishesRunningTotalsOfTheStage() {
        source.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.90));
        source.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.70));
        source.emit(metric(DRIFT_REPORTED, "clicks", "serving", 0.30));
        source.emit(metric(LATENCY_REPORTED, "fraud", "serving", 12.0));

        assertEquals(4, summaries.size());
        StageSummaryEvent last = summaries.get(3);
        assertEquals("serving", last.stageId());
        assertEquals(4, last.eventCount());
        assertEquals(2, last.accuracySamples());
        assertEquals(0.80, last.meanAccuracy().orElseThrow(), 1e-9);
        assertEquals(0.30, last.maxDrift(), 1e-9);
    }

    @Test
    void keepsStagesIndependent() {
        source.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.90));
        source.emit(metric(ACCURACY_REPORTED, "churn", "training", 0.60));

        assertEquals("training", summaries.get(1).stageId());
        assertEquals(1, summaries.get(1).eventCount());
        assertEquals(0.60, summaries.get(1).meanAccuracy().orElseThrow(), 1e-9);
    }

    @Test
    void meanAccuracyIsEmptyUntilAnAccuracySampleArrives() {
        source.emit(metric(LATENCY_REPORTED, "fraud", "serving", 12.0));

        assertTrue(summaries.get(0).meanAccuracy().isEmpty());
    }
}
