package mlops.app;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class ProducerPool implements AutoCloseable {

    private static final long TERMINATION_TIMEOUT_SECONDS = 10;

    private final ExecutorService executor;

    public ProducerPool(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public void runAll(List<Runnable> tasks) throws InterruptedException {
        List<Callable<Void>> callables = tasks.stream().<Callable<Void>>map(task -> () -> {
            task.run();
            return null;
        }).toList();
        for (Future<Void> result : executor.invokeAll(callables)) {
            try {
                result.get();
            } catch (ExecutionException e) {
                throw new IllegalStateException("Producer failed", e.getCause());
            }
        }
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                throw new IllegalStateException("Producer threads did not terminate in time");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
