package com.ayor.mapper;

import com.ayor.entity.pojo.Report;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ReportMapper extends BaseMapper<Report> {

    @Select("""
            select * from report
            where reporter_account_id = #{reporterAccountId}
              and target_type = #{targetType}
              and target_id = #{targetId}
              and status in ('PENDING', 'PROCESSING')
            order by report_id desc
            limit 1
            """)
    Report selectActiveReport(@Param("reporterAccountId") Integer reporterAccountId,
                              @Param("targetType") String targetType,
                              @Param("targetId") Integer targetId);
}
