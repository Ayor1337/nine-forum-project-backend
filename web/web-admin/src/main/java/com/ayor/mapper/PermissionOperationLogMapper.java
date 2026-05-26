package com.ayor.mapper;

import com.ayor.entity.pojo.PermissionOperationLog;
import com.ayor.entity.vo.PermissionOperationLogVO;
import com.ayor.typehandler.OperationLogParamsTypeHandler;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionOperationLogMapper extends BaseMapper<PermissionOperationLog> {

    @Select("""
            select l.log_id,
                   a.username,
                   l.action,
                   l.target_type,
                   l.target_id,
                   l.method,
                   l.params,
                   l.duration_ms,
                   l.create_time
            from permission_operation_log l
            left join account a on l.user_id = a.account_id
            order by l.create_time desc, l.log_id desc
            """)
    @Results({
            @Result(column = "params", property = "params", typeHandler = OperationLogParamsTypeHandler.class)
    })
    List<PermissionOperationLogVO> listLatest();
}
