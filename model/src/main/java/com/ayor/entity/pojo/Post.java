package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("db_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @TableId(type = IdType.AUTO)
    private Integer postId;

    private String content;

    private Integer accountId;

    private Date createTime;

    private Date updateTime;

    private Integer threadId;

    private Integer topicId;

    private Boolean isDeleted;

}
