package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicVO implements Serializable  {

    @Serial
    private static final long serialVersionUID = 115L;

    private Integer topicId;

    private String title;

    private String coverUrl;

    private Integer threadCount;

    private Integer viewCount;

    private String description;

    private String createTime;

    private Integer themeId;

}
