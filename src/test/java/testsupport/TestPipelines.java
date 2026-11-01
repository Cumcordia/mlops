package testsupport;

import static testsupport.TestEvents.T0;

import java.time.Duration;
import java.util.List;
import mlops.model.Dataset;
import mlops.model.Evaluator;
import mlops.model.Model;
import mlops.model.Pipeline;
import mlops.model.Stage;
import mlops.model.Transform;
import mlops.visitor.CostRates;

public final class TestPipelines {

    public static final CostRates RATES = new CostRates(3.0, 0.5, 0.1);

    private TestPipelines() {
    }

    public static Pipeline fixture() {
        return new Pipeline("p", List.of(
                new Stage("ingest", List.of(
                        new Dataset("raw", 100.0),
                        new Dataset("labels", 20.0),
                        new Transform("clean", 10.0, List.of("raw", "labels")))),
                new Stage("training", List.of(
                        new Transform("features", 5.0, List.of("clean")),
                        new Model("m1", "v1", 10.0, T0.minus(Duration.ofDays(40)), List.of("features")),
                        new Model("m2", "v2", 4.0, T0.minus(Duration.ofDays(5)), List.of("features")),
                        new Evaluator("eval1", 2.0, List.of("m1"))))));
    }

    public static Pipeline singleStage(String stageId) {
        return new Pipeline("single", List.of(new Stage(stageId, List.of(
                new Model("model-x", "v1", 1.0, T0, List.of())))));
    }
}
