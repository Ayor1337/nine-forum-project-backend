package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "ThreadTableVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadTableVO {

    @Schema(description = "帖子ID")

    private Integer threadId;

    @Schema(description = "标题")

    private String title;

    @Schema(description = "创建时间")

    private Date createTime;

    @Schema(description = "tagName")

    private String tagName;

    @Schema(description = "用户ID")

    private Integer accountId;

    @Schema(description = "accountName")

    private String accountName;

    @Schema(description = "话题ID")

    private Integer topicId;

    @Schema(description = "topicName")

    private String topicName;

    @Schema(description = "isDeleted")

    private Boolean isDeleted;

}
