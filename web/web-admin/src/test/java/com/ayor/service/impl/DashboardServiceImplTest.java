package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ActivityVO;
import com.ayor.entity.vo.DashboardOverviewVO;
import com.ayor.mapper.DashboardMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private DashboardMapper dashboardMapper;

    @Test
    void shouldBuildOverviewFromAggregatedDashboardMetrics() {
        DashboardServiceImpl service = new DashboardServiceImpl(dashboardMapper);

        when(dashboardMapper.countTotalUsers()).thenReturn(120L);
        when(dashboardMapper.countTodayNewUsers()).thenReturn(6);
        when(dashboardMapper.countYesterdayNewUsers()).thenReturn(3);
        when(dashboardMapper.countTodayActiveUsers()).thenReturn(18);
        when(dashboardMapper.selectTodayActivePeakHour()).thenReturn(14);
        when(dashboardMapper.countTotalThreads()).thenReturn(500L);
        when(dashboardMapper.countTodayThreads()).thenReturn(20);
        when(dashboardMapper.countYesterdayThreads()).thenReturn(40);
        when(dashboardMapper.countTodayPosts()).thenReturn(80);
        when(dashboardMapper.countYesterdayPosts()).thenReturn(100);
        when(dashboardMapper.countTodayReports()).thenReturn(5);
        when(dashboardMapper.countYesterdayReports()).thenReturn(10);
        when(dashboardMapper.countPendingReports()).thenReturn(9);
        when(dashboardMapper.countHighPriorityReports()).thenReturn(2);
        when(dashboardMapper.countOverduePendingReports()).thenReturn(0);
        when(dashboardMapper.selectTopThreadTopicSummary()).thenReturn("技术交流区贡献 42%");

        DashboardOverviewVO overview = service.getOverview();

        assertEquals(120, overview.getKpi().getTotalUsers());
        assertEquals(100.0F, overview.getKpi().getTotalUsersDeltaPercent());
        assertEquals(18, overview.getKpi().getTodayActive());
        assertEquals(14, overview.getKpi().getTodayActivePeakHour());
        assertEquals(500, overview.getKpi().getTotalPosts());
        assertEquals(20, overview.getKpi().getTodayNewPosts());
        assertEquals(9, overview.getKpi().getPendingReports());
        assertEquals(2, overview.getKpi().getHighPriorityReports());
        assertEquals(20, overview.getTrends().getTodayPosts());
        assertEquals(0.5F, overview.getTrends().getTodayPostsRatio());
        assertEquals("技术交流区贡献 42%", overview.getTrends().getTodayPostsSummary());
        assertEquals(80, overview.getTrends().getTodayReplies());
        assertEquals(0.8F, overview.getTrends().getTodayRepliesRatio());
        assertEquals(5, overview.getTrends().getTodayReports());
        assertEquals(0.5F, overview.getTrends().getTodayReportsRatio());
        assertEquals("STABLE", overview.getHealth().getSystemStatus());
    }

    @Test
    void shouldReturnPagedActivitiesWithDefaultPagination() {
        DashboardServiceImpl service = new DashboardServiceImpl(dashboardMapper);
        ActivityVO activity = new ActivityVO();
        activity.setId(1001L);
        activity.setCreatedAt(LocalDateTime.of(2026, 5, 19, 8, 58));
        activity.setUserId(42L);
        activity.setUsername("张三");
        activity.setAction("POST_THREAD");
        activity.setTarget("如何优化论坛性能");
        activity.setTargetId(567L);
        activity.setType("thread");

        when(dashboardMapper.countDashboardActivities()).thenReturn(1L);
        when(dashboardMapper.selectDashboardActivities(0, 20)).thenReturn(List.of(activity));

        PageEntity<ActivityVO> page = service.getActivities(null, null);

        assertEquals(1L, page.getTotalSize());
        assertEquals(1, page.getData().size());
        assertEquals("POST_THREAD", page.getData().get(0).getAction());
        verify(dashboardMapper).selectDashboardActivities(0, 20);
    }
}
