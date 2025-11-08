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
public class TagVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 111L;

    private Integer tagId;

    private String tag;

    private Integer topicId;

    private Date createTime;

}
