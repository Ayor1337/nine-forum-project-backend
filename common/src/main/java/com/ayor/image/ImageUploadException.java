package com.ayor.image;

/**
 * Base type for expected image-upload rejections.
 */
public abstract class ImageUploadException extends RuntimeException {

    protected ImageUploadException(String message) {
        super(message);
    }

    protected ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
