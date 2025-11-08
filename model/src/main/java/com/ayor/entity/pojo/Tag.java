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
public class Tag {

    @TableId(type = IdType.AUTO)
    private Integer tagId;

    private String tag;

    private Date createTime;

    private Integer topicId;
}
