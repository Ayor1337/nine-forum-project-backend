package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicVO {

    private Integer topicId;

    private String title;

    private String coverUrl;

    private Integer threadCount;

    private Integer viewCount;

    private String description;

    private String createTime;

    private Integer themeId;

}
