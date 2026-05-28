package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionOperationLogVO {

    private Long logId;

    private Integer userId;

    private String username;

    private String action;

    private String targetType;

    private Long targetId;

    private String method;

    private Map<String, Object> params;

    private Long durationMs;

    private Date createTime;
}
