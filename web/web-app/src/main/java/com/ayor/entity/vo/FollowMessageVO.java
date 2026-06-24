package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowMessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 119L;

    private Integer followMessageId;

    private Integer fromAccountId;

    private String fromUsername;

    private String fromNickname;

    private String fromAvatarUrl;

    private Integer threadId;

    private String title;

    private String contentSummary;

    private String path;

    private Date createTime;
}
