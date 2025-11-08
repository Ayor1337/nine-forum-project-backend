package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 109L;

    private Integer postId;

    private String content;

    private Integer accountId;

    private Date createTime;

    private Date updateTime;

    private String avatarUrl;

    private String nickname;

    private Integer threadId;

    private Integer topicId;
}
