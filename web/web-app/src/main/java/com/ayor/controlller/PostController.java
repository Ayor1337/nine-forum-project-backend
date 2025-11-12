package com.ayor.controlller;

import com.ayor.entity.app.dto.PostDTO;
import com.ayor.entity.app.vo.PostVO;
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
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    private final SecurityUtils security;

    @GetMapping("/info/thread")
    public Result<List<PostVO>> getPostsByThreadId(@RequestParam(name = "thread_id") String threadId) {
        return Result.dataMessageHandler(() -> postService.getPostsByThreadId(Integer.parseInt(threadId)), "获取失败");
    }

    @PostMapping("/post")
    public Result<Void> addPost(@RequestBody @Validated PostDTO post) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> postService.insertPost(post, userId));
    }

    @DeleteMapping("/delete")
    public Result<Void> deletePost(@RequestParam(name = "post_id") Integer postId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> postService.removePostAuthorizeAccountId(postId, userId));
    }

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @DeleteMapping("/perm/delete")
    public Result<Void> deletePostPermission(@RequestParam(name = "post_id") Integer postId) {
        return Result.messageHandler(() -> postService.removePostPermission(postId));
    }

}
