package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ImageAsset;
import com.ayor.entity.vo.ImageAssetAdminVO;
import com.ayor.mapper.ContentImageRefMapper;
import com.ayor.mapper.ImageAssetFavoriteMapper;
import com.ayor.mapper.ImageAssetMapper;
import com.ayor.minio.MinioService;
import com.ayor.service.ImageAssetService;
import com.ayor.type.ImageAssetStatus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
/**
 * 管理端图片资源业务实现。
 */
public class ImageAssetServiceImpl extends ServiceImpl<ImageAssetMapper, ImageAsset> implements ImageAssetService {

    private final ImageAssetFavoriteMapper imageAssetFavoriteMapper;

    private final ContentImageRefMapper contentImageRefMapper;

    private final MinioService minioService;

    @Override
    public PageEntity<ImageAssetAdminVO> getAssets(Integer accountId, String status, String assetType, Integer pageNum, Integer pageSize) {
        Page<ImageAsset> page = this.page(Page.of(normalizePage(pageNum), normalizeSize(pageSize)),
                new LambdaQueryWrapper<ImageAsset>()
                        .eq(accountId != null, ImageAsset::getAccountId, accountId)
                        .eq(status != null && !status.isBlank(), ImageAsset::getStatus, status)
                        .eq(assetType != null && !assetType.isBlank(), ImageAsset::getAssetType, assetType)
                        .orderByDesc(ImageAsset::getCreateTime));
        List<ImageAssetAdminVO> data = page.getRecords().stream().map(this::toVO).toList();
        return new PageEntity<>(page.getTotal(), data);
    }

    @Override
    public String updateStatus(Integer assetId, String status) {
        if (assetId == null) {
            return "资源不存在";
        }
        if (!ImageAssetStatus.ACTIVE.name().equals(status) && !ImageAssetStatus.DISABLED.name().equals(status)) {
            return "资源状态不合法";
        }
        ImageAsset asset = this.getById(assetId);
        if (asset == null) {
            return "资源不存在";
        }
        asset.setStatus(status);
        return this.updateById(asset) ? null : "更新资源状态失败";
    }

    @Override
    public String forceDelete(Integer assetId) {
        ImageAsset asset = this.getById(assetId);
        if (asset == null) {
            return "资源不存在";
        }
        contentImageRefMapper.deleteByAssetId(assetId);
        imageAssetFavoriteMapper.deleteByAssetId(assetId);
        if (asset.getUrl() != null && minioService.isOwnObjectUrl(asset.getUrl())) {
            try {
                minioService.deleteFile(minioService.extractObjectName(asset.getUrl()));
            } catch (Exception ignored) {
            }
        }
        return this.removeById(assetId) ? null : "强制删除资源失败";
    }

    private ImageAssetAdminVO toVO(ImageAsset asset) {
        ImageAssetAdminVO vo = new ImageAssetAdminVO();
        BeanUtils.copyProperties(asset, vo);
        return vo;
    }

    private int normalizePage(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizeSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 20 : pageSize;
    }
}
