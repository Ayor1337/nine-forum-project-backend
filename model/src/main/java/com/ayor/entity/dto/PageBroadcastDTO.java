package com.ayor.entity.dto;

import com.ayor.type.PageBroadcastScopeType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PageBroadcastDTO {

    private PageBroadcastScopeType scopeType;

    private Integer scopeId;

    private String content;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
