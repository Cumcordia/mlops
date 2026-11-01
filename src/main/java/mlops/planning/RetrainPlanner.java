package mlops.planning;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import mlops.listener.RetrainRequest;
import mlops.model.Model;
import mlops.model.Pipeline;
import mlops.model.Stage;
import mlops.observer.AbstractSubject;
import mlops.visitor.ComputeCostVisitor;
import mlops.visitor.CostRates;
import mlops.visitor.LineageReportVisitor;
import mlops.visitor.StaleModelsVisitor;

public final class RetrainPlanner extends AbstractSubject<RetrainPlanEvent> {

    private final Map<String, Stage> stagesById;
    private final Clock clock;
    private final ComputeCostVisitor costVisitor;
    private final Duration modelMaxAge;

    public RetrainPlanner(Pipeline pipeline, Clock clock, CostRates rates, Duration modelMaxAge) {
        this.stagesById = pipeline.stages().stream().collect(Collectors.toUnmodifiableMap(Stage::id, Function.identity()));
        this.clock = Objects.requireNonNull(clock, "clock");
        this.costVisitor = new ComputeCostVisitor(rates);
        this.modelMaxAge = Objects.requireNonNull(modelMaxAge, "modelMaxAge");
    }

    public void plan(RetrainRequest request) {
        Stage stage = stagesById.get(request.stageId());
        if (stage == null) {
            throw new IllegalArgumentException("Unknown stage: " + request.stageId());
        }
        Instant now = clock.instant();

        double stageCost = stage.accept(costVisitor);
        List<Model> staleModels = stage.accept(new StaleModelsVisitor(now, modelMaxAge));
        LineageReportVisitor lineage = new LineageReportVisitor();
        stage.accept(lineage);

        publish(new RetrainPlanEvent(now, stage.id(), request.reason(), stageCost,
                staleModels.stream().map(Model::id).toList(), lineage.lines()));
    }
}
