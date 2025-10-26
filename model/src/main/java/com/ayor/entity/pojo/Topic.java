package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("db_topic")
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

    @TableLogic
    private Boolean isDeleted;

}
