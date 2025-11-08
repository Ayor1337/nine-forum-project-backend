package com.ayor.entity.app.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 114L;

    private Integer threadId;

    private String title;

    private List<String> imageUrls;

    private String content;

    private Date createTime;

    private Integer viewCount;

    private Integer postCount;

    private Integer likeCount;

    private Integer collectCount;

    private String tagName;

    private Integer accountId;

    private Integer topicId;

    private String accountName;

    private String avatarUrl;

    private TagVO Tag;

}
