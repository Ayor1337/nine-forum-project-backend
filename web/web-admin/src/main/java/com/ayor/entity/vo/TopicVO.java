package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicVO {

    private Integer topicId;

    private String title;

    private String coverUrl;

    private String description;

    private Date createTime;

    private Boolean isDeleted;

}
