package com.ayor.entity.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("db_tag")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag {

    @TableId(type = IdType.AUTO)
    private Integer tagId;

    private String tag;

    private Date createTime;

    private Integer topicId;
}
