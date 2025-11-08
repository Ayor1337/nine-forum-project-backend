package com.ayor.entity.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadDTO {

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

    private Boolean isDeleted;

    private Boolean isAnnouncement;
}
