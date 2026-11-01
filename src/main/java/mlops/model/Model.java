package mlops.model;

import java.time.Instant;
import java.util.List;

public record Model(String id, String version, double gpuHours, Instant lastTrainedAt, List<String> inputs)
        implements StageMember {

    public Model {
        Checks.requireText(id, "id");
        Checks.requireText(version, "version");
        Checks.requireNonNegative(gpuHours, "gpuHours");
        java.util.Objects.requireNonNull(lastTrainedAt, "lastTrainedAt");
        inputs = List.copyOf(inputs);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitModel(this);
    }
}
