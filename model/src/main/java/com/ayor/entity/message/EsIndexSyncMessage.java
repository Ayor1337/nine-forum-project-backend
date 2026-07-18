package com.ayor.entity.message;

import com.ayor.type.EsIndexEntityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ES 索引同步消息, 消费端以 MySQL 当前状态为准重建对应文档")
public class EsIndexSyncMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private EsIndexEntityType entityType;

    private Integer entityId;
}
