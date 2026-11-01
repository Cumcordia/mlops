package visitor;

import static testsupport.TestPipelines.RATES;
import static testsupport.TestPipelines.fixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

import mlops.model.Dataset;
import mlops.model.Pipeline;
import mlops.visitor.ComputeCostVisitor;

import org.junit.jupiter.api.Test;

class ComputeCostVisitorTest {

    private final ComputeCostVisitor visitor = new ComputeCostVisitor(RATES);
    private final Pipeline pipeline = fixture();

    @Test
    void sumsTheWholePipeline() {
        assertEquals(62.5, pipeline.accept(visitor), 1e-9);
    }

    @Test
    void sumsOnlyTheVisitedSubtree() {
        assertEquals(17.0, pipeline.stages().get(0).accept(visitor), 1e-9);
        assertEquals(45.5, pipeline.stages().get(1).accept(visitor), 1e-9);
    }

    @Test
    void pricesASingleLeaf() {
        assertEquals(10.0, new Dataset("d", 100.0).accept(visitor), 1e-9);
    }
}
