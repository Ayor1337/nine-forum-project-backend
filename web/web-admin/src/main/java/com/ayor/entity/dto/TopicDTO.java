package com.ayor.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TopicDTO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicDTO {

    @Schema(description = "话题ID")

    private Integer topicId;

    @Schema(description = "标题")

    private String title;

    @Schema(description = "coverUrl")

    private String coverUrl;

    @Schema(description = "描述")

    private String description;

    @Schema(description = "创建时间")

    private Date createTime;

    @Schema(description = "主题ID")

    private Integer themeId;

    @Schema(description = "isDeleted")

    private Boolean isDeleted;
}
