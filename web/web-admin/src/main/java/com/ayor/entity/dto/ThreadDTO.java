package com.ayor.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ThreadDTO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadDTO {

    @Schema(description = "帖子ID")

    private Integer threadId;

    @Schema(description = "标题")

    private String title;

    @Schema(description = "内容")

    private String content;

    @Schema(description = "创建时间")

    private Date createTime;

    @Schema(description = "更新时间")

    private Date updateTime;

    @Schema(description = "viewCount")

    private Integer viewCount;

    @Schema(description = "postCount")

    private Integer postCount;

    @Schema(description = "likeCount")

    private Integer likeCount;

    @Schema(description = "collectCount")

    private Integer collectCount;

    @Schema(description = "话题ID")

    private Integer topicId;

    @Schema(description = "标签ID")

    private Integer tagId;

    @Schema(description = "用户ID")

    private Integer accountId;

    @Schema(description = "isMuted")

    private Boolean isMuted;

    @Schema(description = "isSelected")

    private Boolean isSelected;

    @Schema(description = "isDeleted")

    private Boolean isDeleted;
}
