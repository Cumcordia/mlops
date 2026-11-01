package visitor;

import static testsupport.TestPipelines.fixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import mlops.model.Dataset;
import mlops.model.Pipeline;
import mlops.model.Transform;
import mlops.visitor.LineageReportVisitor;

import org.junit.jupiter.api.Test;

class LineageReportVisitorTest {

    private final Pipeline pipeline = fixture();

    @Test
    void collectsOneLinePerLeafAcrossTheWholeTree() {
        LineageReportVisitor visitor = new LineageReportVisitor();

        pipeline.accept(visitor);

        assertEquals(List.of(
                "[ingest] dataset raw",
                "[ingest] dataset labels",
                "[ingest] transform clean <- raw, labels",
                "[training] transform features <- clean",
                "[training] model m1 <- features",
                "[training] model m2 <- features",
                "[training] evaluator eval1 <- m1"), visitor.lines());
    }

    @Test
    void reportsOnlyTheVisitedSubtree() {
        LineageReportVisitor visitor = new LineageReportVisitor();

        pipeline.stages().get(1).accept(visitor);

        assertEquals(4, visitor.lines().size());
        assertEquals("[training] transform features <- clean", visitor.lines().get(0));
    }

    @Test
    void leafVisitedOnTheirOwnHaveNoStagePrefix() {
        LineageReportVisitor visitor = new LineageReportVisitor();

        new Dataset("d", 1.0).accept(visitor);
        new Transform("t", 1.0, List.of("d")).accept(visitor);

        assertEquals(List.of("dataset d", "transform t <- d"), visitor.lines());
    }
}
