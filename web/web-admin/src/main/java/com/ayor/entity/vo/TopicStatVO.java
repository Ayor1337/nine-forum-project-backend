package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicStatVO {

    private Integer topicStatId;

    private Integer topicId;

    private Integer threadCount;

    private Integer viewCount;
}
