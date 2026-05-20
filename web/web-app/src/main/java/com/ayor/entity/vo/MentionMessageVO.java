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
public class MentionMessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 117L;

    private Integer mentionMessageId;

    private Integer fromAccountId;

    private String fromUsername;

    private String fromNickname;

    private String fromAvatarUrl;

    private String contentSummary;

    private String path;

    private String sourceType;

    private Date createTime;
}
