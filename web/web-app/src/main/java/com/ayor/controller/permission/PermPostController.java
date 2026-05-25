package com.ayor.controller.permission;

import com.ayor.result.Result;
import com.ayor.service.AuthorizationService;
import com.ayor.service.PostService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/moderation")
public class PermPostController {

    private final PostService postService;

    private final SecurityUtils security;

    private final AuthorizationService authorizationService;

    /**
     * 管理员删除评论。
     */
    @DeleteMapping("/posts/{post_id}")
    public Result<Void> deletePostPermission(@PathVariable(name = "post_id") Integer postId) {
        authorizationService.assertCanDeletePost(security.getSecurityUserId(), postId);
        return Result.messageHandler(() -> postService.removePostPermission(postId));
    }
}
