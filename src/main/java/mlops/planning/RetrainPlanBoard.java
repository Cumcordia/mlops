package mlops.planning;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import mlops.event.EventType;
import mlops.observer.Observer;
import mlops.observer.Subject;

public final class RetrainPlanBoard implements Observer<RetrainPlanEvent> {

    private final Queue<RetrainPlanEvent> plans = new ConcurrentLinkedQueue<>();

    public void attachTo(Subject<RetrainPlanEvent> planner) {
        planner.subscribe(EventType.RETRAIN_PLANNED, this);
    }

    @Override
    public void onEvent(RetrainPlanEvent plan) {
        plans.add(plan);
    }

    public List<RetrainPlanEvent> plans() {
        return List.copyOf(plans);
    }
}
