package com.ayor.controlller;

import com.ayor.entity.app.dto.Konekuto;
import com.ayor.entity.app.dto.TagUpdateDTO;
import com.ayor.entity.app.dto.ThreadDTO;
import com.ayor.entity.app.vo.AnnouncementVO;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.ThreaddService;
import com.ayor.util.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/thread")
public class ThreadController {


    @Resource
    private ThreaddService threaddService;

    @Resource
    private SecurityUtils security;


    @GetMapping("/info/topic/{topic_id}")
    public Result<List<ThreadVO>> getThreadsByTopicId(@PathVariable("topic_id") String topicId) throws InterruptedException {
        return Result.dataMessageHandler(() -> threaddService.getThreadVOsByTopicId(Integer.parseInt(topicId)), "获取失败");
    }


    @GetMapping("/info/user/{user_id}")
    public Result<List<ThreadVO>> getThreadsByUserId(@PathVariable("user_id") String userId) {
        return Result.dataMessageHandler(() -> threaddService.getThreadsByUserId(Integer.parseInt(userId)), "获取失败");
    }

    @GetMapping("/info/{thread_id}")
    public Result<ThreadVO> getThreadTitleById(@PathVariable("thread_id") String threadId) {
        return Result.dataMessageHandler(() -> threaddService.getThreadById(Integer.parseInt(threadId)), "获取失败");
    }

    @GetMapping("/info/announcement/{topic_id}")
    public Result<List<AnnouncementVO>> getAnnouncementByTopicId(@PathVariable("topic_id") String topicId) {
        return Result.dataMessageHandler(() -> threaddService.getAnnouncementThreads(Integer.parseInt(topicId)), "获取失败");
    }

    @PostMapping("/post_thread")
    public Result<Void> postThread(@Valid @RequestBody ThreadDTO threadDTO) {
        String username = security.getSecurityUsername();
        return Result.messageHandler(() -> threaddService.insertThread(threadDTO, username));
    }

    @DeleteMapping("/remove_thread/{thread_id}")
    public Result<Void> removeThreadById(@PathVariable("thread_id") Integer threadId) {
        String username = security.getSecurityUsername();
        return Result.messageHandler(() -> threaddService.removeThreadById(threadId, username));
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
            "or hasAuthority('PERM_DELETE_THREAD')" +
            "and hasAuthority('TOPIC_' + #konekuto.getTopicId())")
    @PostMapping("/perm/remove_thread")
    public Result<Void> removeThreadByIdPermission(@RequestBody Konekuto konekuto) {
        return Result.messageHandler(() -> threaddService.permRemoveThreadById(konekuto.getThreadId()));
    }

    // You see but you do not observe
    @PostMapping("/view/{thread_id}")
    public Result<Void> viewThread(@PathVariable("thread_id") String threadId) {
        return Result.messageHandler(() -> threaddService.updateViewCount(Integer.parseInt(threadId)));
    }
}
