package com.ayor.image;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 已上传到对象存储的图片结果，额外携带对象路径与可访问 URL。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StoredStaticImage extends ProcessedStaticImage {

    private String objectName;

    private String url;

    public StoredStaticImage(ProcessedStaticImage image, String objectName, String url) {
        super(
                image.getBytes(),
                image.getOriginalExt(),
                image.getOutputExt(),
                image.getMimeType(),
                image.getFileSize(),
                image.getWidth(),
                image.getHeight(),
                image.getSha256()
        );
        this.objectName = objectName;
        this.url = url;
    }
}
