package com.ayor.entity.pojo;

import com.ayor.type.FeedbackStatus;
import com.ayor.type.FeedbackType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Integer feedbackId;

    private Integer accountId;

    private FeedbackType type;

    private String content;

    private FeedbackStatus status;

    private Integer handlerAccountId;

    private String handleNote;

    private Date handledAt;

    private Date createTime;

    private Date updateTime;
}
