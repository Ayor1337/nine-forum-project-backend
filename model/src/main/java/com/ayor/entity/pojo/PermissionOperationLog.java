package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ayor.typehandler.OperationLogParamsTypeHandler;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@TableName(value = "permission_operation_log", autoResultMap = true)
public class PermissionOperationLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    private Integer userId;

    private String action;

    private String targetType;

    private Long targetId;

    private String method;

    @TableField(typeHandler = OperationLogParamsTypeHandler.class)
    private Map<String, Object> params;

    private Long durationMs;

    private Date createTime;
}
