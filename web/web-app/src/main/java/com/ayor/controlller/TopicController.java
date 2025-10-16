package com.ayor.controlller;

import com.ayor.entity.app.dto.TopicDTO;
import com.ayor.entity.app.vo.TopicVO;
import com.ayor.result.Result;
import com.ayor.service.TopicService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topic")
public class TopicController {

    @Resource
    private TopicService topicService;

    @GetMapping("/info/list_by_theme_id/{theme_id}")
    public Result<List<TopicVO>> getTopicList(@PathVariable("theme_id") String themeId) {
        Integer themeIdInt = Integer.parseInt(themeId);
        return Result.dataMessageHandler(() -> topicService.getTopicListByThemeId(themeIdInt), "获取主题下的帖子列表失败");
    }

    @PreAuthorize("hasAnyRole('ROLE_OWNER')")
    @PutMapping("/insert")
    public Result<Void> insertTopic(@RequestBody TopicDTO topicDTO) {
        return Result.messageHandler(() -> topicService.insertTopic(topicDTO));
    }

    @PreAuthorize("hasAnyRole('ROLE_OWNER')")
    @PutMapping("/update")
    public Result<Void> updateTopic(@RequestBody TopicDTO topicDTO) {
        return Result.messageHandler(() -> topicService.updateTopic(topicDTO));
    }

    @PreAuthorize("hasAnyRole('ROLE_OWNER')")
    @DeleteMapping("/delete/{topic_id}")
    public Result<Void> deleteTopic(@PathVariable("topic_id") String topicId) {
        Integer topicIdInt = Integer.parseInt(topicId);
        return Result.messageHandler(() -> topicService.deleteTopic(topicIdInt));
    }

}
