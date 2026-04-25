package com.ayor.controller;

import com.ayor.result.Result;
import com.ayor.service.ThreaddService;
import com.ayor.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class BreadController {

    private final TopicService topicService;

    private final ThreaddService threaddService;
    /**
     * getTopicInfo 方法。
     */

    @GetMapping("/topics/{topic_id}/breadcrumb")
    public Result<String> getTopicInfo(@PathVariable(name = "topic_id") Integer topicId) {
        return Result.dataMessageHandler(() -> topicService.getTopicNameById(topicId), "获取帖子信息失败");
    }
    /**
     * getThreadInfo 方法。
     */

    @GetMapping("/threads/{thread_id}/breadcrumb")
    public Result<String> getThreadInfo(@PathVariable(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> threaddService.getThreadTitleById(threadId), "获取帖子信息失败");
    }



}
