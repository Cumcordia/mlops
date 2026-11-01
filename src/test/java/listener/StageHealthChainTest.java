package listener;

import static mlops.event.EventType.ACCURACY_REPORTED;
import static testsupport.TestEvents.CLOCK;
import static testsupport.TestEvents.metric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import testsupport.TestSubject;
import org.junit.jupiter.api.Test;

import mlops.listener.StageAggregator;
import mlops.listener.StageHealthListener;

class StageHealthChainTest {

    @Test
    void stageBecomesUnhealthyAndRecoversThroughTheChain() {
        TestSubject source = new TestSubject();
        StageAggregator aggregator = new StageAggregator(CLOCK);
        StageHealthListener health = new StageHealthListener(0.85);
        aggregator.attachTo(source);
        health.attachTo(aggregator);

        source.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.70));
        assertEquals(Set.of("serving"), health.unhealthyStages());

        source.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 1.0));
        source.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 1.0));
        assertTrue(health.unhealthyStages().isEmpty());
    }
}
