package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TopicStatVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicStatVO {

    @Schema(description = "topicStatId")

    private Integer topicStatId;

    @Schema(description = "话题ID")

    private Integer topicId;

    @Schema(description = "threadCount")

    private Integer threadCount;

    @Schema(description = "viewCount")

    private Integer viewCount;
}
