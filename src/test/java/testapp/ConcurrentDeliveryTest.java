package testapp;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import mlops.app.MonitoringConfig;
import mlops.app.MonitoringSystem;
import mlops.app.ProducerPool;
import static mlops.event.EventType.ACCURACY_REPORTED;
import static mlops.event.EventType.STAGE_SUMMARY;
import mlops.event.MetricEvent;
import mlops.event.StageSummaryEvent;
import mlops.listener.RetrainRequest;
import mlops.observer.Observer;
import mlops.source.ModelMetricsSource;

class ConcurrentDeliveryTest {

    private static final int PRODUCERS = 4;
    private static final int EVENTS_PER_PRODUCER = 1_000;
    private static final int TOTAL = PRODUCERS * EVENTS_PER_PRODUCER;

    private static List<Runnable> producers(ModelMetricsSource source, double accuracy) {
        return IntStream.range(0, PRODUCERS)
                .mapToObj(producer -> (Runnable) () -> {
                    for (int i = 0; i < EVENTS_PER_PRODUCER; i++) {
                        source.reportAccuracy("model-" + producer, "shared-stage", accuracy);
                    }
                })
                .toList();
    }

    @Test
    void fourProducersDeliverEveryEventToEveryListenerExactlyOnce() throws InterruptedException {
        MonitoringSystem system = new MonitoringSystem(Clock.systemUTC(), MonitoringConfig.defaults());
        List<StageSummaryEvent> summaries = new CopyOnWriteArrayList<>();
        system.stageAggregator().subscribe(STAGE_SUMMARY, summaries::add);

        try (ProducerPool pool = new ProducerPool(PRODUCERS)) {
            pool.runAll(producers(system.modelMetrics(), 0.95));
        }

        assertEquals(TOTAL, system.metricLog().size());
        assertEquals(TOTAL, summaries.size());
        long maxCount = summaries.stream().mapToLong(StageSummaryEvent::eventCount).max().orElseThrow();
        assertEquals(TOTAL, maxCount);
        assertEquals(4, system.dashboard().trackedSeries());
        assertTrue(system.retrainRequests().isEmpty());
    }

    @Test
    void contendedStageGetsExactlyOneRetrainRequest() throws InterruptedException {
        MonitoringSystem system = new MonitoringSystem(Clock.systemUTC(), MonitoringConfig.defaults());

        try (ProducerPool pool = new ProducerPool(PRODUCERS)) {
            pool.runAll(producers(system.modelMetrics(), 0.10));
        }

        List<RetrainRequest> requests = system.retrainRequests();
        assertEquals(1, requests.size());
        assertEquals("shared-stage", requests.get(0).stageId());
        assertEquals(TOTAL, system.metricLog().size());
    }

    @Test
    void unsubscribingWhilePublishingNeverLosesEventsForStableListeners() throws InterruptedException {
        ModelMetricsSource source = new ModelMetricsSource(Clock.systemUTC());
        AtomicInteger stableCount = new AtomicInteger();
        source.subscribe(ACCURACY_REPORTED, event -> stableCount.incrementAndGet());

        Observer<MetricEvent> flapping = event -> { };
        AtomicBoolean done = new AtomicBoolean();
        Thread churn = new Thread(() -> {
            while (!done.get()) {
                source.subscribe(ACCURACY_REPORTED, flapping);
                source.unsubscribe(ACCURACY_REPORTED, flapping);
            }
        });
        churn.start();

        try (ProducerPool pool = new ProducerPool(PRODUCERS)) {
            pool.runAll(producers(source, 0.95));
        } finally {
            done.set(true);
            churn.join();
        }

        assertEquals(TOTAL, stableCount.get());
    }

    @Test
    void poolRunsTasksOnlyOnTheConfiguredNumberOfThreads() throws InterruptedException {
        Set<String> threadNames = ConcurrentHashMap.newKeySet();
        Runnable task = () -> threadNames.add(Thread.currentThread().getName());

        try (ProducerPool pool = new ProducerPool(1)) {
            pool.runAll(List.of(task, task, task, task));
        }

        assertEquals(1, threadNames.size());
    }

    @Test
    void poolReportsAFailedProducerInsteadOfLosingIt() throws InterruptedException {
        try (ProducerPool pool = new ProducerPool(2)) {
            Runnable failing = () -> {
                throw new IllegalArgumentException("bad producer");
            };
            assertThrows(IllegalStateException.class, () -> pool.runAll(List.of(failing)));
        }
    }
}
