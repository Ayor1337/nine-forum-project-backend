package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TagVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagVO {

    @Schema(description = "标签ID")

    private Integer tagId;

    @Schema(description = "tag")

    private String tag;

    @Schema(description = "创建时间")

    private Date createTime;

    @Schema(description = "话题ID")

    private Integer topicId;
}
