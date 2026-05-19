package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ActivityVO;
import com.ayor.entity.vo.DashboardOverviewVO;
import com.ayor.result.Result;
import com.ayor.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<DashboardOverviewVO> getOverview() {
        return Result.dataMessageHandler(dashboardService::getOverview, "获取仪表盘总览失败");
    }

    @GetMapping("/activities")
    public Result<PageEntity<ActivityVO>> getActivities(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                        @RequestParam(value = "page_size", defaultValue = "20") Integer pageSize) {
        return Result.dataMessageHandler(() -> dashboardService.getActivities(pageNum, pageSize), "获取最近动态失败");
    }
}
