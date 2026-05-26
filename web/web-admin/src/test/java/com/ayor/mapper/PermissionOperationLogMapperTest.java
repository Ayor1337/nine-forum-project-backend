package com.ayor.mapper;

import com.ayor.entity.pojo.PermissionOperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionOperationLogMapperTest {

    @Test
    void mapperProvidesBasicLogQuery() throws NoSuchMethodException {
        assertThat(BaseMapper.class).isAssignableFrom(PermissionOperationLogMapper.class);

        Method method = PermissionOperationLogMapper.class.getMethod("listLatest");
        assertThat(method.getReturnType()).isEqualTo(List.class);

        Select select = method.getAnnotation(Select.class);
        assertThat(select).isNotNull();
        assertThat(select.value()).containsExactly(
                "select * from permission_operation_log order by create_time desc, log_id desc"
        );
    }
}
