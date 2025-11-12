package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatboardHistoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 104L;

    private Integer chatboardHistoryId;

    private Integer accountId;

    private Integer topicId;

    private String content;

    private Date createTime;

}
