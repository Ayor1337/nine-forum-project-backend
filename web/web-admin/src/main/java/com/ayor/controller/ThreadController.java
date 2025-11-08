package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.vo.ThreadTableVO;
import com.ayor.result.Result;
import com.ayor.service.ThreaddService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/thread")
@RequiredArgsConstructor
public class ThreadController {

    private final ThreaddService threaddService;

    @GetMapping("get_threads")
    public Result<PageEntity<ThreadTableVO>> getThreads(@RequestParam("page_num") Integer pageNum,
                                                        @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> threaddService.getThreads(pageNum, pageSize), "获取帖子列表失败");
    }

}
