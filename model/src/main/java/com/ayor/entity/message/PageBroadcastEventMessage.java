package com.ayor.entity.message;

import com.ayor.entity.vo.PageBroadcastVO;
import com.ayor.type.PageBroadcastEventType;
import com.ayor.type.PageBroadcastScopeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "页面广播事件消息")
public class PageBroadcastEventMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private PageBroadcastEventType eventType;

    private String broadcastId;

    private PageBroadcastScopeType scopeType;

    private Integer scopeId;

    private PageBroadcastVO broadcast;
}
