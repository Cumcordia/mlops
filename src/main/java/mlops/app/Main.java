package mlops.app;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import mlops.source.DataDriftSource;
import mlops.source.ModelMetricsSource;

public final class Main {

    private static final int DEFAULT_THREADS = 3;
    private static final int DEFAULT_EVENTS = 500;

    private Main() {
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_THREADS;
        int events = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_EVENTS;

        Clock clock = Clock.systemUTC();
        MonitoringSystem system = new MonitoringSystem(clock, MonitoringConfig.defaults(),
                SamplePipeline.create(clock.instant()));

        try (ProducerPool pool = new ProducerPool(threads)) {
            pool.runAll(List.of(
                    () -> produceModelMetrics(system.modelMetrics(), "fraud-model", "serving", events),
                    () -> produceModelMetrics(system.modelMetrics(), "churn-model", "training", events),
                    () -> produceDrift(system.dataDrift(), "transactions", "ingest", events)));
        }

        System.out.printf("threads=%d, events per producer=%d%n", threads, events);
        System.out.println("metric log entries : " + system.metricLog().size());
        System.out.println("drift alerts       : " + system.driftAlerts().alerts().size());
        System.out.println("retrain requests   : " + system.retrainRequests());
        System.out.println("unhealthy stages   : " + system.stageHealth().unhealthyStages());
        system.planBoard().plans().forEach(plan -> System.out.printf("plan: stage=%s reason=%s cost=%.2f stale=%s%n",
                plan.stageId(), plan.reason(), plan.stageCost(), plan.staleModels()));
    }

    private static void produceModelMetrics(ModelMetricsSource source, String modelId, String stageId, int events) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < events; i++) {
            source.reportAccuracy(modelId, stageId, random.nextDouble(0.70, 1.0));
            source.reportLatency(modelId, stageId, random.nextDouble(5.0, 50.0));
        }
    }

    private static void produceDrift(DataDriftSource source, String datasetId, String stageId, int events) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < events; i++) {
            source.reportDrift(datasetId, stageId, random.nextDouble(0.0, 0.35));
        }
    }
}
