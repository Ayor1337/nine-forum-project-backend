package com.ayor.entity.vo;

import com.ayor.type.PageBroadcastScopeType;
import com.ayor.type.PageBroadcastStatus;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Schema(description = "页面广播视图对象")
public class PageBroadcastVO {

    private String broadcastId;

    private PageBroadcastScopeType scopeType;

    private Integer scopeId;

    private String content;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private PageBroadcastStatus status;
}
