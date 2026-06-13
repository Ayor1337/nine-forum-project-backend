package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.MentionMessageVO;
import com.ayor.result.Result;
import com.ayor.service.MentionMessageService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mention-messages")
@RequiredArgsConstructor
@Tag(name = "提及消息")
public class MentionMessageController {

    private final MentionMessageService mentionMessageService;

    private final SecurityUtils securityUtils;

    @Operation(summary = "获取当前用户的提及消息")
    @GetMapping
    public Result<PageEntity<MentionMessageVO>> getMentionMessages(@Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                                   @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "7") Integer pageSize) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> mentionMessageService.listMentionMessages(pageNum, pageSize, userId), "获取提及消息失败");
    }
}
