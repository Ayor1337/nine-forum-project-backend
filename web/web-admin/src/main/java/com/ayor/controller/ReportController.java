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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "举报管理", description = "后台举报处理接口")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReportController {

    private final ReportService reportService;

    private final SecurityUtils securityUtils;

    @Operation(summary = "查询后台资源")
    @GetMapping
    public Result<PageEntity<ReportVO>> getReports(@Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                   @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                   @Parameter(description = "状态") @RequestParam(value = "status", required = false) ReportStatus status,
                                                   @Parameter(description = "targetType") @RequestParam(value = "target_type", required = false) ReportTargetType targetType,
                                                   @Parameter(description = "reportType") @RequestParam(value = "report_type", required = false) String reportType,
                                                   @Parameter(description = "reporterAccountId") @RequestParam(value = "reporter_account_id", required = false) Integer reporterAccountId,
                                                   @Parameter(description = "reportedAccountId") @RequestParam(value = "reported_account_id", required = false) Integer reportedAccountId) {
        return Result.dataMessageHandler(
                () -> reportService.getReports(pageNum, pageSize, status, targetType, reportType, reporterAccountId, reportedAccountId),
                "获取举报列表失败");
    }

    @Operation(summary = "执行后台管理操作")
    @GetMapping("/{reportId}")
    public Result<ReportVO> getReportDetail(@Parameter(description = "举报ID") @PathVariable("reportId") Integer reportId) {
        return Result.dataMessageHandler(() -> reportService.getReportDetail(reportId), "获取举报详情失败");
    }

    @Operation(summary = "执行后台管理操作")
    @PutMapping("/{reportId}/status")
    public Result<Void> handleReport(@Parameter(description = "举报ID") @PathVariable("reportId") Integer reportId,
                                     @Parameter(description = "请求体") @RequestBody ReportHandleDTO dto) {
        Integer handlerAccountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> reportService.handleReport(reportId, handlerAccountId, dto));
    }
}
