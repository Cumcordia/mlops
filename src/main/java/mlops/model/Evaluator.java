package mlops.model;

import java.util.List;

public record Evaluator(String id, double cpuHours, List<String> inputs) implements StageMember {

    public Evaluator {
        Checks.requireText(id, "id");
        Checks.requireNonNegative(cpuHours, "cpuHours");
        inputs = List.copyOf(inputs);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitEvaluator(this);
    }
}
