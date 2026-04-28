package com.ayor.scheduled;

import com.ayor.service.ESIndexService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ElasticTask {

    private final ESIndexService elIndexService;

    @PostConstruct
    public void initIndex() {
        elIndexService.initIndex();
        log.info("Elastic 初始化成功");
    }



}
