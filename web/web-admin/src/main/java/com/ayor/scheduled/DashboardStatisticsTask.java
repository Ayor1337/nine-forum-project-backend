package com.ayor.scheduled;

import com.ayor.service.DashboardStatisticsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DashboardStatisticsTask {

    private final DashboardStatisticsService dashboardStatisticsService;

    @PostConstruct
    public void startUp() {
        try {
            refreshDashboardActivities();
        } catch (Exception exception) {
            log.warn("仪表盘动态刷新失败", exception);
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void refreshDashboardActivities() {
        log.info("开始刷新仪表盘动态");
        dashboardStatisticsService.refreshDashboardActivities();
        log.info("刷新仪表盘动态完成");
    }
}
