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
public class DecorationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 143L;

    private Integer decorationId;

    private String decorationKey;

    private String name;

    private String description;

    private String type;

    private Integer status;

    private String draftConfig;

    private String publishedConfig;

    private Date publishedAt;

    private Date createTime;

    private Date updateTime;
}
