package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ChatboardHistoryVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatboardHistoryVO {

    @Schema(description = "chatboardHistoryId")

    private Integer chatboardHistoryId;

    @Schema(description = "用户ID")

    private Integer accountId;

    @Schema(description = "话题ID")

    private Integer topicId;

    @Schema(description = "内容")

    private String content;

    @Schema(description = "创建时间")

    private Date createTime;
}
