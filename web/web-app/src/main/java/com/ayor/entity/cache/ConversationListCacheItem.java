package com.ayor.entity.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationListCacheItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 118L;

    private Integer conversationId;

    private Integer viewerAccountId;

    private Integer partnerAccountId;

    private Date updateTime;

    private Boolean pinned;

    public ConversationListCacheItem(Integer conversationId, Integer partnerAccountId, Date updateTime) {
        this.conversationId = conversationId;
        this.partnerAccountId = partnerAccountId;
        this.updateTime = updateTime;
        this.pinned = false;
    }
}
