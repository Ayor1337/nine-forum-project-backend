package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ChatboardHistoryVO;
import com.ayor.entity.stomp.ChatBoardMessage;
import com.ayor.result.Result;
import com.ayor.service.ChatboardHistoryService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/topics/{topic_id}/chat-messages")
@Tag(name = "聊天室")
public class ChatBoardController {

    private final ChatboardHistoryService chatboardHistoryService;

    private final SecurityUtils securityUtils;
    /**
     * 发送聊天频道消息并写入聊天记录。
     */
    @Operation(summary = "发送聊天频道消息")
    @PostMapping
    public Result<Void> chat(@Parameter(description = "主题 ID") @PathVariable("topic_id") Integer topicId,
                             @RequestBody ChatBoardMessage message) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> chatboardHistoryService.insertChatboardHistory(userId, topicId, message.getContent()));
    }
    /**
     * 获取主题聊天室的聊天记录。
     */
    @Operation(summary = "获取主题聊天室的聊天记录")
    @GetMapping
    public Result<PageEntity<ChatboardHistoryVO>> getHistory(@Parameter(description = "主题 ID") @PathVariable("topic_id")Integer topicId,
                                                             @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                             @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> chatboardHistoryService.getChatboardHistory(userId, topicId, pageNum, pageSize), "获取聊天记录失败");
    }



}
