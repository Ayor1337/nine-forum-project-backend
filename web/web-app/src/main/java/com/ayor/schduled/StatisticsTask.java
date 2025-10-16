package com.ayor.schduled;

import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.AccountStatService;
import com.ayor.service.ThreaddService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class StatisticsTask {

    @Resource
    private AccountStatService accountStatService;

    @Resource
    private ThreaddService threaddService;

    @PostConstruct
    public void startUp() {
        accountStatistics();
        threadStatistics();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void accountStatistics() {
        log.info("开始更新用户统计信息");
        accountStatService.updateAccountStat();
        log.info("更新用户统计信息完成");
    }

    @Scheduled(cron = "0 0 * * * *")
    public void threadStatistics() {
        log.info("开始更新主题统计信息");
        threaddService.updateThreadStat();
        log.info("更新主题统计信息完成");
    }


}
