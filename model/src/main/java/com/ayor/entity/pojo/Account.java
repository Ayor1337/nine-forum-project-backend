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
public class Account {

    @TableId(type = IdType.AUTO)
    private Integer accountId;

    private String username;

    private String password;

    private String nickname;

    private String avatarUrl;

    private String bannerUrl;

    private String bio;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private Integer roleId;

//    @TableLogic
    private boolean isDeleted;

    private String email;
}
