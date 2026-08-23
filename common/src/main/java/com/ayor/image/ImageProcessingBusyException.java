package com.ayor.image;

/**
 * Indicates that all image-processing slots are currently occupied.
 */
public class ImageProcessingBusyException extends ImageUploadException {

    public ImageProcessingBusyException() {
        super("图片处理繁忙，请稍后重试");
    }
}
