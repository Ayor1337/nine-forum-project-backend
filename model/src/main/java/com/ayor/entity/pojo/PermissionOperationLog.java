package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("permission_operation_log")
public class PermissionOperationLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    private Integer userId;

    private String action;

    private String targetType;

    private Long targetId;

    private String method;

    private String params;

    private Long durationMs;

    private Date createTime;
}
