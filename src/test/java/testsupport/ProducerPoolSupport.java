package testsupport;

import java.util.List;
import java.util.stream.IntStream;
import mlops.app.ProducerPool;

public final class ProducerPoolSupport {

    private ProducerPoolSupport() {
    }

    public static void runConcurrently(int threads, int repetitions, Runnable action) throws InterruptedException {
        List<Runnable> producers = IntStream.range(0, threads)
                .mapToObj(producer -> (Runnable) () -> {
                    for (int i = 0; i < repetitions; i++) {
                        action.run();
                    }
                })
                .toList();
        try (ProducerPool pool = new ProducerPool(threads)) {
            pool.runAll(producers);
        }
    }
}
