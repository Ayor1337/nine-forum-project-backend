package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 102L;

    private Integer threadId;

    private Integer topicId;

    private String title;

}
