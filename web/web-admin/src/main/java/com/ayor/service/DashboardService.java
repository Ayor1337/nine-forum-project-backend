package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ActivityVO;
import com.ayor.entity.vo.DashboardOverviewVO;

public interface DashboardService {

    DashboardOverviewVO getOverview();

    PageEntity<ActivityVO> getActivities(Integer pageNum, Integer pageSize);
}
