package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.FeedbackCreateDTO;
import com.ayor.entity.vo.FeedbackVO;
import com.ayor.result.Result;
import com.ayor.service.FeedbackService;
import com.ayor.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
@Tag(name = "意见反馈")
public class FeedbackController {

    private final FeedbackService feedbackService;

    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "提交意见反馈")
    public Result<Void> createFeedback(@RequestBody @Valid FeedbackCreateDTO dto) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> feedbackService.createFeedback(accountId, dto));
    }

    @GetMapping("/me")
    @Operation(summary = "分页查询当前用户的意见反馈")
    public Result<PageEntity<FeedbackVO>> getMyFeedbacks(
            @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量，范围1到100") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(
                () -> feedbackService.getMyFeedbacks(accountId, pageNum, pageSize),
                "获取反馈列表失败");
    }
}
