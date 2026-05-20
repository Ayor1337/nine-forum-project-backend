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
@TableName("mention_message")
public class MentionMessage {

    @TableId(type = IdType.AUTO)
    private Integer mentionMessageId;

    private Integer accountId;

    private Integer fromAccountId;

    private String sourceType;

    private Integer sourceId;

    private Integer threadId;

    private String path;

    private String contentSummary;

    private Date createTime;
}
