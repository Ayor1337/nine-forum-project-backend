package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.dto.PostDTO;
import com.ayor.entity.app.vo.PostVO;
import com.ayor.entity.app.vo.ReplyMessageVO;
import com.ayor.result.Result;
import com.ayor.service.PostService;
import com.ayor.util.SecurityUtils;
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
    /**
     * getPostsByThreadId 方法。
     */

    @GetMapping("/threads/{thread_id}/posts")
    public Result<List<PostVO>> getPostsByThreadId(@PathVariable(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> postService.getPostsByThreadId(threadId), "获取失败");
    }
    /**
     * addPost 方法。
     */

    @PostMapping("/threads/{thread_id}/posts")
    public Result<Void> addPost(@PathVariable(name = "thread_id") Integer threadId,
                                @RequestBody @Validated PostDTO post) {
        post.setThreadId(threadId);
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> postService.insertPost(post, userId));
    }
    /**
     * deletePost 方法。
     */

    @DeleteMapping("/posts/{post_id}")
    public Result<Void> deletePost(@PathVariable(name = "post_id") Integer postId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> postService.removePostAuthorizeAccountId(postId, userId));
    }
    /**
     * deletePostPermission 方法。
     */

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @DeleteMapping("/moderation/posts/{post_id}")
    public Result<Void> deletePostPermission(@PathVariable(name = "post_id") Integer postId) {
        return Result.messageHandler(() -> postService.removePostPermission(postId));
    }
    /**
     * getReplyMessage 方法。
     */

    @GetMapping("/posts/reply-messages")
    public Result<PageEntity<ReplyMessageVO>> getReplyMessage(@RequestParam("page_num") Integer pageNum,
                                                            @RequestParam(value = "page_size", defaultValue = "7") Integer pageSize) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> postService.listReplyMessage(pageNum, pageSize, userId), "获取失败");
    }

}
