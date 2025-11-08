package com.ayor.entity.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicDTO {

    private Integer topicId;

    private String title;

    private String coverUrl;

    private String description;

    private Date createTime;

    private Integer themeId;

    private Boolean isDeleted;
}
