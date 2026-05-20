package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicChat;
import com.ayor.entity.vo.TopicChatVO;
import com.ayor.result.Result;
import com.ayor.service.TopicChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/topic_chats")
@RequiredArgsConstructor
public class TopicChatController {

    private final TopicChatService topicChatService;

    /**
     * 分页查询话题聊天记录。
     */
    @GetMapping
    public Result<PageEntity<TopicChatVO>> listTopicChats(@RequestParam("page_num") Integer pageNum,
                                                          @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                          @RequestParam(value = "topic_id", required = false) Integer topicId) {
        return Result.dataMessageHandler(() -> topicChatService.getTopicChats(topicId, pageNum, pageSize), "获取话题聊天记录失败");
    }

    /**
     * 查询单条话题聊天记录。
     */
    @GetMapping("/{topicChatId}")
    public Result<TopicChatVO> getTopicChat(@PathVariable("topicChatId") Integer topicChatId) {
        return Result.dataMessageHandler(() -> topicChatService.getTopicChatById(topicChatId), "获取话题聊天记录失败");
    }

    /**
     * 创建话题聊天记录。
     */
    @PostMapping
    public Result<Void> createTopicChat(@RequestBody TopicChat topicChat) {
        return Result.messageHandler(() -> topicChatService.createTopicChat(topicChat));
    }

    /**
     * 更新话题聊天记录。
     */
    @PutMapping("/{topicChatId}")
    public Result<Void> updateTopicChat(@PathVariable("topicChatId") Integer topicChatId,
                                        @RequestBody TopicChat topicChat) {
        topicChat.setTopicChatId(topicChatId);
        return Result.messageHandler(() -> topicChatService.updateTopicChat(topicChat));
    }

    /**
     * 删除指定话题聊天记录。
     */
    @DeleteMapping("/{topicChatId}")
    public Result<Void> deleteTopicChat(@PathVariable("topicChatId") Integer topicChatId) {
        return Result.messageHandler(() -> topicChatService.deleteTopicChat(topicChatId));
    }
}
