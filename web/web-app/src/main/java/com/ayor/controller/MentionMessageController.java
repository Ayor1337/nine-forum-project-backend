package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.MentionMessageVO;
import com.ayor.result.Result;
import com.ayor.service.MentionMessageService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mention-messages")
@RequiredArgsConstructor
public class MentionMessageController {

    private final MentionMessageService mentionMessageService;

    private final SecurityUtils securityUtils;

    @GetMapping
    public Result<PageEntity<MentionMessageVO>> getMentionMessages(@RequestParam("page_num") Integer pageNum,
                                                                   @RequestParam(value = "page_size", defaultValue = "7") Integer pageSize) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> mentionMessageService.listMentionMessages(pageNum, pageSize, userId), "获取提及消息失败");
    }
}
