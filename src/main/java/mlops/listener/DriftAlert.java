package mlops.listener;

import java.time.Instant;

public record DriftAlert(String sourceId, String stageId, double drift, Instant raisedAt) {
}
