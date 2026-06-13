package com.ayor.controller;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.StickerByUrlDTO;
import com.ayor.entity.vo.StickerVO;
import com.ayor.result.Result;
import com.ayor.service.ImageAssetService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户侧表情包库接口。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/stickers")
@Tag(name = "表情包")
public class StickerController {

    private final ImageAssetService imageAssetService;

    private final SecurityUtils securityUtils;

    /**
     * 上传表情包并自动加入当前用户的表情包库。
     */
    @Operation(summary = "上传表情包")
    @PostMapping
    public Result<String> upload(@RequestBody Base64Upload upload) {
        Integer accountId = securityUtils.getSecurityUserId();
        try {
            return Result.ok(imageAssetService.upload(accountId, upload));
        } catch (IllegalArgumentException exception) {
            return Result.fail(500, exception.getMessage());
        }
    }

    /**
     * 分页查询当前用户的表情包库。
     */
    @Operation(summary = "分页查询当前用户的表情包库")
    @GetMapping
    public Result<PageEntity<StickerVO>> getStickers(@Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                     @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "12") Integer pageSize) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> imageAssetService.getStickers(accountId, pageNum, pageSize), "获取表情包库失败");
    }

    /**
     * 将指定表情包加入当前用户的表情包库。
     */
    @Operation(summary = "将指定表情包加入当前用户的表情包库")
    @PostMapping("/{assetId}")
    public Result<Void> addSticker(@Parameter(description = "素材 ID") @PathVariable("assetId") Integer assetId) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> imageAssetService.addSticker(accountId, assetId));
    }

    /**
     * 根据平台图片地址添加表情包。
     */
    @Operation(summary = "根据平台图片地址添加表情包")
    @PostMapping("/by-url")
    public Result<Void> addStickerByUrl(@RequestBody @Valid StickerByUrlDTO dto) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> imageAssetService.addStickerByUrl(accountId, dto.getUrl()));
    }

    /**
     * 从当前用户的表情包库移除指定表情包。
     */
    @Operation(summary = "从当前用户的表情包库移除指定表情包")
    @DeleteMapping("/{assetId}")
    public Result<Void> removeSticker(@Parameter(description = "素材 ID") @PathVariable("assetId") Integer assetId) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> imageAssetService.removeSticker(accountId, assetId));
    }

    /**
     * 查询表情包详情。
     */
    @Operation(summary = "查询表情包详情")
    @GetMapping("/{assetId}")
    public Result<StickerVO> getDetail(@Parameter(description = "素材 ID") @PathVariable("assetId") Integer assetId) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> imageAssetService.getDetail(accountId, assetId), "获取表情包详情失败");
    }
}
