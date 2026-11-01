package mlops.app;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import mlops.model.Dataset;
import mlops.model.Evaluator;
import mlops.model.Model;
import mlops.model.Pipeline;
import mlops.model.Stage;
import mlops.model.Transform;

public final class SamplePipeline {

    private SamplePipeline() {
    }

    public static Pipeline create(Instant now) {
        return new Pipeline("fraud-pipeline", List.of(
                new Stage("ingest", List.of(
                        new Dataset("transactions", 120.0),
                        new Dataset("clicks", 40.0),
                        new Transform("clean", 8.0, List.of("transactions", "clicks")))),
                new Stage("training", List.of(
                        new Transform("features", 12.0, List.of("clean")),
                        new Model("churn-model", "v3", 20.0, now.minus(Duration.ofDays(45)), List.of("features")),
                        new Evaluator("churn-eval", 2.0, List.of("churn-model")))),
                new Stage("serving", List.of(
                        new Model("fraud-model", "v7", 30.0, now.minus(Duration.ofDays(10)), List.of("features")),
                        new Evaluator("fraud-eval", 3.0, List.of("fraud-model"))))));
    }
}
