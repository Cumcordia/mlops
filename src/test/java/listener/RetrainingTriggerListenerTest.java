package listener;

import static mlops.event.EventType.ACCURACY_REPORTED;
import static mlops.event.EventType.DRIFT_REPORTED;
import static testsupport.TestEvents.metric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import mlops.listener.RetrainRequest;
import mlops.listener.RetrainRequest.Reason;
import mlops.listener.RetrainingTriggerListener;
import testsupport.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetrainingTriggerListenerTest {

    private final List<RetrainRequest> requests = new ArrayList<>();
    private final TestSubject subject = new TestSubject();
    private final RetrainingTriggerListener trigger = new RetrainingTriggerListener(0.80, 0.25, requests::add);

    @BeforeEach
    void wire() {
        trigger.attachTo(subject);
    }

    @Test
    void requestsRetrainingWhenAccuracyDropsBelowTheFloor() {
        subject.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.75));

        assertEquals(1, requests.size());
        RetrainRequest request = requests.get(0);
        assertEquals("serving", request.stageId());
        assertEquals("fraud", request.sourceId());
        assertEquals(Reason.ACCURACY_DROP, request.reason());
        assertEquals(0.75, request.value());
    }

    @Test
    void requestsRetrainingWhenDriftExceedsTheCeiling() {
        subject.emit(metric(DRIFT_REPORTED, "transactions", "ingest", 0.30));

        assertEquals(1, requests.size());
        assertEquals(Reason.DATA_DRIFT, requests.get(0).reason());
    }

    @Test
    void valuesExactlyOnTheLimitDoNotTrigger() {
        subject.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.80));
        subject.emit(metric(DRIFT_REPORTED, "transactions", "ingest", 0.25));

        assertTrue(requests.isEmpty());
    }

    @Test
    void keepsOneOpenRequestPerStageUntilMarkedRetrained() {
        subject.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.70));
        subject.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.60));
        subject.emit(metric(ACCURACY_REPORTED, "churn", "training", 0.70));
        assertEquals(2, requests.size());

        trigger.markRetrained("serving");
        subject.emit(metric(ACCURACY_REPORTED, "fraud", "serving", 0.65));

        assertEquals(3, requests.size());
    }
}
