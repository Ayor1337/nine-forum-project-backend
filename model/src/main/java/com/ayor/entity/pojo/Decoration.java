package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("decoration")
public class Decoration {

    @TableId(type = IdType.AUTO)
    private Integer decorationId;

    private String decorationKey;

    private String name;

    private String description;

    private String type;

    private Integer status;

    /**
     * 编辑中的结构化配置（JSON 文本）
     */
    private String draftConfig;

    /**
     * 已发布配置（JSON 文本，用户端只读此字段）
     */
    private String publishedConfig;

    @Version
    private Integer version;

    private Date publishedAt;

    private Integer createdBy;

    private Boolean isDeleted;

    private Date createTime;

    private Date updateTime;
}
