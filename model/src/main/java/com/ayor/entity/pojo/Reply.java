package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("db_reply")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reply {

    private Integer replyId;

    private String accountId;

    private String content;

    private String threadId;

    private Date createTime;

    private Date updateTime;

}
