package com.ayor.service.impl;

import com.ayor.mapper.DashboardMapper;
import com.ayor.service.DashboardStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DashboardStatisticsServiceImpl implements DashboardStatisticsService {

    private final DashboardMapper dashboardMapper;

    @Override
    public String refreshDashboardActivities() {
        dashboardMapper.refreshDashboardActivities();
        return null;
    }
}
