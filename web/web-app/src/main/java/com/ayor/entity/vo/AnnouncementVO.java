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
public class AnnouncementVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 102L;

    private Integer announcementId;

    private Integer threadId;

    private Integer topicId;

    private String title;

    private Boolean isGlobal;

    private Date createTime;

}
