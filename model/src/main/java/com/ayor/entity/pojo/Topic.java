package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Topic {

    @TableId(type = IdType.AUTO)
    private Integer topicId;

    private String title;

    private String coverUrl;

    private String description;

    private Date createTime;

    private Integer themeId;

    private Boolean isDeleted;

}
