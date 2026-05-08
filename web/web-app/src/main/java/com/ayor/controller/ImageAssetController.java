package com.ayor.controller;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ImageAssetFavoriteByUrlDTO;
import com.ayor.entity.vo.ImageAssetVO;
import com.ayor.result.Result;
import com.ayor.service.ImageAssetService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/image-assets")
public class ImageAssetController {

    private final ImageAssetService imageAssetService;

    private final SecurityUtils securityUtils;

    @PostMapping
    /**
     * 上传表情包资源。
     */
    public Result<ImageAssetVO> upload(@RequestBody Base64Upload upload) {
        Integer accountId = securityUtils.getSecurityUserId();
        try {
            return Result.ok(imageAssetService.upload(accountId, upload));
        } catch (IllegalArgumentException exception) {
            return Result.fail(500, exception.getMessage());
        }
    }

    @GetMapping("/mine")
    /**
     * 分页查询我的表情包资源。
     */
    public Result<PageEntity<ImageAssetVO>> getMine(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                    @RequestParam(value = "page_size", defaultValue = "12") Integer pageSize) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> imageAssetService.getMine(accountId, pageNum, pageSize), "获取我的图片资源失败");
    }

    @GetMapping("/favorites")
    /**
     * 分页查询我收藏的表情包资源。
     */
    public Result<PageEntity<ImageAssetVO>> getFavorites(@RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                         @RequestParam(value = "page_size", defaultValue = "12") Integer pageSize) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> imageAssetService.getFavorites(accountId, pageNum, pageSize), "获取收藏图片资源失败");
    }

    @PostMapping("/{assetId}/favorites")
    /**
     * 收藏指定表情包资源。
     */
    public Result<Void> favorite(@PathVariable("assetId") Integer assetId) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> imageAssetService.favorite(accountId, assetId));
    }

    @PostMapping("/favorites/by-url")
    /**
     * 根据平台图片地址收藏或派生表情包资源。
     */
    public Result<Void> favoriteByUrl(@RequestBody @Valid ImageAssetFavoriteByUrlDTO dto) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> imageAssetService.favoriteByUrl(accountId, dto.getUrl()));
    }

    @DeleteMapping("/{assetId}/favorites")
    /**
     * 取消收藏指定表情包资源。
     */
    public Result<Void> unfavorite(@PathVariable("assetId") Integer assetId) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> imageAssetService.unfavorite(accountId, assetId));
    }

    @DeleteMapping("/{assetId}")
    /**
     * 删除当前用户上传的表情包资源。
     */
    public Result<Void> deleteMine(@PathVariable("assetId") Integer assetId) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> imageAssetService.deleteMine(accountId, assetId));
    }

    @GetMapping("/{assetId}")
    /**
     * 查询表情包资源详情。
     */
    public Result<ImageAssetVO> getDetail(@PathVariable("assetId") Integer assetId) {
        Integer accountId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> imageAssetService.getDetail(accountId, assetId), "获取图片资源详情失败");
    }
}
