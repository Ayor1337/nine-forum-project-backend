package com.ayor.controller.permission;

import com.ayor.aspect.oplog.OperationLog;
import com.ayor.entity.dto.TagUpdateDTO;
import com.ayor.entity.vo.ThreadEditHistoryDetailVO;
import com.ayor.result.Result;
import com.ayor.service.AuthorizationService;
import com.ayor.service.ThreaddService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/perm/thread")
@Tag(name = "权限-帖子")
public class PermThreadController {

    private final ThreaddService threaddService;

    private final SecurityUtils security;

    private final AuthorizationService authorizationService;

    /**
     * 修改帖子标签。
     */
    @OperationLog(value = "更新帖子标签", save = true, action = "UPDATE_THREAD_TAG", targetType = "thread", targetIdParam = "threadId")
    @Operation(summary = "修改帖子标签")
    @PutMapping("/{thread_id}/tag")
    public Result<Void> updateTag(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId,
                                  @Parameter(description = "话题 ID") @RequestParam(name = "topic_id") Integer topicId,
                                  @Valid @RequestBody TagUpdateDTO tagUpdateDTO) {
        authorizationService.assertCanUpdateThreadTag(security.getSecurityUserId(), threadId, topicId);
        return Result.messageHandler(() -> threaddService.updateThreadTag(threadId, topicId, tagUpdateDTO.getTagId()));
    }

    /**
     * 删除帖子标签。
     */
    @OperationLog(value = "删除帖子标签", save = true, action = "DELETE_THREAD_TAG", targetType = "thread", targetIdParam = "threadId")
    @Operation(summary = "删除帖子标签")
    @DeleteMapping("/{thread_id}/tag")
    public Result<Void> deleteThreadTag(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId,
                                        @Parameter(description = "话题 ID") @RequestParam(name = "topic_id") Integer topicId) {
        authorizationService.assertCanUpdateThreadTag(security.getSecurityUserId(), threadId, topicId);
        return Result.messageHandler(() -> threaddService.removeThreadTag(threadId, topicId));
    }

    /**
     * 管理员删除帖子。
     */
    @OperationLog(value = "权限删除帖子", save = true, action = "DELETE_THREAD", targetType = "thread", targetIdParam = "threadId")
    @Operation(summary = "管理员删除帖子")
    @DeleteMapping("/{thread_id}")
    public Result<Void> removeThreadByIdPermission(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId,
                                                   @Parameter(description = "话题 ID") @RequestParam(name = "topic_id") Integer topicId) {
        authorizationService.assertCanModerateDeleteThread(security.getSecurityUserId(), threadId, topicId);
        return Result.messageHandler(() -> threaddService.permRemoveThreadById(threadId));
    }

    /**
     * 将帖子设为话题公告。
     */
    @OperationLog(value = "设置公告帖", save = true, action = "SET_ANNOUNCEMENT", targetType = "thread", targetIdParam = "threadId")
    @Operation(summary = "设置公告帖")
    @PutMapping("/{thread_id}/announcement")
    public Result<Void> setAnnouncement(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId,
                                        @Parameter(description = "话题 ID") @RequestParam(name = "topic_id") Integer topicId) {
        authorizationService.assertCanSetAnnouncement(security.getSecurityUserId(), threadId, topicId);
        return Result.messageHandler(() -> threaddService.setAnnouncementByThreadId(threadId, topicId));
    }

    /**
     * 取消帖子公告状态。
     */
    @OperationLog(value = "取消公告帖", save = true, action = "UNSET_ANNOUNCEMENT", targetType = "thread", targetIdParam = "threadId")
    @Operation(summary = "取消公告帖")
    @DeleteMapping("/{thread_id}/announcement")
    public Result<Void> unsetAnnouncement(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId,
                                          @Parameter(description = "话题 ID") @RequestParam(name = "topic_id") Integer topicId) {
        authorizationService.assertCanSetAnnouncement(security.getSecurityUserId(), threadId, topicId);
        return Result.messageHandler(() -> threaddService.removeAnnouncementByThreadId(threadId, topicId));
    }

    /**
     * 版主查看帖子编辑历史（含标题与正文快照）。
     */
    @Operation(summary = "版主查看帖子编辑历史")
    @GetMapping("/{thread_id}/edit-history")
    public Result<List<ThreadEditHistoryDetailVO>> listEditHistoryWithSnapshots(
            @Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId,
            @Parameter(description = "话题 ID") @RequestParam(name = "topic_id") Integer topicId) {
        authorizationService.assertCanViewThreadEditSnapshots(security.getSecurityUserId(), threadId, topicId);
        return Result.dataMessageHandler(() -> threaddService.listEditHistoryWithSnapshots(threadId), "获取失败");
    }
}
