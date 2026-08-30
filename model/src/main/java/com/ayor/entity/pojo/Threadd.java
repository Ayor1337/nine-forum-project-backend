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


@TableName(value = "thread", autoResultMap = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Threadd {

    @TableId(type = IdType.AUTO)
    private Integer threadId;

    private String title;

    private String content;

    @TableField(value = "images_urls", typeHandler = StringListTypeHandler.class)
    private List<String> imagesUrls;

    private Date createTime;

    private Date updateTime;

    private Integer viewCount;

    private Integer postCount;

    private Integer likeCount;

    private Integer collectCount;

    private Integer topicId;

    private Integer tagId;

    private Integer accountId;

    private Boolean isMuted;

    private Boolean isSelected;

    private Boolean isDeleted;

}
