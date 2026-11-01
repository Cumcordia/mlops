package mlops.visitor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mlops.model.Dataset;
import mlops.model.Evaluator;
import mlops.model.Model;
import mlops.model.Pipeline;
import mlops.model.Stage;
import mlops.model.StageMember;
import mlops.model.Transform;
import mlops.model.Visitor;

public final class StaleModelsVisitor implements Visitor<List<Model>> {

    private final Instant now;
    private final Duration maxAge;

    public StaleModelsVisitor(Instant now, Duration maxAge) {
        this.now = Objects.requireNonNull(now, "now");
        this.maxAge = Objects.requireNonNull(maxAge, "maxAge");
    }

    @Override
    public List<Model> visitPipeline(Pipeline pipeline) {
        List<Model> stale = new ArrayList<>();
        for (Stage stage : pipeline.stages()) {
            stale.addAll(stage.accept(this));
        }
        return stale;
    }

    @Override
    public List<Model> visitStage(Stage stage) {
        List<Model> stale = new ArrayList<>();
        for (StageMember member : stage.members()) {
            stale.addAll(member.accept(this));
        }
        return stale;
    }

    @Override
    public List<Model> visitModel(Model model) {
        boolean isStale = Duration.between(model.lastTrainedAt(), now).compareTo(maxAge) > 0;
        return isStale ? List.of(model) : List.of();
    }

    @Override
    public List<Model> visitDataset(Dataset dataset) {
        return List.of();
    }

    @Override
    public List<Model> visitTransform(Transform transform) {
        return List.of();
    }

    @Override
    public List<Model> visitEvaluator(Evaluator evaluator) {
        return List.of();
    }
}
