package mlops.planning;

import java.time.Instant;
import java.util.List;
import mlops.event.Event;
import mlops.event.EventType;
import mlops.listener.RetrainRequest;

public record RetrainPlanEvent(Instant timestamp, String stageId, RetrainRequest.Reason reason, double stageCost,
                               List<String> staleModels, List<String> lineage) implements Event {

    public RetrainPlanEvent {
        staleModels = List.copyOf(staleModels);
        lineage = List.copyOf(lineage);
    }

    @Override
    public EventType type() {
        return EventType.RETRAIN_PLANNED;
    }
}
