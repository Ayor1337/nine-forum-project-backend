package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ReportHandleDTO;
import com.ayor.entity.vo.ReportVO;
import com.ayor.result.Result;
import com.ayor.service.ReportService;
import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReportController {

    private final ReportService reportService;

    private final SecurityUtils securityUtils;

    @GetMapping
    public Result<PageEntity<ReportVO>> getReports(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                   @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                   @RequestParam(value = "status", required = false) ReportStatus status,
                                                   @RequestParam(value = "target_type", required = false) ReportTargetType targetType,
                                                   @RequestParam(value = "report_type", required = false) String reportType,
                                                   @RequestParam(value = "reporter_account_id", required = false) Integer reporterAccountId,
                                                   @RequestParam(value = "reported_account_id", required = false) Integer reportedAccountId) {
        return Result.dataMessageHandler(
                () -> reportService.getReports(pageNum, pageSize, status, targetType, reportType, reporterAccountId, reportedAccountId),
                "获取举报列表失败");
    }

    @GetMapping("/{reportId}")
    public Result<ReportVO> getReportDetail(@PathVariable("reportId") Integer reportId) {
        return Result.dataMessageHandler(() -> reportService.getReportDetail(reportId), "获取举报详情失败");
    }

    @PutMapping("/{reportId}/status")
    public Result<Void> handleReport(@PathVariable("reportId") Integer reportId,
                                     @RequestBody ReportHandleDTO dto) {
        Integer handlerAccountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> reportService.handleReport(reportId, handlerAccountId, dto));
    }
}
