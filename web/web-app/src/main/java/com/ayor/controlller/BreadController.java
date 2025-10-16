package com.ayor.controlller;

import com.ayor.result.Result;
import com.ayor.service.ThreaddService;
import com.ayor.service.TopicService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bread")
public class BreadController {

    @Resource
    private TopicService topicService;

    @Resource
    private ThreaddService threaddService;

    @GetMapping("/info/topic/{topic_id}")
    public Result<String> getTopicInfo(@PathVariable("topic_id") Integer topicId) {
        return Result.dataMessageHandler(() -> topicService.getTopicNameById(topicId), "获取帖子信息失败");
    }

    @GetMapping("/info/thread/{thread_id}")
    public Result<String> getThreadInfo(@PathVariable("thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> threaddService.getThreadTitleById(threadId), "获取帖子信息失败");
    }



}
