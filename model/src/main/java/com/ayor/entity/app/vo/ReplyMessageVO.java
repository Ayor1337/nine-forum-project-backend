package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyMessageVO {

    private Integer postId;

    private String content;

    private Date createTime;

    private Integer threadId;

    private Integer topicId;

    private String threadTitle;

    private String nickname;

}
