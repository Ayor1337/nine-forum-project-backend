package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ImageAssetStatusUpdateDTO;
import com.ayor.entity.vo.ImageAssetAdminVO;
import com.ayor.result.Result;
import com.ayor.service.ImageAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "图片资源管理", description = "后台图片资源审核和删除接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image-assets")
public class ImageAssetController {

    private final ImageAssetService imageAssetService;

    /**
     * 分页查询图片资源。
     */
    @Operation(summary = "分页查询图片资源")
    @GetMapping
    public Result<PageEntity<ImageAssetAdminVO>> getAssets(@Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                           @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "20") Integer pageSize,
                                                           @Parameter(description = "用户ID") @RequestParam(value = "account_id", required = false) Integer accountId,
                                                           @Parameter(description = "状态") @RequestParam(value = "status", required = false) String status,
                                                           @Parameter(description = "assetType") @RequestParam(value = "asset_type", required = false) String assetType) {
        return Result.dataMessageHandler(() -> imageAssetService.getAssets(accountId, status, assetType, pageNum, pageSize), "获取图片资源列表失败");
    }

    /**
     * 修改图片资源状态。
     */
    @Operation(summary = "修改图片资源状态")
    @PutMapping("/{assetId}/status")
    public Result<Void> updateStatus(@Parameter(description = "图片资源ID") @PathVariable("assetId") Integer assetId,
                                     @Parameter(description = "请求体") @RequestBody ImageAssetStatusUpdateDTO dto) {
        return Result.messageHandler(() -> imageAssetService.updateStatus(assetId, dto.getStatus()));
    }

    /**
     * 强制删除图片资源。
     */
    @Operation(summary = "强制删除图片资源")
    @DeleteMapping("/{assetId}")
    public Result<Void> forceDelete(@Parameter(description = "图片资源ID") @PathVariable("assetId") Integer assetId) {
        return Result.messageHandler(() -> imageAssetService.forceDelete(assetId));
    }
}
