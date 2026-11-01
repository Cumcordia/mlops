package mlops.app;

import java.time.Clock;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import mlops.event.MetricEvent;
import mlops.listener.DashboardListener;
import mlops.listener.DriftAlertListener;
import mlops.listener.MetricLogListener;
import mlops.listener.RetrainRequest;
import mlops.listener.RetrainingTriggerListener;
import mlops.listener.StageAggregator;
import mlops.listener.StageHealthListener;
import mlops.model.Pipeline;
import mlops.observer.Subject;
import mlops.planning.RetrainPlanBoard;
import mlops.planning.RetrainPlanner;
import mlops.source.DataDriftSource;
import mlops.source.ModelMetricsSource;

public final class MonitoringSystem {

    private final ModelMetricsSource modelMetrics;
    private final DataDriftSource dataDrift;
    private final DriftAlertListener driftAlerts;
    private final RetrainingTriggerListener retrainingTrigger;
    private final DashboardListener dashboard;
    private final MetricLogListener metricLog;
    private final StageAggregator stageAggregator;
    private final StageHealthListener stageHealth;
    private final RetrainPlanner retrainPlanner;
    private final RetrainPlanBoard planBoard;
    private final Queue<RetrainRequest> retrainRequests = new ConcurrentLinkedQueue<>();

    public MonitoringSystem(Clock clock, MonitoringConfig config, Pipeline pipeline) {
        modelMetrics = new ModelMetricsSource(clock);
        dataDrift = new DataDriftSource(clock);
        driftAlerts = new DriftAlertListener(config.driftAlertThreshold());
        retrainPlanner = new RetrainPlanner(pipeline, clock, config.costRates(), config.modelMaxAge());
        planBoard = new RetrainPlanBoard();
        retrainingTrigger = new RetrainingTriggerListener(config.retrainMinAccuracy(), config.retrainMaxDrift(),
                request -> {
                    retrainRequests.add(request);
                    retrainPlanner.plan(request);
                });
        dashboard = new DashboardListener();
        metricLog = new MetricLogListener();
        stageAggregator = new StageAggregator(clock);
        stageHealth = new StageHealthListener(config.healthyStageAccuracy());

        for (Subject<MetricEvent> source : List.<Subject<MetricEvent>>of(modelMetrics, dataDrift)) {
            driftAlerts.attachTo(source);
            retrainingTrigger.attachTo(source);
            dashboard.attachTo(source);
            metricLog.attachTo(source);
            stageAggregator.attachTo(source);
        }
        stageHealth.attachTo(stageAggregator);
        planBoard.attachTo(retrainPlanner);
    }

    public ModelMetricsSource modelMetrics() {
        return modelMetrics;
    }

    public DataDriftSource dataDrift() {
        return dataDrift;
    }

    public DriftAlertListener driftAlerts() {
        return driftAlerts;
    }

    public RetrainingTriggerListener retrainingTrigger() {
        return retrainingTrigger;
    }

    public DashboardListener dashboard() {
        return dashboard;
    }

    public MetricLogListener metricLog() {
        return metricLog;
    }

    public StageAggregator stageAggregator() {
        return stageAggregator;
    }

    public StageHealthListener stageHealth() {
        return stageHealth;
    }

    public RetrainPlanner retrainPlanner() {
        return retrainPlanner;
    }

    public RetrainPlanBoard planBoard() {
        return planBoard;
    }

    public List<RetrainRequest> retrainRequests() {
        return List.copyOf(retrainRequests);
    }
}
