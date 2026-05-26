package com.ayor.mapper;

import com.ayor.entity.vo.PermissionOperationLogVO;
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
        assertThat(method.getGenericReturnType().getTypeName())
                .contains(PermissionOperationLogVO.class.getName());

        Select select = method.getAnnotation(Select.class);
        assertThat(select).isNotNull();
        String sql = String.join(" ", select.value());
        assertThat(sql).contains("a.username");
        assertThat(sql).doesNotContain("l.user_id as userId");
    }
}
