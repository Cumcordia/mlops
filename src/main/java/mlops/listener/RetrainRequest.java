package mlops.listener;

import java.time.Instant;

public record RetrainRequest(String stageId, String sourceId, Reason reason, double value, Instant requestedAt) {

    public enum Reason {
        ACCURACY_DROP,
        DATA_DRIFT
    }
}
