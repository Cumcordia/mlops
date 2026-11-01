package mlops.visitor;

import java.util.Objects;

import mlops.model.Dataset;
import mlops.model.Evaluator;
import mlops.model.Model;
import mlops.model.Pipeline;
import mlops.model.Stage;
import mlops.model.StageMember;
import mlops.model.Transform;
import mlops.model.Visitor;

public final class ComputeCostVisitor implements Visitor<Double> {

    private final CostRates rates;

    public ComputeCostVisitor(CostRates rates) {
        this.rates = Objects.requireNonNull(rates, "rates");
    }

    @Override
    public Double visitPipeline(Pipeline pipeline) {
        double total = 0.0;
        for (Stage stage : pipeline.stages()) {
            total += stage.accept(this);
        }
        return total;
    }

    @Override
    public Double visitStage(Stage stage) {
        double total = 0.0;
        for (StageMember member : stage.members()) {
            total += member.accept(this);
        }
        return total;
    }

    @Override
    public Double visitDataset(Dataset dataset) {
        return dataset.sizeGb() * rates.storageGbMonth();
    }

    @Override
    public Double visitModel(Model model) {
        return model.gpuHours() * rates.gpuHour();
    }

    @Override
    public Double visitTransform(Transform transform) {
        return transform.cpuHours() * rates.cpuHour();
    }

    @Override
    public Double visitEvaluator(Evaluator evaluator) {
        return evaluator.cpuHours() * rates.cpuHour();
    }
}
