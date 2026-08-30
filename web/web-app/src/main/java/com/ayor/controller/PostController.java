package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ContentReportDTO;
import com.ayor.entity.dto.PostEditDTO;
import com.ayor.entity.vo.PostEditHistoryVO;
import com.ayor.entity.vo.ReplyMessageVO;
import com.ayor.result.Result;
import com.ayor.service.PostService;
import com.ayor.service.ReportService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "回复")
public class PostController {

    private final PostService postService;

    private final SecurityUtils security;

    private final ReportService reportService;

    /**
     * 编辑当前用户的评论。
     */
    @Operation(summary = "编辑当前用户的评论")
    @PutMapping("/posts/{post_id}")
    public Result<Void> editPost(@Parameter(description = "回复 ID") @PathVariable(name = "post_id") Integer postId,
                                 @RequestBody @Valid PostEditDTO postEditDTO) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> postService.editPost(postId, postEditDTO, userId));
    }

    /**
     * 获取回复的公开编辑历史（不含正文快照）。
     */
    @Operation(summary = "获取回复的公开编辑历史")
    @GetMapping("/posts/{post_id}/edit-history")
    public Result<List<PostEditHistoryVO>> getPostEditHistory(@Parameter(description = "回复 ID") @PathVariable(name = "post_id") Integer postId) {
        return Result.dataMessageHandler(() -> postService.listEditHistory(postId), "获取失败");
    }

    /**
     * 删除当前用户的评论。
     */
    @Operation(summary = "删除当前用户的评论")
    @DeleteMapping("/posts/{post_id}")
    public Result<Void> deletePost(@Parameter(description = "回复 ID") @PathVariable(name = "post_id") Integer postId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> postService.removePostAuthorizeAccountId(postId, userId));
    }

    @Operation(summary = "举报回复")
    @PostMapping("/posts/{post_id}/reports")
    public Result<Void> createPostReport(@Parameter(description = "回复 ID") @PathVariable(name = "post_id") Integer postId,
                                         @RequestBody @Valid ContentReportDTO dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> reportService.createPostReport(userId, postId, dto));
    }
    /**
     * 获取回复消息分页数据。
     */
    @Operation(summary = "获取回复消息分页数据")
    @GetMapping("/posts/reply-messages")
    public Result<PageEntity<ReplyMessageVO>> getReplyMessage(@Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                            @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "7") Integer pageSize) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> postService.listReplyMessage(pageNum, pageSize, userId), "获取失败");
    }

}
