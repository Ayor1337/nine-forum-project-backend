package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Post;
import com.ayor.result.Result;
import com.ayor.result.ResultCodeEnum;
import com.ayor.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "回帖管理", description = "后台回帖管理接口")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 分页查询回帖列表，可按帖子或用户过滤。
     */
    @Operation(summary = "分页查询回帖列表，可按帖子或用户过滤")
    @GetMapping
    public Result<PageEntity<Post>> listPosts(@Parameter(description = "帖子ID") @RequestParam(value = "thread_id", required = false) Integer threadId,
                                              @Parameter(description = "用户ID") @RequestParam(value = "account_id", required = false) Integer accountId,
                                              @Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                              @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        if (threadId != null) {
            return Result.dataMessageHandler(() -> postService.getPostsByThreadId(threadId, pageNum, pageSize), "获取帖子回复失败");
        }
        if (accountId != null) {
            return Result.dataMessageHandler(() -> postService.getPostsByAccountId(accountId, pageNum, pageSize), "获取用户回帖失败");
        }
        return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), "thread_id 或 account_id 至少需要一个");
    }

    /**
     * 读取单条回帖详情。
     */
    @Operation(summary = "读取单条回帖详情")
    @GetMapping("/{postId}")
    public Result<Post> getPost(@Parameter(description = "回帖ID") @PathVariable("postId") Integer postId) {
        return Result.dataMessageHandler(() -> postService.getPostById(postId), "获取回复失败");
    }

    /**
     * 创建回帖。
     */
    @Operation(summary = "创建回帖")
    @PostMapping
    public Result<Void> createPost(@Parameter(description = "回帖信息") @RequestBody Post post) {
        return Result.messageHandler(() -> postService.createPost(post));
    }

    /**
     * 更新回帖内容。
     */
    @Operation(summary = "更新回帖内容")
    @PutMapping("/{postId}")
    public Result<Void> updatePost(@Parameter(description = "回帖ID") @PathVariable("postId") Integer postId, @RequestBody Post post) {
        post.setPostId(postId);
        return Result.messageHandler(() -> postService.updatePost(post));
    }

    /**
     * 删除指定回帖。
     */
    @Operation(summary = "删除指定回帖")
    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(@Parameter(description = "回帖ID") @PathVariable("postId") Integer postId) {
        return Result.messageHandler(() -> postService.deletePost(postId));
    }
}
