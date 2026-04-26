package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemMessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 110L;

    private Integer systemMessageId;

    private String title;

    private String content;

    private Date createTime;

}
