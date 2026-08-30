package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ConversationVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationVO {

    @Schema(description = "conversationId")

    private Integer conversationId;

    @Schema(description = "alphaAccountId")

    private Integer alphaAccountId;

    @Schema(description = "betaAccountId")

    private Integer betaAccountId;

    @Schema(description = "创建时间")

    private Date createTime;

    @Schema(description = "更新时间")

    private Date updateTime;

    @Schema(description = "isDeleted")

    private Boolean isDeleted;

    @Schema(description = "hidden")

    private Integer hidden;
}
