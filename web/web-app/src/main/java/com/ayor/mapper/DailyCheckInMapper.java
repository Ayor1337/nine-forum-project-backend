package com.ayor.mapper;

import com.ayor.entity.pojo.DailyCheckIn;
import com.ayor.entity.vo.RecentCheckInUserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyCheckInMapper extends BaseMapper<DailyCheckIn> {

    List<RecentCheckInUserVO> selectRecentCheckInUsers(@Param("limit") int limit);

    boolean existsByAccountIdAndCheckInDate(@Param("accountId") Integer accountId,
                                            @Param("checkInDate") LocalDate checkInDate);
}
