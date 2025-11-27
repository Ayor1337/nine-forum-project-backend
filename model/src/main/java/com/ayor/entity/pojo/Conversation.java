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
public class Conversation {

    @TableId(type = IdType.AUTO)
    private Integer conversationId;

    private Integer alphaAccountId;

    private Integer betaAccountId;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted;

    private Integer hidden;

}
