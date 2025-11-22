package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicChat;
import com.ayor.result.Result;
import com.ayor.service.TopicChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/topic_chat")
@RequiredArgsConstructor
public class TopicChatController {

    private final TopicChatService topicChatService;

    @GetMapping("/list")
    public Result<PageEntity<TopicChat>> listTopicChats(@RequestParam("page_num") Integer pageNum,
                                                        @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                        @RequestParam(value = "topic_id", required = false) Integer topicId) {
        return Result.dataMessageHandler(() -> topicChatService.getTopicChats(topicId, pageNum, pageSize), "获取话题聊天记录失败");
    }

    @DeleteMapping("/{topic_chat_id}")
    public Result<Void> deleteTopicChat(@PathVariable("topic_chat_id") Integer topicChatId) {
        return Result.messageHandler(() -> topicChatService.deleteTopicChat(topicChatId));
    }
}
