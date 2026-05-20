package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagVO {

    private Integer tagId;

    private String tag;

    private Date createTime;

    private Integer topicId;
}
