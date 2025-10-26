package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("db_topic_chat")
public class TopicChat {

    @TableId(type = IdType.AUTO)
    private Integer topicChatId;

    private Integer topicId;

    private Integer accountId;

    private String content;

    private Date createTime;

}
