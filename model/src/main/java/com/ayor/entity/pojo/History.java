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
public class History {

    @TableId(type = IdType.AUTO)
    private Integer historyId;

    private Integer threadId;

    private Integer accountId;

    private Date createTime;

}
