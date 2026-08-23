package com.ayor.image;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageProcessingLimiterTest {

    @Test
    void shouldAllowExactlyTwoConcurrentActionsByDefault() throws Exception {
        ImageProcessingLimiter limiter = new ImageProcessingLimiter();
        CountDownLatch entered = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(() -> limiter.execute(() -> occupy(entered, release, "first")));
            Future<String> second = executor.submit(() -> limiter.execute(() -> occupy(entered, release, "second")));
            entered.await();

            assertThrows(ImageProcessingBusyException.class, () -> limiter.execute(() -> "third"));
            release.countDown();
            assertEquals("first", first.get());
            assertEquals("second", second.get());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldFailFastWhenAllSlotsAreOccupiedAndReleaseAfterSuccess() throws Exception {
        ImageProcessingLimiter limiter = new ImageProcessingLimiter(1);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> running = executor.submit(() -> limiter.execute(() -> {
                entered.countDown();
                await(release);
                return "done";
            }));
            entered.await();

            assertThrows(ImageProcessingBusyException.class, () -> limiter.execute(() -> "rejected"));
            release.countDown();
            assertEquals("done", running.get());
            assertEquals("again", limiter.execute(() -> "again"));
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldReleaseSlotAfterActionThrows() {
        ImageProcessingLimiter limiter = new ImageProcessingLimiter(1);

        assertThrows(IllegalStateException.class, () -> limiter.execute(() -> {
            throw new IllegalStateException("failed");
        }));

        assertEquals("recovered", limiter.execute(() -> "recovered"));
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private static String occupy(CountDownLatch entered, CountDownLatch release, String result) {
        entered.countDown();
        await(release);
        return result;
    }
}
