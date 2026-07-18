package com.ayor.initializer;

import com.ayor.service.ESIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * ES 索引初始化入口：异步执行全量重建, ES 不可用时只告警不阻塞应用启动。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ESIndexInitializer implements ApplicationRunner {

    private final ESIndexService esIndexService;

    @Override
    public void run(ApplicationArguments args) {
        Thread thread = new Thread(() -> {
            try {
                esIndexService.initIndex();
            } catch (Exception e) {
                log.error("Elastic | 索引初始化失败, 搜索功能暂不可用, 可通过后台手动重建: {}", e.getMessage(), e);
            }
        });
        thread.setName("es-index-initializer");
        thread.setDaemon(true);
        thread.start();
    }
}
