package mlops.listener;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import mlops.event.EventType;
import mlops.event.MetricEvent;
import mlops.observer.Observer;
import mlops.observer.Subject;

public final class RetrainingTriggerListener implements Observer<MetricEvent> {

    private final double minAccuracy;
    private final double maxDrift;
    private final Consumer<RetrainRequest> requestSink;
    private final Set<String> stagesInRetraining = ConcurrentHashMap.newKeySet();

    public RetrainingTriggerListener(double minAccuracy, double maxDrift, Consumer<RetrainRequest> requestSink) {
        this.minAccuracy = minAccuracy;
        this.maxDrift = maxDrift;
        this.requestSink = requestSink;
    }

    public void attachTo(Subject<MetricEvent> source) {
        source.subscribe(List.of(EventType.ACCURACY_REPORTED, EventType.DRIFT_REPORTED), this);
    }

    @Override
    public void onEvent(MetricEvent event) {
        Optional<RetrainRequest.Reason> reason = reasonFor(event);
        if (reason.isPresent() && stagesInRetraining.add(event.stageId())) {
            send(new RetrainRequest(event.stageId(), event.sourceId(), reason.get(), event.value(), event.timestamp()));
        }
    }

    public void markRetrained(String stageId) {
        stagesInRetraining.remove(stageId);
    }

    private Optional<RetrainRequest.Reason> reasonFor(MetricEvent event) {
        if (event.type() == EventType.ACCURACY_REPORTED && event.value() < minAccuracy) {
            return Optional.of(RetrainRequest.Reason.ACCURACY_DROP);
        }
        if (event.type() == EventType.DRIFT_REPORTED && event.value() > maxDrift) {
            return Optional.of(RetrainRequest.Reason.DATA_DRIFT);
        }
        return Optional.empty();
    }

    private void send(RetrainRequest request) {
        try {
            requestSink.accept(request);
        } catch (RuntimeException e) {
            stagesInRetraining.remove(request.stageId());
            throw e;
        }
    }
}
