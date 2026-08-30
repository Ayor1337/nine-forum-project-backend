package com.ayor.controller.permission;

import com.ayor.aspect.oplog.OperationLog;
import com.ayor.entity.vo.PostEditHistoryDetailVO;
import com.ayor.result.Result;
import com.ayor.service.AuthorizationService;
import com.ayor.service.PostService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/perm/post")
@Tag(name = "权限-回复")
public class PermPostController {

    private final PostService postService;

    private final SecurityUtils security;

    private final AuthorizationService authorizationService;

    /**
     * 版主查看回复编辑历史（含正文快照）。
     */
    @Operation(summary = "版主查看回复编辑历史")
    @GetMapping("/{post_id}/edit-history")
    public Result<List<PostEditHistoryDetailVO>> listEditHistoryWithSnapshots(
            @Parameter(description = "回复 ID") @PathVariable(name = "post_id") Integer postId) {
        authorizationService.assertCanViewPostEditSnapshots(security.getSecurityUserId(), postId);
        return Result.dataMessageHandler(() -> postService.listEditHistoryWithSnapshots(postId), "获取失败");
    }

    /**
     * 管理员删除评论。
     */
    @OperationLog(value = "权限删除回复", save = true, action = "DELETE_POST", targetType = "post", targetIdParam = "postId")
    @Operation(summary = "管理员删除回复")
    @DeleteMapping("/{post_id}")
    public Result<Void> deletePostPermission(@Parameter(description = "回复 ID") @PathVariable(name = "post_id") Integer postId) {
        authorizationService.assertCanModerateDeletePost(security.getSecurityUserId(), postId);
        return Result.messageHandler(() -> postService.removePostPermission(postId));
    }
}
