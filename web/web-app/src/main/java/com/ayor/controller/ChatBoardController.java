package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ChatboardHistoryVO;
import com.ayor.entity.stomp.ChatBoardMessage;
import com.ayor.result.Result;
import com.ayor.service.ChatboardHistoryService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/topics/{topic_id}/chat-messages")
public class ChatBoardController {

    private final ChatboardHistoryService chatboardHistoryService;

    private final SecurityUtils securityUtils;
    /**
     * 发送聊天频道消息并写入聊天记录。
     *
     * @param topicId 主题 ID
     * @param message 聊天消息内容
     * @return 发送结果
     */

    @PostMapping
    public Result<Void> chat(@PathVariable("topic_id") Integer topicId,
                             @RequestBody ChatBoardMessage message) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> chatboardHistoryService.insertChatboardHistory(userId, topicId, message.getContent()));
    }
    /**
     * 获取主题聊天室的聊天记录。
     *
     * @param topicId 主题 ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 聊天记录分页数据
     */

    @GetMapping
    public Result<PageEntity<ChatboardHistoryVO>> getHistory(@PathVariable("topic_id")Integer topicId,
                                                             @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                             @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> chatboardHistoryService.getChatboardHistory(topicId, pageNum, pageSize), "获取聊天记录失败");
    }



}
