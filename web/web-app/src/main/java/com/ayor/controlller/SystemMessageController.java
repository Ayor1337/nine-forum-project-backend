package com.ayor.controlller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.SystemMessageVO;
import com.ayor.result.Result;
import com.ayor.service.SystemMessageService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/message")
@RequiredArgsConstructor
public class SystemMessageController {

    private final SystemMessageService systemMessageService;

    private final SecurityUtils securityUtils;

    @GetMapping("/list")
    public Result<PageEntity<SystemMessageVO>> getSystemMessages(@RequestParam("page_num") Integer pageNum,
                                                                   @RequestParam(value = "page_size", defaultValue = "7") Integer pageSize) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> systemMessageService.listSystemMessage(pageNum, pageSize, userId), "获取系统消息成功");
    }

}
