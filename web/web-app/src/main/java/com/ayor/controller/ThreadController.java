package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ContentReportDTO;
import com.ayor.entity.dto.TagUpdateDTO;
import com.ayor.entity.dto.ThreadDTO;
import com.ayor.entity.vo.AnnouncementVO;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.ReportService;
import com.ayor.service.ThreaddService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ThreadController {

    private final ThreaddService threaddService;

    private final SecurityUtils security;

    private final ReportService reportService;
    /**
     * 获取指定主题下的帖子列表。
     */


    @GetMapping("/topics/{topic_id}/threads")
    public Result<PageEntity<ThreadVO>> getThreadsByTopicId(@PathVariable("topic_id") Integer topicId,
                                                          @RequestParam(value = "tag", required = false) Integer tag,
                                                          @RequestParam(value = "order", defaultValue = "hot") String order,
                                                          @RequestParam("page_num")Integer pageNum,
                                                          @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> threaddService.getThreadVOsByTopicId(topicId, tag, order, pageNum, pageSize), "获取失败");
    }
    /**
     * 获取指定用户发布的帖子列表。
     */

    @GetMapping("/users/{user_id}/threads")
    public Result<PageEntity<ThreadVO>> getThreadsByUserId(@PathVariable(name = "user_id") Integer userId,
                                                 @RequestParam(name = "page") Integer page,
                                                 @RequestParam(name = "page_size") Integer size) {
        return Result.dataMessageHandler(() -> threaddService.getThreadPagesByUserId(userId, page, size), "获取失败");
    }

    // 注意调度
    /**
     * 获取帖子详情。
     */
    @GetMapping("/threads/{thread_id}")
    public Result<ThreadVO> getThreadById(@PathVariable(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> threaddService.getThreadById(threadId), "获取失败");
    }
    /**
     * 获取主题下的公告帖子。
     */

    @GetMapping("/topics/{topic_id}/announcements")
    public Result<List<AnnouncementVO>> getAnnouncementByTopicId(@PathVariable(name = "topic_id") Integer topicId) {
        return Result.dataMessageHandler(() -> threaddService.getAnnouncementThreads(topicId), "获取失败");
    }
    /**
     * 发布新帖子。
     */

    @PostMapping("/threads")
    public Result<Void> postThread(@Valid @RequestBody ThreadDTO threadDTO) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> threaddService.insertThread(threadDTO, userId));
    }
    /**
     * 删除当前用户发布的帖子。
     */

    @DeleteMapping("/threads/{thread_id}")
    public Result<Void> removeThreadById(@PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> threaddService.removeThreadById(threadId, userId));
    }

    @PostMapping("/threads/{thread_id}/reports")
    public Result<Void> createThreadReport(@PathVariable(name = "thread_id") Integer threadId,
                                           @RequestBody @Valid ContentReportDTO dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> reportService.createThreadReport(userId, threadId, dto));
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_UPDATE_TAG')" +
            "and hasAuthority('TOPIC_' + #topicId)")
    /**
     * 修改帖子标签。
     */
    @PutMapping("/moderation/threads/{thread_id}/tag")
    public Result<Void> updateTag(@PathVariable(name = "thread_id") Integer threadId,
                                  @RequestParam(name = "topic_id") Integer topicId,
                                  @RequestBody TagUpdateDTO tagUpdateDTO) {
        tagUpdateDTO.setThreadId(threadId);
        tagUpdateDTO.setTopicId(topicId);
        return Result.messageHandler(() -> threaddService.updateThreadTag(tagUpdateDTO));
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_UPDATE_TAG')" +
            "and hasAuthority('TOPIC_' + #topicId)")
    /**
     * 删除帖子标签。
     */
    @DeleteMapping("/moderation/threads/{thread_id}/tag")
    public Result<Void> deleteThreadTag(@PathVariable(name = "thread_id") Integer threadId,
                                        @RequestParam(name = "topic_id") Integer topicId) {
        return Result.messageHandler(() -> threaddService.removeThreadTag(threadId, topicId));
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_UPDATE_TAG')" +
            "and hasAuthority('TOPIC_' + #topicId)")
    /**
     * 将帖子设为主题公告。
     */
    @PutMapping("/topics/{topic_id}/announcements/{thread_id}")
    public Result<Void> setAnnouncement(@PathVariable(name = "topic_id") Integer topicId,
                                        @PathVariable(name = "thread_id") Integer threadId) {
        return Result.messageHandler(() -> threaddService.setAnnouncementByThreadId(threadId, topicId));
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_UPDATE_TAG')" +
            "and hasAuthority('TOPIC_' + #topicId)")
    /**
     * 取消帖子公告状态。
     */
    @DeleteMapping("/topics/{topic_id}/announcements/{thread_id}")
    public Result<Void> unsetAnnouncement(@PathVariable(name = "topic_id") Integer topicId,
                                          @PathVariable(name = "thread_id") Integer threadId) {
        return Result.messageHandler(() -> threaddService.removeAnnouncementByThreadId(threadId, topicId));
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_sDELETE_THREAD')" +
            "and hasAuthority('TOPIC_' + #topicId)")
    /**
     * 管理员删除帖子。
     */
    @DeleteMapping("/moderation/threads/{thread_id}")
    public Result<Void> removeThreadByIdPermission(@PathVariable(name = "thread_id") Integer threadId,
                                                   @RequestParam(name = "topic_id") Integer topicId) {
        return Result.messageHandler(() -> threaddService.permRemoveThreadById(threadId));
    }

    // You see but you do not observe
    /**
     * 记录帖子浏览次数。
     */
    @PostMapping("/threads/{thread_id}/views")
    public Result<Void> viewThread(@PathVariable(name = "thread_id") Integer threadId) {
        return Result.messageHandler(() -> threaddService.updateViewCount(threadId));
    }


}
