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

        Method countMethod = PermissionOperationLogMapper.class.getMethod(
                "countPermissionOperationLogs",
                String.class,
                String.class,
                String.class,
                Long.class);
        assertThat(countMethod.getReturnType()).isEqualTo(Long.class);
        assertThat(assertSql(countMethod))
                .contains("COUNT(*)")
                .contains("l.action = #{action}")
                .contains("a.username = #{username}")
                .contains("l.target_type = #{targetType}")
                .contains("l.target_id = #{targetId}");

        Method method = PermissionOperationLogMapper.class.getMethod(
                "selectPermissionOperationLogs",
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                Long.class,
                boolean.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
        assertThat(method.getGenericReturnType().getTypeName())
                .contains(PermissionOperationLogVO.class.getName());

        String sql = assertSql(method);
        assertThat(sql).contains("a.username");
        assertThat(sql).contains("l.user_id AS userId");
        assertThat(sql).contains("ORDER BY l.create_time ASC, l.log_id ASC");
        assertThat(sql).contains("ORDER BY l.create_time DESC, l.log_id DESC");
        assertThat(sql).contains("LIMIT #{pageSize} OFFSET #{offset}");

        Method usernameOptionsMethod = PermissionOperationLogMapper.class.getMethod("selectUsernameOptions");
        assertThat(usernameOptionsMethod.getReturnType()).isEqualTo(List.class);
        assertThat(assertSql(usernameOptionsMethod))
                .contains("DISTINCT a.username")
                .contains("a.username IS NOT NULL")
                .contains("ORDER BY a.username ASC");
    }

    private String assertSql(Method method) {
        Select select = method.getAnnotation(Select.class);
        assertThat(select).isNotNull();
        return String.join(" ", select.value());
    }
}
