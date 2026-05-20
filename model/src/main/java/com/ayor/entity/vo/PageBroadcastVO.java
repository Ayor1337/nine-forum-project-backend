package com.ayor.entity.vo;

import com.ayor.type.PageBroadcastScopeType;
import com.ayor.type.PageBroadcastStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PageBroadcastVO {

    private String broadcastId;

    private PageBroadcastScopeType scopeType;

    private Integer scopeId;

    private String content;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private PageBroadcastStatus status;
}
