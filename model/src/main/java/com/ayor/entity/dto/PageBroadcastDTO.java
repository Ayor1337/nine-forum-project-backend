package com.ayor.entity.dto;

import com.ayor.type.PageBroadcastScopeType;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Schema(description = "页面广播请求对象")
public class PageBroadcastDTO {

    private PageBroadcastScopeType scopeType;

    private Integer scopeId;

    private String content;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
