package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 帖子面包屑展示信息。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadBreadcrumbVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 116L;

    private String threadName;

    private String topicName;
}
