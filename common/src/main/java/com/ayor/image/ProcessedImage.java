package com.ayor.image;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 处理后的图片结果，包含用于存储和索引的元数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedImage {

    private byte[] bytes;

    private String originalExt;

    private String outputExt;

    private String mimeType;

    private long fileSize;

    private int width;

    private int height;

    private String sha256;
}
