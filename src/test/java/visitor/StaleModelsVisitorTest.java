package visitor;

import static testsupport.TestEvents.T0;
import static testsupport.TestPipelines.fixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import mlops.model.Model;
import mlops.model.Pipeline;
import mlops.visitor.StaleModelsVisitor;

import org.junit.jupiter.api.Test;

class StaleModelsVisitorTest {

    private final Pipeline pipeline = fixture();

    @Test
    void returnsOnlyModelsOlderThanTheLimit() {
        List<Model> stale = pipeline.accept(new StaleModelsVisitor(T0, Duration.ofDays(30)));

        assertEquals(List.of("m1"), stale.stream().map(Model::id).toList());
    }

    @Test
    void modelExactlyAtTheLimitIsNotStale() {
        List<Model> stale = pipeline.accept(new StaleModelsVisitor(T0, Duration.ofDays(40)));

        assertTrue(stale.isEmpty());
    }

    @Test
    void looksOnlyInsideTheVisitedSubtree() {
        StaleModelsVisitor visitor = new StaleModelsVisitor(T0, Duration.ofDays(1));

        assertTrue(pipeline.stages().get(0).accept(visitor).isEmpty());
        assertEquals(List.of("m1", "m2"),
                pipeline.stages().get(1).accept(visitor).stream().map(Model::id).toList());
    }
}
