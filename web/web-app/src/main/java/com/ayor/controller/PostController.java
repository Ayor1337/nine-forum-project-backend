package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ContentReportDTO;
import com.ayor.entity.vo.ReplyMessageVO;
import com.ayor.result.Result;
import com.ayor.service.PostService;
import com.ayor.service.ReportService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    private final SecurityUtils security;

    private final ReportService reportService;

    /**
     * 删除当前用户的评论。
     */

    @DeleteMapping("/posts/{post_id}")
    public Result<Void> deletePost(@PathVariable(name = "post_id") Integer postId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> postService.removePostAuthorizeAccountId(postId, userId));
    }

    @PostMapping("/posts/{post_id}/reports")
    public Result<Void> createPostReport(@PathVariable(name = "post_id") Integer postId,
                                         @RequestBody @Valid ContentReportDTO dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> reportService.createPostReport(userId, postId, dto));
    }
    /**
     * 获取回复消息分页数据。
     */

    @GetMapping("/posts/reply-messages")
    public Result<PageEntity<ReplyMessageVO>> getReplyMessage(@RequestParam("page_num") Integer pageNum,
                                                            @RequestParam(value = "page_size", defaultValue = "7") Integer pageSize) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> postService.listReplyMessage(pageNum, pageSize, userId), "获取失败");
    }

}
