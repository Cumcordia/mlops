package mlops.model;

import java.util.List;

public record Transform(String id, double cpuHours, List<String> inputs) implements StageMember {

    public Transform {
        Checks.requireText(id, "id");
        Checks.requireNonNegative(cpuHours, "cpuHours");
        inputs = List.copyOf(inputs);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitTransform(this);
    }
}
