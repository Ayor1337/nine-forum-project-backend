package com.ayor.image;

/**
 * Indicates that uploaded bytes do not satisfy the image security contract.
 */
public class ImageValidationException extends ImageUploadException {

    public ImageValidationException(String message) {
        super(message);
    }

    public ImageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
