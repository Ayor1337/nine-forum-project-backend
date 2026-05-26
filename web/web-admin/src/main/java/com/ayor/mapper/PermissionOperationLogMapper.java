package com.ayor.mapper;

import com.ayor.entity.pojo.PermissionOperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionOperationLogMapper extends BaseMapper<PermissionOperationLog> {

    @Select("select * from permission_operation_log order by create_time desc, log_id desc")
    List<PermissionOperationLog> listLatest();
}
