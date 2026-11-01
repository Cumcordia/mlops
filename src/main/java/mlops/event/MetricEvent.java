package mlops.event;

import java.time.Instant;
import java.util.Objects;

public record MetricEvent(EventType type, Instant timestamp, String sourceId, String stageId, double value)
        implements Event {

    public MetricEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(timestamp, "timestamp");
        if (!type.isMetric()) {
            throw new IllegalArgumentException("Not a metric event type: " + type);
        }
        requireText(sourceId, "sourceId");
        requireText(stageId, "stageId");
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value must be finite: " + value);
        }
    }

    private static void requireText(String text, String name) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
