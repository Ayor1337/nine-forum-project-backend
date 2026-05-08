package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 内容与图片资源的引用关系实体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("content_image_ref")
public class ContentImageRef {

    @TableId(type = IdType.AUTO)
    private Integer refId;

    private Integer assetId;

    private String contentType;

    private Integer contentId;

    private Date createTime;
}
