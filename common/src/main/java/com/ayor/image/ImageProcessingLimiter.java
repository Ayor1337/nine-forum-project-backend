package com.ayor.image;

import com.ayor.entity.ImageUploadLimits;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * Applies a per-JVM, non-blocking concurrency bound to image processing.
 */
@Component
public class ImageProcessingLimiter {

    private final Semaphore permits;

    public ImageProcessingLimiter() {
        this(ImageUploadLimits.MAX_CONCURRENT_PROCESSING);
    }

    ImageProcessingLimiter(int maximumConcurrency) {
        if (maximumConcurrency < 1) {
            throw new IllegalArgumentException("图片处理并发数必须大于零");
        }
        this.permits = new Semaphore(maximumConcurrency, true);
    }

    public <T> T execute(Supplier<T> action) {
        if (!permits.tryAcquire()) {
            throw new ImageProcessingBusyException();
        }
        try {
            return action.get();
        } finally {
            permits.release();
        }
    }
}
