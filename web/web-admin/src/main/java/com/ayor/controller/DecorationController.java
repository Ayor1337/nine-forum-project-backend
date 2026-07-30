package com.ayor.controller;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.DecorationDTO;
import com.ayor.entity.vo.DecorationVO;
import com.ayor.result.Result;
import com.ayor.service.DecorationService;
import com.ayor.type.DecorationStatus;
import com.ayor.type.ShopItemType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/decorations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "装扮管理", description = "低代码装扮设计、发布与素材上传接口")
public class DecorationController {

    private final DecorationService decorationService;

    @PostMapping
    @Operation(summary = "创建装扮（初始草稿）")
    public Result<Void> createDecoration(@RequestBody @Valid DecorationDTO dto) {
        return Result.messageHandler(() -> decorationService.createDecoration(dto));
    }

    @PutMapping("/{decoration_id}")
    @Operation(summary = "保存草稿配置")
    public Result<Void> updateDecoration(
            @Parameter(description = "装扮ID") @PathVariable("decoration_id") Integer decorationId,
            @RequestBody @Valid DecorationDTO dto) {
        return Result.messageHandler(() -> decorationService.updateDecoration(decorationId, dto));
    }

    @PostMapping("/{decoration_id}/publish")
    @Operation(summary = "发布装扮")
    public Result<Void> publishDecoration(
            @Parameter(description = "装扮ID") @PathVariable("decoration_id") Integer decorationId) {
        return Result.messageHandler(() -> decorationService.publishDecoration(decorationId));
    }

    @PostMapping("/{decoration_id}/archive")
    @Operation(summary = "归档装扮")
    public Result<Void> archiveDecoration(
            @Parameter(description = "装扮ID") @PathVariable("decoration_id") Integer decorationId) {
        return Result.messageHandler(() -> decorationService.archiveDecoration(decorationId));
    }

    @DeleteMapping("/{decoration_id}")
    @Operation(summary = "删除装扮（软删除，仅草稿可删）")
    public Result<Void> deleteDecoration(
            @Parameter(description = "装扮ID") @PathVariable("decoration_id") Integer decorationId) {
        return Result.messageHandler(() -> decorationService.deleteDecoration(decorationId));
    }

    @GetMapping
    @Operation(summary = "分页查询装扮")
    public Result<PageEntity<DecorationVO>> listDecorations(
            @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1", required = false) Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10", required = false) Integer pageSize,
            @Parameter(description = "装扮名称（模糊）") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "装扮类型") @RequestParam(value = "type", required = false) ShopItemType type,
            @Parameter(description = "装扮状态") @RequestParam(value = "status", required = false) DecorationStatus status) {
        return Result.dataMessageHandler(
                () -> decorationService.listDecorations(pageNum, pageSize, name, type, status),
                "获取装扮列表失败");
    }

    @GetMapping("/{decoration_id}")
    @Operation(summary = "查询装扮详情")
    public Result<DecorationVO> getDecoration(
            @Parameter(description = "装扮ID") @PathVariable("decoration_id") Integer decorationId) {
        return Result.dataMessageHandler(() -> decorationService.getDecoration(decorationId), "装扮不存在");
    }

    @PostMapping("/assets")
    @Operation(summary = "上传装扮素材图片")
    public Result<String> uploadAsset(@RequestBody Base64Upload upload) {
        return Result.dataMessageHandler(() -> decorationService.uploadAsset(upload), "上传素材失败");
    }
}
