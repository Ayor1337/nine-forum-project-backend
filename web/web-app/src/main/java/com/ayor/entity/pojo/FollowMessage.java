package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("follow_message")
public class FollowMessage {

    @TableId(type = IdType.AUTO)
    private Integer followMessageId;

    private Integer accountId;

    private Integer fromAccountId;

    private Integer threadId;

    private Integer topicId;

    private String path;

    private String title;

    private String contentSummary;

    private Date createTime;
}
