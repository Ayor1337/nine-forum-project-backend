package com.ayor.entity.vo;

import com.ayor.type.FeedbackStatus;
import com.ayor.type.FeedbackType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "后台意见反馈")
public class FeedbackVO {

    @Schema(description = "反馈ID")
    private Integer feedbackId;

    @Schema(description = "提交用户账号ID")
    private Integer accountId;

    @Schema(description = "反馈类型")
    private FeedbackType type;

    @Schema(description = "反馈内容")
    private String content;

    @Schema(description = "处理状态")
    private FeedbackStatus status;

    @Schema(description = "处理管理员账号ID")
    private Integer handlerAccountId;

    @Schema(description = "处理备注")
    private String handleNote;

    @Schema(description = "处理完成时间")
    private Date handledAt;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
