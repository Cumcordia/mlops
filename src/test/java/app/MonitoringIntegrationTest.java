package app;

import static testsupport.TestEvents.CLOCK;
import static testsupport.TestPipelines.RATES;
import static testsupport.TestPipelines.fixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import mlops.app.MonitoringConfig;
import mlops.app.MonitoringSystem;
import mlops.listener.RetrainRequest.Reason;
import mlops.planning.RetrainPlanEvent;
import org.junit.jupiter.api.Test;

class MonitoringIntegrationTest {

    private final MonitoringConfig config =
            new MonitoringConfig(0.20, 0.80, 0.25, 0.85, RATES, Duration.ofDays(30));
    private final MonitoringSystem system = new MonitoringSystem(CLOCK, config, fixture());

    @Test
    void accuracyDropTriggersAnInspectionOfTheTrainingSubtreeOnly() {
        system.modelMetrics().reportAccuracy("m1", "training", 0.50);

        assertEquals(1, system.retrainRequests().size());
        List<RetrainPlanEvent> plans = system.planBoard().plans();
        assertEquals(1, plans.size());
        assertEquals("training", plans.get(0).stageId());
        assertEquals(Reason.ACCURACY_DROP, plans.get(0).reason());
        assertEquals(45.5, plans.get(0).stageCost(), 1e-9);
        assertEquals(List.of("m1"), plans.get(0).staleModels());
    }

    @Test
    void driftTriggersAnInspectionOfTheIngestSubtreeOnly() {
        system.dataDrift().reportDrift("raw", "ingest", 0.30);

        RetrainPlanEvent plan = system.planBoard().plans().get(0);
        assertEquals("ingest", plan.stageId());
        assertEquals(Reason.DATA_DRIFT, plan.reason());
        assertEquals(17.0, plan.stageCost(), 1e-9);
        assertTrue(plan.staleModels().isEmpty());
    }

    @Test
    void healthyMetricsProduceNoPlan() {
        system.modelMetrics().reportAccuracy("m1", "training", 0.95);
        system.dataDrift().reportDrift("raw", "ingest", 0.10);

        assertTrue(system.planBoard().plans().isEmpty());
    }

    @Test
    void stageCanBePlannedAgainAfterItIsMarkedRetrained() {
        system.modelMetrics().reportAccuracy("m1", "training", 0.50);
        system.modelMetrics().reportAccuracy("m1", "training", 0.40);
        assertEquals(1, system.planBoard().plans().size());

        system.retrainingTrigger().markRetrained("training");
        system.modelMetrics().reportAccuracy("m1", "training", 0.45);

        assertEquals(2, system.planBoard().plans().size());
    }

    @Test
    void requestForAStageMissingFromThePipelineIsLoggedAndTheStageIsFreedAgain() {
        system.modelMetrics().reportAccuracy("ghost", "no-such-stage", 0.10);

        assertTrue(system.planBoard().plans().isEmpty());
        system.modelMetrics().reportAccuracy("ghost", "no-such-stage", 0.10);
        assertEquals(2, system.retrainRequests().size());
    }
}
