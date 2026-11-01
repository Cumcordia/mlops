package mlops.listener;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mlops.event.EventType;
import mlops.event.StageSummaryEvent;
import mlops.observer.Observer;
import mlops.observer.Subject;

public final class StageHealthListener implements Observer<StageSummaryEvent> {

    private final double minMeanAccuracy;
    private final Set<String> unhealthyStages = ConcurrentHashMap.newKeySet();

    public StageHealthListener(double minMeanAccuracy) {
        this.minMeanAccuracy = minMeanAccuracy;
    }

    public void attachTo(Subject<StageSummaryEvent> aggregator) {
        aggregator.subscribe(EventType.STAGE_SUMMARY, this);
    }

    @Override
    public void onEvent(StageSummaryEvent summary) {
        summary.meanAccuracy().ifPresent(mean -> {
            if (mean < minMeanAccuracy) {
                unhealthyStages.add(summary.stageId());
            } else {
                unhealthyStages.remove(summary.stageId());
            }
        });
    }

    public Set<String> unhealthyStages() {
        return Set.copyOf(unhealthyStages);
    }
}
