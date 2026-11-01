package mlops.event;

import java.time.Instant;
import java.util.OptionalDouble;

public record StageSummaryEvent(Instant timestamp, String stageId, long eventCount,
                                long accuracySamples, double accuracySum, double maxDrift) implements Event {

    @Override
    public EventType type() {
        return EventType.STAGE_SUMMARY;
    }

    public OptionalDouble meanAccuracy() {
        return accuracySamples == 0 ? OptionalDouble.empty() : OptionalDouble.of(accuracySum / accuracySamples);
    }
}
