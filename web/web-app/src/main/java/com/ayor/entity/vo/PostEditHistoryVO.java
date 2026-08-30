package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostEditHistoryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 117L;

    private Integer historyId;

    private Integer postId;

    private Date editTime;

    private Integer editorId;

    private String editorName;

    private String editorAvatar;

}
