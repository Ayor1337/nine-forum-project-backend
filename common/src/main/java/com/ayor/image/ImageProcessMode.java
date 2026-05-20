package com.ayor.image;

/**
 * 图片处理模式。
 */
public enum ImageProcessMode {
    /**
     * 表情包模式，限制尺寸并输出 WebP。
     */
    STICKER,
    /**
     * 正文图片模式，按原格式直传。
     */
    IMAGE
}
