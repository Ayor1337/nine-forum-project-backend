package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ContentReportDTO;
import com.ayor.entity.dto.PostDTO;
import com.ayor.entity.dto.ThreadDTO;
import com.ayor.entity.vo.AnnouncementVO;
import com.ayor.entity.vo.PostVO;
import com.ayor.entity.vo.ThreadEditHistoryVO;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.PostService;
import com.ayor.service.ReportService;
import com.ayor.service.ThreaddService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "帖子")
public class ThreadController {

    private final ThreaddService threaddService;

    private final PostService postService;

    private final SecurityUtils security;

    private final ReportService reportService;

    /**
     * 获取指定主题下的帖子列表。
     */
    @Operation(summary = "获取指定主题下的帖子列表")
    @GetMapping("/topics/{topic_id}/threads")
    public Result<PageEntity<ThreadVO>> getThreadsByTopicId(@Parameter(description = "话题 ID") @PathVariable("topic_id") Integer topicId,
                                                          @Parameter(description = "标签 ID") @RequestParam(value = "tag_id", required = false) Integer tagId,
                                                          @Parameter(description = "是否精选") @RequestParam(value = "is_selected", required = false) Boolean isSelected,
                                                          @Parameter(description = "排序方式") @RequestParam(value = "order", defaultValue = "hot") String order,
                                                          @Parameter(description = "页码") @RequestParam("page_num")Integer pageNum,
                                                          @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        Integer viewerId = security.getOptionalSecurityUserId();
        return Result.dataMessageHandler(() -> threaddService.getThreadVOsByTopicId(viewerId, topicId, tagId, isSelected, order, pageNum, pageSize), "获取失败");
    }

    @Operation(summary = "获取话题帖子排行榜")
    @GetMapping("/topics/{topic_id}/thread-rankings")
    public Result<PageEntity<ThreadVO>> getTopicThreadRankings(@Parameter(description = "话题 ID") @PathVariable("topic_id") Integer topicId,
                                                               @Parameter(description = "统计周期") @RequestParam(value = "period", defaultValue = "day") String period,
                                                               @Parameter(description = "统计指标") @RequestParam(value = "metric", defaultValue = "likes") String metric,
                                                               @Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                               @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        Integer viewerId = security.getOptionalSecurityUserId();
        return Result.dataMessageHandler(() -> threaddService.getThreadRankingsByTopicId(viewerId, topicId, period, metric, pageNum, pageSize), "获取失败");
    }

    @Operation(summary = "获取全站帖子排行榜")
    @GetMapping("/thread-rankings")
    public Result<PageEntity<ThreadVO>> getThreadRankings(@Parameter(description = "统计周期") @RequestParam(value = "period", defaultValue = "day") String period,
                                                          @Parameter(description = "统计指标") @RequestParam(value = "metric", defaultValue = "likes") String metric,
                                                          @Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                          @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        Integer viewerId = security.getOptionalSecurityUserId();
        return Result.dataMessageHandler(() -> threaddService.getThreadRankings(viewerId, period, metric, pageNum, pageSize), "获取失败");
    }
    /**
     * 获取指定用户发布的帖子列表。
     */
    @Operation(summary = "获取指定用户发布的帖子列表")
    @GetMapping("/users/{user_id}/threads")
    public Result<PageEntity<ThreadVO>> getThreadsByUserId(@Parameter(description = "用户 ID") @PathVariable(name = "user_id") Integer userId,
                                                 @Parameter(description = "页码") @RequestParam(name = "page") Integer page,
                                                 @Parameter(description = "每页数量") @RequestParam(name = "page_size") Integer size) {
        Integer viewerId = security.getOptionalSecurityUserId();
        return Result.dataMessageHandler(() -> threaddService.getThreadPagesByUserId(viewerId, userId, page, size), "获取失败");
    }

    // 注意调度
    /**
     * 获取帖子详情。
     */
    @Operation(summary = "获取帖子详情")
    @GetMapping("/threads/{thread_id}")
    public Result<ThreadVO> getThreadById(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        Integer viewerId = security.getOptionalSecurityUserId();
        return Result.dataMessageHandler(() -> threaddService.getThreadById(viewerId, threadId), "获取失败");
    }

    /**
     * 分页获取帖子下的评论列表。
     */
    @Operation(summary = "分页获取帖子下的评论列表")
    @GetMapping("/threads/{thread_id}/posts")
    public Result<PageEntity<PostVO>> getPostsByThreadId(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId,
                                                         @Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                         @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        Integer viewerId = security.getOptionalSecurityUserId();
        return Result.dataMessageHandler(() -> postService.getPostsByThreadId(viewerId, threadId, pageNum, pageSize), "获取失败");
    }

    /**
     * 发布评论。
     */
    @Operation(summary = "发布评论")
    @PostMapping("/threads/{thread_id}/posts")
    public Result<Void> addPost(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId,
                                @RequestBody @Validated PostDTO post) {
        post.setThreadId(threadId);
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> postService.insertPost(post, userId));
    }

    /**
     * 获取主题下的公告帖子。
     */
    @Operation(summary = "获取主题下的公告帖子")
    @GetMapping("/topics/{topic_id}/announcements")
    public Result<List<AnnouncementVO>> getAnnouncementByTopicId(@Parameter(description = "话题 ID") @PathVariable(name = "topic_id") Integer topicId) {
        return Result.dataMessageHandler(() -> threaddService.getAnnouncementThreads(topicId), "获取失败");
    }

    /**
     * 获取全局公告帖子。
     */
    @Operation(summary = "获取全局公告帖子")
    @GetMapping("/announcements/global")
    public Result<List<AnnouncementVO>> getGlobalAnnouncements() {
        return Result.dataMessageHandler(threaddService::getGlobalAnnouncementThreads, "获取失败");
    }
    /**
     * 发布新帖子。
     */
    @Operation(summary = "发布新帖子")
    @PostMapping("/threads")
    public Result<Void> postThread(@Valid @RequestBody ThreadDTO threadDTO) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> threaddService.insertThread(threadDTO, userId));
    }

    /**
     * 编辑当前用户发布的帖子。
     */
    @Operation(summary = "编辑当前用户发布的帖子")
    @PutMapping("/threads/{thread_id}")
    public Result<Void> editThread(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId,
                                   @Valid @RequestBody ThreadDTO threadDTO) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> threaddService.editThread(threadId, threadDTO, userId));
    }

    /**
     * 获取帖子的公开编辑历史（不含正文与标题快照）。
     */
    @Operation(summary = "获取帖子的公开编辑历史")
    @GetMapping("/threads/{thread_id}/edit-history")
    public Result<List<ThreadEditHistoryVO>> getThreadEditHistory(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> threaddService.listEditHistory(threadId), "获取失败");
    }

    /**
     * 删除当前用户发布的帖子。
     */
    @Operation(summary = "删除当前用户发布的帖子")
    @DeleteMapping("/threads/{thread_id}")
    public Result<Void> removeThreadById(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> threaddService.removeThreadById(threadId, userId));
    }

    @Operation(summary = "举报帖子")
    @PostMapping("/threads/{thread_id}/reports")
    public Result<Void> createThreadReport(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId,
                                           @RequestBody @Valid ContentReportDTO dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> reportService.createThreadReport(userId, threadId, dto));
    }

    // You see but you do not observe
    /**
     * 记录帖子浏览次数。
     */
    @Operation(summary = "记录帖子浏览次数")
    @PostMapping("/threads/{thread_id}/views")
    public Result<Void> viewThread(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        return Result.messageHandler(() -> threaddService.updateViewCount(threadId));
    }


}
