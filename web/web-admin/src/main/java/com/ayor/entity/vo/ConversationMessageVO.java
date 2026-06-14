package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ConversationMessageVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationMessageVO {

    @Schema(description = "conversationMessageId")

    private Integer conversationMessageId;

    @Schema(description = "conversationId")

    private Integer conversationId;

    @Schema(description = "内容")

    private String content;

    @Schema(description = "用户ID")

    private Integer accountId;

    @Schema(description = "创建时间")

    private Date createTime;

    @Schema(description = "更新时间")

    private Date updateTime;

    @Schema(description = "isDeleted")

    private Boolean isDeleted;

    @Schema(description = "isEdit")

    private Boolean isEdit;
}
