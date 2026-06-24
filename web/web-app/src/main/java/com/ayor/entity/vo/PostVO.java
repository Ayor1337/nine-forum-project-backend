package com.ayor.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

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

    @JsonProperty("reply_to")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PostVO replyTo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer editCount;
}
