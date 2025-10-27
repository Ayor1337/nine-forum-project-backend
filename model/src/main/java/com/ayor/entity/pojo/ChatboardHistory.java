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
@TableName("db_chatboard_history")
public class ChatboardHistory {

    @TableId(type = IdType.AUTO)
    private Integer chatboardHistoryId;

    private Integer accountId;

    private String username;

    private Integer topicId;

    private String content;

    private Date createTime;
}
