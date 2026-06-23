package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.FeedbackHandleDTO;
import com.ayor.entity.vo.FeedbackVO;
import com.ayor.result.Result;
import com.ayor.service.FeedbackService;
import com.ayor.type.FeedbackStatus;
import com.ayor.type.FeedbackType;
import com.ayor.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "意见反馈管理", description = "后台意见反馈查询与处理接口")
public class FeedbackController {

    private final FeedbackService feedbackService;

    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "分页查询意见反馈")
    public Result<PageEntity<FeedbackVO>> getFeedbacks(
            @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量，范围1到100") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
            @Parameter(description = "处理状态") @RequestParam(value = "status", required = false) FeedbackStatus status,
            @Parameter(description = "反馈类型") @RequestParam(value = "type", required = false) FeedbackType type,
            @Parameter(description = "提交用户账号ID") @RequestParam(value = "account_id", required = false) Integer accountId) {
        return Result.dataMessageHandler(
                () -> feedbackService.getFeedbacks(pageNum, pageSize, status, type, accountId),
                "获取反馈列表失败");
    }

    @GetMapping("/{feedbackId}")
    @Operation(summary = "查看意见反馈详情")
    public Result<FeedbackVO> getFeedbackDetail(
            @Parameter(description = "反馈ID") @PathVariable("feedbackId") Integer feedbackId) {
        return Result.dataMessageHandler(
                () -> feedbackService.getFeedbackDetail(feedbackId),
                "反馈不存在");
    }

    @PutMapping("/{feedbackId}/status")
    @Operation(summary = "更新意见反馈处理状态")
    public Result<Void> handleFeedback(
            @Parameter(description = "反馈ID") @PathVariable("feedbackId") Integer feedbackId,
            @RequestBody @Valid FeedbackHandleDTO dto) {
        Integer handlerAccountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(
                () -> feedbackService.handleFeedback(feedbackId, handlerAccountId, dto));
    }
}
