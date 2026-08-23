package com.ayor.entity;

/**
 * Shared security limits for Base64 image uploads.
 */
public final class ImageUploadLimits {

    public static final int MAX_SOURCE_BYTES = 10 * 1024 * 1024;
    public static final int MAX_BASE64_PAYLOAD_CHARS = ((MAX_SOURCE_BYTES + 2) / 3) * 4;
    public static final int MAX_BASE64_TEXT_CHARS = MAX_BASE64_PAYLOAD_CHARS + 23;
    public static final int MAX_FILE_NAME_CHARS = 255;
    public static final int MAX_IMAGE_EDGE = 8192;
    public static final long MAX_IMAGE_PIXELS = 16_777_216L;
    public static final int MAX_IMAGE_FRAMES = 50;
    public static final int MAX_REQUEST_BODY_BYTES = 16 * 1024 * 1024;
    public static final int MAX_CONCURRENT_PROCESSING = 2;

    private ImageUploadLimits() {
    }
}
