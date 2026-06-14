package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "HistoryVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryVO {

    @Schema(description = "historyId")

    private Integer historyId;

    @Schema(description = "帖子ID")

    private Integer threadId;

    @Schema(description = "用户ID")

    private Integer accountId;

    @Schema(description = "创建时间")

    private Date createTime;
}
