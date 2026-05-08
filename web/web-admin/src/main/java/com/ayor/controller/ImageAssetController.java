package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ImageAssetStatusUpdateDTO;
import com.ayor.entity.vo.ImageAssetAdminVO;
import com.ayor.result.Result;
import com.ayor.service.ImageAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image-assets")
public class ImageAssetController {

    private final ImageAssetService imageAssetService;

    @GetMapping
    /**
     * 分页查询图片资源。
     */
    public Result<PageEntity<ImageAssetAdminVO>> getAssets(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                           @RequestParam(value = "page_size", defaultValue = "20") Integer pageSize,
                                                           @RequestParam(value = "account_id", required = false) Integer accountId,
                                                           @RequestParam(value = "status", required = false) String status,
                                                           @RequestParam(value = "asset_type", required = false) String assetType) {
        return Result.dataMessageHandler(() -> imageAssetService.getAssets(accountId, status, assetType, pageNum, pageSize), "获取图片资源列表失败");
    }

    @PutMapping("/{assetId}/status")
    /**
     * 修改图片资源状态。
     */
    public Result<Void> updateStatus(@PathVariable("assetId") Integer assetId,
                                     @RequestBody ImageAssetStatusUpdateDTO dto) {
        return Result.messageHandler(() -> imageAssetService.updateStatus(assetId, dto.getStatus()));
    }

    @DeleteMapping("/{assetId}")
    /**
     * 强制删除图片资源。
     */
    public Result<Void> forceDelete(@PathVariable("assetId") Integer assetId) {
        return Result.messageHandler(() -> imageAssetService.forceDelete(assetId));
    }
}
