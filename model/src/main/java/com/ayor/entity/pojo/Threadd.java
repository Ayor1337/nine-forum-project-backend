package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@TableName("db_thread")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Threadd {

    @TableId(type = IdType.AUTO)
    private Integer threadId;

    private String title;

    private String content;

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

    @TableLogic
    private Boolean isDeleted;

    private Boolean isAnnouncement;

}
