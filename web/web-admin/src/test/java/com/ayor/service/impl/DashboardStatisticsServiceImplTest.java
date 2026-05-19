package com.ayor.service.impl;

import com.ayor.mapper.DashboardMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardStatisticsServiceImplTest {

    @Mock
    private DashboardMapper dashboardMapper;

    @Test
    void shouldRefreshDashboardActivitiesThroughMapper() {
        DashboardStatisticsServiceImpl service = new DashboardStatisticsServiceImpl(dashboardMapper);
        when(dashboardMapper.refreshDashboardActivities()).thenReturn(3);

        String result = service.refreshDashboardActivities();

        assertNull(result);
        verify(dashboardMapper).refreshDashboardActivities();
    }
}
