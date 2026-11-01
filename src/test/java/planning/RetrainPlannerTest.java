package planning;

import static testsupport.TestEvents.CLOCK;
import static testsupport.TestEvents.T0;
import static testsupport.TestPipelines.RATES;
import static testsupport.TestPipelines.fixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import mlops.event.EventType;
import mlops.listener.RetrainRequest;
import mlops.listener.RetrainRequest.Reason;
import mlops.planning.RetrainPlanBoard;
import mlops.planning.RetrainPlanEvent;
import mlops.planning.RetrainPlanner;
import testsupport.ProducerPoolSupport;
import org.junit.jupiter.api.Test;
import mlops.listener.*;

class RetrainPlannerTest {

    private final RetrainPlanner planner = new RetrainPlanner(fixture(), CLOCK, RATES, Duration.ofDays(30));
    private final RetrainPlanBoard board = new RetrainPlanBoard();

    private static RetrainRequest request(String stageId, Reason reason) {
        return new RetrainRequest(stageId, "source", reason, 0.5, T0);
    }

    @Test
    void inspectsOnlyTheRequestedStage() {
        board.attachTo(planner);

        planner.plan(request("training", Reason.ACCURACY_DROP));

        RetrainPlanEvent plan = board.plans().get(0);
        assertEquals("training", plan.stageId());
        assertEquals(Reason.ACCURACY_DROP, plan.reason());
        assertEquals(45.5, plan.stageCost(), 1e-9);
        assertEquals(List.of("m1"), plan.staleModels());
        assertEquals(List.of(
                "[training] transform features <- clean",
                "[training] model m1 <- features",
                "[training] model m2 <- features",
                "[training] evaluator eval1 <- m1"), plan.lineage());
        assertEquals(T0, plan.timestamp());
        assertEquals(EventType.RETRAIN_PLANNED, plan.type());
    }

    @Test
    void stageWithoutModelsGetsAnEmptyStaleList() {
        board.attachTo(planner);

        planner.plan(request("ingest", Reason.DATA_DRIFT));

        RetrainPlanEvent plan = board.plans().get(0);
        assertEquals(17.0, plan.stageCost(), 1e-9);
        assertTrue(plan.staleModels().isEmpty());
        assertEquals(3, plan.lineage().size());
    }

    @Test
    void rejectsRequestsForStagesOutsideThePipeline() {
        board.attachTo(planner);

        assertThrows(IllegalArgumentException.class, () -> planner.plan(request("missing", Reason.DATA_DRIFT)));
        assertTrue(board.plans().isEmpty());
    }

    @Test
    void concurrentRequestsAllProduceCorrectPlans() throws InterruptedException {
        List<RetrainPlanEvent> plans = new CopyOnWriteArrayList<>();
        planner.subscribe(EventType.RETRAIN_PLANNED, plans::add);

        ProducerPoolSupport.runConcurrently(4, 250,
                () -> planner.plan(request("training", Reason.ACCURACY_DROP)));

        assertEquals(1_000, plans.size());
        assertTrue(plans.stream().allMatch(plan -> Math.abs(plan.stageCost() - 45.5) < 1e-9
                && plan.staleModels().equals(List.of("m1"))));
    }
}
