package com.ayor.controlller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.dto.Konekuto;
import com.ayor.entity.app.dto.TagUpdateDTO;
import com.ayor.entity.app.dto.ThreadDTO;
import com.ayor.entity.app.vo.AnnouncementVO;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.ThreaddService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/thread")
public class ThreadController {

    private final ThreaddService threaddService;

    private final SecurityUtils security;


    @GetMapping("/info/topic")
    public Result<PageEntity<ThreadVO>> getThreadsByTopicId(@RequestParam("page_num")Integer pageNum,
                                                          @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                          @RequestParam(name = "topic_id") Integer topicId) {
        return Result.dataMessageHandler(() -> threaddService.getThreadVOsByTopicId(topicId, pageNum, pageSize), "获取失败");
    }

    @GetMapping("/info/user")
    public Result<PageEntity<ThreadVO>> getThreadsByUserId(@RequestParam(name = "user_id") Integer userId,
                                                 @RequestParam(name = "page") Integer page,
                                                 @RequestParam(name = "page_size") Integer size) {
        return Result.dataMessageHandler(() -> threaddService.getThreadPagesByUserId(userId, page, size), "获取失败");
    }

    // 注意调度
    @GetMapping("/info")
    public Result<ThreadVO> getThreadById(@RequestParam(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> threaddService.getThreadById(threadId), "获取失败");
    }

    @GetMapping("/info/announcement")
    public Result<List<AnnouncementVO>> getAnnouncementByTopicId(@RequestParam(name = "topic_id") String topicId) {
        return Result.dataMessageHandler(() -> threaddService.getAnnouncementThreads(Integer.parseInt(topicId)), "获取失败");
    }

    @PostMapping("/post_thread")
    public Result<Void> postThread(@Valid @RequestBody ThreadDTO threadDTO) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> threaddService.insertThread(threadDTO, userId));
    }

    @DeleteMapping("/remove_thread")
    public Result<Void> removeThreadById(@RequestParam(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> threaddService.removeThreadById(threadId, userId));
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_UPDATE_TAG')" +
            "and hasAuthority('TOPIC_' + #tagUpdateDTO.topicId)")
    @PostMapping("/perm/update_tag")
    public Result<Void> updateTag(@RequestBody TagUpdateDTO tagUpdateDTO) {
        return Result.messageHandler(() -> threaddService.updateThreadTag(tagUpdateDTO));
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_UPDATE_TAG')" +
            "and hasAuthority('TOPIC_' + #konekuto.topicId)")
    @PostMapping("/perm/delete_tag")
    public Result<Void> deleteTreadTag(@RequestBody Konekuto konekuto) {
        return Result.messageHandler(() -> threaddService.removeThreadTag(konekuto.getThreadId(), konekuto.getTopicId()));
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_UPDATE_TAG')" +
            "and hasAuthority('TOPIC_' + #konekuto.topicId)")
    @PostMapping("/perm/set_announcement")
    public Result<Void> setAnnouncement(@RequestBody Konekuto konekuto) {
        return Result.messageHandler(() -> threaddService.setAnnouncementByThreadId(konekuto.getThreadId(), konekuto.getTopicId()));
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_UPDATE_TAG')" +
            "and hasAuthority('TOPIC_' + #konekuto.getTopicId())")
    @PostMapping("/perm/unset_announcement")
    public Result<Void> unsetAnnouncement(@RequestBody Konekuto konekuto) {
        return Result.messageHandler(() -> threaddService.removeAnnouncementByThreadId(konekuto.getThreadId(), konekuto.getTopicId()));
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_sDELETE_THREAD')" +
            "and hasAuthority('TOPIC_' + #konekuto.getTopicId())")
    @PostMapping("/perm/remove_thread")
    public Result<Void> removeThreadByIdPermission(@RequestBody Konekuto konekuto) {
        return Result.messageHandler(() -> threaddService.permRemoveThreadById(konekuto.getThreadId()));
    }

    // You see but you do not observe
    @PostMapping("/view")
    public Result<Void> viewThread(@RequestParam(name = "thread_id") Integer threadId) {
        return Result.messageHandler(() -> threaddService.updateViewCount(threadId));
    }


}
