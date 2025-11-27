package com.ayor.entity.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadTableVO {

    private Integer threadId;

    private String title;

    private Date createTime;

    private String tagName;

    private Integer accountId;

    private String accountName;

    private Integer topicId;

    private String topicName;

    private Boolean isDeleted;

}
