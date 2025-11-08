package com.ayor.schduled;

import com.ayor.service.AccountStatService;
import com.ayor.service.ThreaddService;
import com.ayor.service.TopicStatService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLSyntaxErrorException;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class StatisticsTask {

    private final AccountStatService accountStatService;

    private final ThreaddService threaddService;

    private final TopicStatService topicStatService;

    @PostConstruct
    public void startUp() {
        try {
            accountStatistics();
            threadStatistics();
            topicStatistics();
        } catch (SQLSyntaxErrorException e) {
            log.warn("数据库连接失败");
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void accountStatistics() throws SQLSyntaxErrorException {
        log.info("开始更新用户统计信息");
        accountStatService.updateAccountStat();
        log.info("更新用户统计信息完成");
    }

    @Scheduled(cron = "0 0 * * * *")
    public void threadStatistics() throws SQLSyntaxErrorException {
        log.info("开始更新帖子统计信息");
        threaddService.updateThreadStat();
        log.info("更新帖子统计信息完成");
    }

    @Scheduled(cron = "0 0 * * * *")
    public void topicStatistics() throws SQLSyntaxErrorException {
        log.info("开始更新主题统计信息");
        topicStatService.updateTopicStat();
        log.info("更新主题统计信息完成");
    }




}
