package com.ayor.controlller;

import com.ayor.result.Result;
import com.ayor.service.ThreaddService;
import com.ayor.service.TopicService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bread")
public class BreadController {

    private final TopicService topicService;

    private final ThreaddService threaddService;

    @GetMapping("/info/topic_bread")
    public Result<String> getTopicInfo(@RequestParam(name = "topic_id") Integer topicId) {
        return Result.dataMessageHandler(() -> topicService.getTopicNameById(topicId), "获取帖子信息失败");
    }

    @GetMapping("/info/thread_bread")
    public Result<String> getThreadInfo(@RequestParam(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> threaddService.getThreadTitleById(threadId), "获取帖子信息失败");
    }



}
