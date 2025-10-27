package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatboardHistoryVO {

    private Integer chatboardHistoryId;

    private Integer accountId;

    private String username;

    private Integer topicId;

    private String content;

    private Date createTime;

}
