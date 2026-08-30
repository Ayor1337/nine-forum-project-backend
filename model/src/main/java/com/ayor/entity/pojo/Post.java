package com.ayor.entity.pojo;

import com.ayor.typehandler.StringListTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@TableName(value = "post", autoResultMap = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @TableId(type = IdType.AUTO)
    private Integer postId;

    private String content;

    @TableField(value = "images_urls", typeHandler = StringListTypeHandler.class)
    private List<String> imagesUrls;

    private Integer accountId;

    private Date createTime;

    private Date updateTime;

    private Integer threadId;

    @TableField("reply_to")
    private Integer replyTo;

    private Integer topicId;

    private Boolean isDeleted;

}
