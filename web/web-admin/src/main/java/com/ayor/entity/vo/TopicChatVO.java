package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicChatVO {

    private Integer topicChatId;

    private Integer topicId;

    private Integer accountId;

    private String content;

    private Date createTime;
}
