package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ActivityVO;
import com.ayor.entity.vo.DashboardOverviewVO;
import com.ayor.entity.vo.HealthVO;
import com.ayor.entity.vo.KpiVO;
import com.ayor.entity.vo.TrendsVO;
import com.ayor.mapper.DashboardMapper;
import com.ayor.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final DashboardMapper dashboardMapper;

    @Override
    public DashboardOverviewVO getOverview() {
        Integer todayNewUsers = value(dashboardMapper.countTodayNewUsers());
        Integer yesterdayNewUsers = value(dashboardMapper.countYesterdayNewUsers());
        Integer todayThreads = value(dashboardMapper.countTodayThreads());
        Integer yesterdayThreads = value(dashboardMapper.countYesterdayThreads());
        Integer todayPosts = value(dashboardMapper.countTodayPosts());
        Integer yesterdayPosts = value(dashboardMapper.countYesterdayPosts());
        Integer todayReports = value(dashboardMapper.countTodayReports());
        Integer yesterdayReports = value(dashboardMapper.countYesterdayReports());
        Integer pendingReports = value(dashboardMapper.countPendingReports());
        Integer highPriorityReports = value(dashboardMapper.countHighPriorityReports());
        Integer overdueReports = value(dashboardMapper.countOverduePendingReports());

        DashboardOverviewVO overview = new DashboardOverviewVO();
        overview.setKpi(buildKpi(
                todayNewUsers,
                yesterdayNewUsers,
                todayThreads,
                pendingReports,
                highPriorityReports));
        overview.setTrends(buildTrends(
                todayThreads,
                yesterdayThreads,
                todayPosts,
                yesterdayPosts,
                todayReports,
                yesterdayReports));
        overview.setHealth(buildHealth(pendingReports, highPriorityReports, overdueReports));
        return overview;
    }

    @Override
    public PageEntity<ActivityVO> getActivities(Integer pageNum, Integer pageSize) {
        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        return new PageEntity<>(
                longValue(dashboardMapper.countDashboardActivities()),
                dashboardMapper.selectDashboardActivities(offset, normalizedPageSize));
    }

    private KpiVO buildKpi(Integer todayNewUsers,
                           Integer yesterdayNewUsers,
                           Integer todayThreads,
                           Integer pendingReports,
                           Integer highPriorityReports) {
        KpiVO kpi = new KpiVO();
        kpi.setTotalUsers(intValue(dashboardMapper.countTotalUsers()));
        kpi.setTotalUsersDeltaPercent(deltaPercent(todayNewUsers, yesterdayNewUsers));
        kpi.setTodayActive(value(dashboardMapper.countTodayActiveUsers()));
        kpi.setTodayActivePeakHour(value(dashboardMapper.selectTodayActivePeakHour()));
        kpi.setTotalPosts(intValue(dashboardMapper.countTotalThreads()));
        kpi.setTodayNewPosts(todayThreads);
        kpi.setPendingReports(pendingReports);
        kpi.setHighPriorityReports(highPriorityReports);
        return kpi;
    }

    private TrendsVO buildTrends(Integer todayThreads,
                                 Integer yesterdayThreads,
                                 Integer todayPosts,
                                 Integer yesterdayPosts,
                                 Integer todayReports,
                                 Integer yesterdayReports) {
        TrendsVO trends = new TrendsVO();
        trends.setTodayPosts(todayThreads);
        trends.setTodayPostsRatio(progressRatio(todayThreads, yesterdayThreads));
        trends.setTodayPostsSummary(threadSummary());
        trends.setTodayReplies(todayPosts);
        trends.setTodayRepliesRatio(progressRatio(todayPosts, yesterdayPosts));
        trends.setTodayRepliesSummary(changeSummary(todayPosts, yesterdayPosts));
        trends.setTodayReports(todayReports);
        trends.setTodayReportsRatio(progressRatio(todayReports, yesterdayReports));
        trends.setTodayReportsSummary(reportSummary(todayReports, yesterdayReports));
        return trends;
    }

    private HealthVO buildHealth(Integer pendingReports, Integer highPriorityReports, Integer overdueReports) {
        HealthVO health = new HealthVO();
        health.setAvgReportResponseMinutes(value(dashboardMapper.selectAvgReportResponseMinutes()));
        health.setOperationAlertCount(highPriorityReports + overdueReports);
        if (overdueReports > 0 || highPriorityReports >= 5 || pendingReports >= 50) {
            health.setSystemStatus("ERROR");
            health.setSystemStatusDetail("存在积压或高优先级举报");
            health.setOperationAlertDetail("需优先处理逾期和高优先级举报");
            return health;
        }
        if (highPriorityReports >= 5 || pendingReports >= 20) {
            health.setSystemStatus("WARNING");
            health.setSystemStatusDetail("存在待关注举报");
            health.setOperationAlertDetail("需关注高频重复举报");
            return health;
        }
        health.setSystemStatus("STABLE");
        health.setSystemStatusDetail("接口与资源同步正常");
        health.setOperationAlertDetail("暂无待关注运营提醒");
        return health;
    }

    private String threadSummary() {
        String summary = dashboardMapper.selectTopThreadTopicSummary();
        return StringUtils.hasText(summary) ? summary : "暂无今日发帖";
    }

    private String changeSummary(Integer today, Integer yesterday) {
        if (yesterday == 0) {
            return today == 0 ? "与昨日同段持平" : "较昨日同段 +100%";
        }
        int percent = Math.round((today - yesterday) * 100.0F / yesterday);
        return percent >= 0 ? "较昨日同段 +" + percent + "%" : "较昨日同段 " + percent + "%";
    }

    private String reportSummary(Integer today, Integer yesterday) {
        if (today == 0) {
            return "今日暂无新增举报";
        }
        return changeSummary(today, yesterday);
    }

    private Float deltaPercent(Integer today, Integer yesterday) {
        if (yesterday == 0) {
            return today == 0 ? 0F : 100F;
        }
        return (today - yesterday) * 100.0F / yesterday;
    }

    private Float progressRatio(Integer today, Integer yesterday) {
        if (yesterday == 0) {
            return today == 0 ? 0F : 1F;
        }
        return Math.min(1F, today * 1.0F / yesterday);
    }

    private Integer value(Integer value) {
        return value == null ? 0 : value;
    }

    private Long longValue(Long value) {
        return value == null ? 0L : value;
    }

    private Integer intValue(Long value) {
        if (value == null) {
            return 0;
        }
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : value.intValue();
    }
}
