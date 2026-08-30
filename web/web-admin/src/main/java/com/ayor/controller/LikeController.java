package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.LikeThread;
import com.ayor.entity.vo.LikeThreadVO;
import com.ayor.result.Result;
import com.ayor.service.LikeService;
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

@Tag(name = "点赞管理", description = "后台点赞记录管理接口")
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 分页查询点赞记录，可按帖子或用户过滤。
     */
    @Operation(summary = "分页查询点赞记录，可按帖子或用户过滤")
    @GetMapping
    public Result<PageEntity<LikeThreadVO>> listLikes(@Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                      @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                      @Parameter(description = "帖子ID") @RequestParam(value = "thread_id", required = false) Integer threadId,
                                                      @Parameter(description = "用户ID") @RequestParam(value = "account_id", required = false) Integer accountId) {
        return Result.dataMessageHandler(() -> likeService.getLikes(pageNum, pageSize, threadId, accountId), "获取点赞记录失败");
    }

    /**
     * 查询单条点赞记录。
     */
    @Operation(summary = "查询单条点赞记录")
    @GetMapping("/{likeId}")
    public Result<LikeThreadVO> getLike(@Parameter(description = "点赞记录ID") @PathVariable("likeId") Integer likeId) {
        return Result.dataMessageHandler(() -> likeService.getLikeById(likeId), "获取点赞记录失败");
    }

    /**
     * 创建点赞记录。
     */
    @Operation(summary = "创建点赞记录")
    @PostMapping
    public Result<Void> createLike(@Parameter(description = "点赞记录") @RequestBody LikeThread like) {
        return Result.messageHandler(() -> likeService.createLike(like));
    }

    /**
     * 更新点赞记录。
     */
    @Operation(summary = "更新点赞记录")
    @PutMapping("/{likeId}")
    public Result<Void> updateLike(@Parameter(description = "点赞记录ID") @PathVariable("likeId") Integer likeId,
                                   @Parameter(description = "点赞记录") @RequestBody LikeThread like) {
        like.setLikeId(likeId);
        return Result.messageHandler(() -> likeService.updateLike(like));
    }

    /**
     * 删除指定点赞记录。
     */
    @Operation(summary = "删除指定点赞记录")
    @DeleteMapping("/{likeId}")
    public Result<Void> deleteLike(@Parameter(description = "点赞记录ID") @PathVariable("likeId") Integer likeId) {
        return Result.messageHandler(() -> likeService.deleteLike(likeId));
    }
}
