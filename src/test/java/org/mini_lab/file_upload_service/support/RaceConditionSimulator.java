package org.mini_lab.file_upload_service.support;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class RaceConditionSimulator implements AutoCloseable {

    private static final Duration READY_TIMEOUT =
            Duration.ofSeconds(5);

    private static final Duration EXECUTION_TIMEOUT =
            Duration.ofSeconds(10);

    private final int concurrentRequestCount;
    private final ExecutorService executorService;
    private final CountDownLatch readyLatch;
    private final CountDownLatch startLatch;

    private RaceConditionSimulator(int concurrentRequestCount) {
        if (concurrentRequestCount <= 0) {
            throw new IllegalArgumentException(
                    "concurrentRequestCount must be greater than zero"
            );
        }

        this.concurrentRequestCount = concurrentRequestCount;
        this.executorService =
                Executors.newFixedThreadPool(concurrentRequestCount);

        this.readyLatch =
                new CountDownLatch(concurrentRequestCount);

        this.startLatch =
                new CountDownLatch(1);
    }

    public static RaceConditionSimulator getRaceConditionSimulator(int concurrentRequestCount) {
        return new RaceConditionSimulator(concurrentRequestCount);
    }

    public <T> List<T> execute(Callable<T> task)
            throws InterruptedException,
            ExecutionException,
            TimeoutException {

        List<CompletableFuture<T>> futures =
                createConcurrentRequests(task);

        boolean allWorkersReady = readyLatch.await(
                READY_TIMEOUT.toMillis(),
                TimeUnit.MILLISECONDS
        );

        if (!allWorkersReady) {
            throw new IllegalStateException(
                    "Not all concurrent workers became ready"
            );
        }

        startLatch.countDown();

        CompletableFuture.allOf(
                futures.toArray(CompletableFuture[]::new)
        ).get(
                EXECUTION_TIMEOUT.toMillis(),
                TimeUnit.MILLISECONDS
        );

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private <T> List<CompletableFuture<T>> createConcurrentRequests(
            Callable<T> task
    ) {
        return IntStream.range(0, concurrentRequestCount)
                .mapToObj(index ->
                        CompletableFuture.supplyAsync(
                                () -> executeTask(task),
                                executorService
                        )
                )
                .toList();
    }

    private <T> T executeTask(Callable<T> task) {
        readyLatch.countDown();
        awaitStartSignal();

        try {
            return task.call();
        } catch (Exception exception) {
            throw new CompletionException(
                    "Concurrent task execution failed",
                    exception
            );
        }
    }

    private void awaitStartSignal() {
        try {
            startLatch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new CompletionException(
                    "Concurrent task was interrupted",
                    exception
            );
        }
    }

    @Override
    public void close() {
        executorService.shutdownNow();

        try {
            if (!executorService.awaitTermination(
                    5,
                    TimeUnit.SECONDS
            )) {
                throw new IllegalStateException(
                        "Executor did not terminate"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while shutting down executor",
                    exception
            );
        }
    }
}
