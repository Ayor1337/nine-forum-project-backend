package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ContentReportDTO;
import com.ayor.entity.dto.PostDTO;
import com.ayor.entity.vo.PostVO;
import com.ayor.entity.vo.ReplyMessageVO;
import com.ayor.result.Result;
import com.ayor.service.PostService;
import com.ayor.service.ReportService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    private final SecurityUtils security;

    private final ReportService reportService;
    /**
     * 获取帖子下的评论列表。
     */

    @GetMapping("/threads/{thread_id}/posts")
    public Result<List<PostVO>> getPostsByThreadId(@PathVariable(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> postService.getPostsByThreadId(threadId), "获取失败");
    }
    /**
     * 发布评论。
     */

    @PostMapping("/threads/{thread_id}/posts")
    public Result<Void> addPost(@PathVariable(name = "thread_id") Integer threadId,
                                @RequestBody @Validated PostDTO post) {
        post.setThreadId(threadId);
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> postService.insertPost(post, userId));
    }
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
     * 管理员删除评论。
     */

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @DeleteMapping("/moderation/posts/{post_id}")
    public Result<Void> deletePostPermission(@PathVariable(name = "post_id") Integer postId) {
        return Result.messageHandler(() -> postService.removePostPermission(postId));
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
