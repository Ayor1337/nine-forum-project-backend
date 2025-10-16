package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostVO {
    private Integer postId;

    private String content;

    private Integer accountId;

    private Date createTime;

    private Date updateTime;

    private String avatarUrl;

    private String nickname;
}
