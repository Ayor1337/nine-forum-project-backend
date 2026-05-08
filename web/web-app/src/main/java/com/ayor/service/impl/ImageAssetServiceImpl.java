package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ContentImageRef;
import com.ayor.entity.pojo.ImageAsset;
import com.ayor.entity.pojo.ImageAssetFavorite;
import com.ayor.entity.vo.ImageAssetVO;
import com.ayor.image.ProcessedStaticImage;
import com.ayor.image.StaticImageProcessor;
import com.ayor.image.StaticImageStorageService;
import com.ayor.image.StoredStaticImage;
import com.ayor.mapper.ContentImageRefMapper;
import com.ayor.mapper.ImageAssetFavoriteMapper;
import com.ayor.mapper.ImageAssetMapper;
import com.ayor.minio.MinioService;
import com.ayor.service.ImageAssetService;
import com.ayor.type.ImageAssetSourceType;
import com.ayor.type.ImageAssetStatus;
import com.ayor.type.ImageAssetType;
import com.ayor.type.ImageAssetVisibility;
import com.ayor.util.TipTapUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
/**
 * 表情包资源业务实现。
 */
public class ImageAssetServiceImpl extends ServiceImpl<ImageAssetMapper, ImageAsset> implements ImageAssetService {

    private final ImageAssetFavoriteMapper imageAssetFavoriteMapper;

    private final ContentImageRefMapper contentImageRefMapper;

    private final StaticImageStorageService staticImageStorageService;

    private final StaticImageProcessor staticImageProcessor;

    private final MinioService minioService;

    private final TipTapUtils tipTapUtils;

    @Override
    public ImageAssetVO upload(Integer accountId, Base64Upload upload) {
        if (accountId == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        StoredStaticImage storedImage = staticImageStorageService.storeStickerBase64Image(upload, "image-assets/" + accountId + "/");
        ImageAsset asset = buildAsset(accountId, storedImage, ImageAssetSourceType.UPLOAD.name(), ImageAssetType.STICKER.name(), ImageAssetVisibility.PRIVATE.name());
        this.save(asset);
        return toVO(asset, false);
    }

    @Override
    public PageEntity<ImageAssetVO> getMine(Integer accountId, Integer pageNum, Integer pageSize) {
        if (accountId == null) {
            return new PageEntity<>(0L, List.of());
        }
        Page<ImageAsset> page = this.page(Page.of(normalizePage(pageNum), normalizeSize(pageSize)),
                new LambdaQueryWrapper<ImageAsset>()
                        .eq(ImageAsset::getAccountId, accountId)
                        .eq(ImageAsset::getAssetType, ImageAssetType.STICKER.name())
                        .eq(ImageAsset::getStatus, ImageAssetStatus.ACTIVE.name())
                        .orderByDesc(ImageAsset::getCreateTime));
        return new PageEntity<>(page.getTotal(), page.getRecords().stream().map(asset -> toVO(asset, false)).toList());
    }

    @Override
    public PageEntity<ImageAssetVO> getFavorites(Integer accountId, Integer pageNum, Integer pageSize) {
        if (accountId == null) {
            return new PageEntity<>(0L, List.of());
        }
        int normalizedPage = normalizePage(pageNum);
        int normalizedSize = normalizeSize(pageSize);
        long offset = (long) (normalizedPage - 1) * normalizedSize;
        List<ImageAsset> assets = this.baseMapper.selectActiveFavorites(accountId, normalizedSize, offset);
        Long total = this.baseMapper.countActiveFavorites(accountId);
        return new PageEntity<>(total, assets.stream().map(asset -> toVO(asset, true)).toList());
    }

    @Override
    public String favorite(Integer accountId, Integer assetId) {
        if (accountId == null) {
            return "用户不存在";
        }
        ImageAsset asset = this.getById(assetId);
        if (asset == null) {
            return "资源不存在";
        }
        if (!ImageAssetType.STICKER.name().equals(asset.getAssetType())) {
            return "该资源不是表情包";
        }
        if (!ImageAssetStatus.ACTIVE.name().equals(asset.getStatus())) {
            return "资源不可用";
        }
        if (imageAssetFavoriteMapper.findByAccountIdAndAssetId(accountId, assetId) != null) {
            return null;
        }
        imageAssetFavoriteMapper.insert(new ImageAssetFavorite(null, accountId, assetId, new Date()));
        refreshFavoriteCount(assetId);
        return null;
    }

    @Override
    public String favoriteByUrl(Integer accountId, String url) {
        if (accountId == null) {
            return "用户不存在";
        }
        ImageAsset asset;
        try {
            asset = getOrCreateStickerAssetByUrl(accountId, url);
        } catch (IllegalArgumentException exception) {
            return exception.getMessage();
        }
        if (asset == null) {
            return "仅支持收藏平台内的图片";
        }
        return favorite(accountId, asset.getAssetId());
    }

    @Override
    public String unfavorite(Integer accountId, Integer assetId) {
        if (accountId == null) {
            return "用户不存在";
        }
        ImageAssetFavorite favorite = imageAssetFavoriteMapper.findByAccountIdAndAssetId(accountId, assetId);
        if (favorite == null) {
            return null;
        }
        imageAssetFavoriteMapper.deleteById(favorite.getFavoriteId());
        refreshFavoriteCount(assetId);
        return null;
    }

    @Override
    public String deleteMine(Integer accountId, Integer assetId) {
        if (accountId == null) {
            return "用户不存在";
        }
        ImageAsset asset = this.getById(assetId);
        if (asset == null) {
            return "资源不存在";
        }
        if (!Objects.equals(asset.getAccountId(), accountId)) {
            return "没有权限";
        }
        if (!ImageAssetType.STICKER.name().equals(asset.getAssetType())) {
            return "只能删除自己的表情包资源";
        }
        refreshFavoriteCount(assetId);
        refreshUseCount(assetId);
        ImageAsset latestAsset = this.getById(assetId);
        if (latestAsset.getFavoriteCount() == 0 && latestAsset.getUseCount() == 0) {
            deleteObjectIfNecessary(latestAsset);
            this.removeById(assetId);
            return null;
        }
        latestAsset.setStatus(ImageAssetStatus.DISABLED.name());
        latestAsset.setUpdateTime(new Date());
        return this.updateById(latestAsset) ? null : "删除资源失败";
    }

    @Override
    public ImageAssetVO getDetail(Integer accountId, Integer assetId) {
        ImageAsset asset = this.getById(assetId);
        if (asset == null) {
            return null;
        }
        if (!ImageAssetType.STICKER.name().equals(asset.getAssetType())) {
            return null;
        }
        boolean favorited = accountId != null
                && imageAssetFavoriteMapper.findByAccountIdAndAssetId(accountId, assetId) != null;
        return toVO(asset, favorited);
    }

    @Override
    public void syncContentRefs(String contentType, Integer contentId, String content, Integer accountId) {
        List<Integer> oldAssetIds = new ArrayList<>(contentImageRefMapper.selectAssetIdsByContent(contentType, contentId));
        contentImageRefMapper.deleteByContent(contentType, contentId);

        Set<Integer> touchedAssetIds = new LinkedHashSet<>(oldAssetIds);
        if (content != null && !content.isBlank()) {
            Set<String> urls = new LinkedHashSet<>(tipTapUtils.extractAllImageUrls(content));
            for (String url : urls) {
                ImageAsset asset = getOrIngestAssetByUrl(url, accountId);
                if (asset == null) {
                    continue;
                }
                touchedAssetIds.add(asset.getAssetId());
                contentImageRefMapper.insertIgnore(new ContentImageRef(null, asset.getAssetId(), contentType, contentId, new Date()));
            }
        }

        touchedAssetIds.forEach(this::refreshUseCount);
    }

    @Override
    public void clearContentRefs(String contentType, Integer contentId) {
        List<Integer> assetIds = new ArrayList<>(contentImageRefMapper.selectAssetIdsByContent(contentType, contentId));
        contentImageRefMapper.deleteByContent(contentType, contentId);
        assetIds.forEach(this::refreshUseCount);
    }

    private ImageAsset getOrIngestAssetByUrl(String rawUrl, Integer accountId) {
        String normalizedUrl = minioService.normalizeUrl(rawUrl);
        if (normalizedUrl == null) {
            return null;
        }
        ImageAsset existing = this.baseMapper.findByUrl(normalizedUrl);
        if (existing != null) {
            return existing;
        }

        byte[] bytes = minioService.getObjectBytes(normalizedUrl);
        ProcessedStaticImage image = staticImageProcessor.inspectStoredImage(bytes, normalizedUrl);
        ImageAsset asset = buildAsset(accountId, image, ImageAssetSourceType.CONTENT.name(), ImageAssetType.IMAGE.name(), ImageAssetVisibility.PUBLIC.name());
        asset.setUrl(normalizedUrl);
        asset.setObjectPath(minioService.extractObjectName(normalizedUrl));
        this.save(asset);
        return asset;
    }

    private ImageAsset getOrCreateStickerAssetByUrl(Integer accountId, String rawUrl) {
        String normalizedUrl = minioService.normalizeUrl(rawUrl);
        if (normalizedUrl == null) {
            return null;
        }
        ImageAsset existing = this.baseMapper.findByUrl(normalizedUrl);
        if (existing != null && ImageAssetType.STICKER.name().equals(existing.getAssetType())) {
            return existing;
        }

        byte[] bytes = minioService.getObjectBytes(normalizedUrl);
        ProcessedStaticImage sourceImage = staticImageProcessor.inspectStoredImage(bytes, normalizedUrl);
        if ("gif".equalsIgnoreCase(sourceImage.getOriginalExt())) {
            throw new IllegalArgumentException("GIF 或其他动图暂不支持添加到表情");
        }

        String dataUrl = "data:" + sourceImage.getMimeType() + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
        StoredStaticImage stickerImage = staticImageStorageService.storeStickerBase64Image(
                new Base64Upload(dataUrl, "sticker." + sourceImage.getOriginalExt()),
                "image-assets/" + accountId + "/"
        );
        ImageAsset asset = buildAsset(accountId, stickerImage, ImageAssetSourceType.CONTENT.name(), ImageAssetType.STICKER.name(), ImageAssetVisibility.PRIVATE.name());
        this.save(asset);
        return asset;
    }

    private ImageAsset buildAsset(Integer accountId,
                                  ProcessedStaticImage image,
                                  String sourceType,
                                  String assetType,
                                  String visibility) {
        Date now = new Date();
        ImageAsset asset = new ImageAsset();
        asset.setAccountId(accountId);
        asset.setOriginalExt(image.getOriginalExt());
        asset.setOutputExt(image.getOutputExt());
        asset.setMimeType(image.getMimeType());
        asset.setFileSize(image.getFileSize());
        asset.setWidth(image.getWidth());
        asset.setHeight(image.getHeight());
        asset.setSha256(image.getSha256());
        asset.setSourceType(sourceType);
        asset.setAssetType(assetType);
        asset.setVisibility(visibility);
        asset.setStatus(ImageAssetStatus.ACTIVE.name());
        asset.setFavoriteCount(0);
        asset.setUseCount(0);
        asset.setCreateTime(now);
        asset.setUpdateTime(now);
        if (image instanceof StoredStaticImage storedStaticImage) {
            asset.setUrl(storedStaticImage.getUrl());
            asset.setObjectPath(storedStaticImage.getObjectName());
        }
        return asset;
    }

    private ImageAssetVO toVO(ImageAsset asset, boolean favorited) {
        ImageAssetVO vo = new ImageAssetVO();
        BeanUtils.copyProperties(asset, vo);
        vo.setFavorited(favorited);
        vo.setAvailable(ImageAssetStatus.ACTIVE.name().equals(asset.getStatus()));
        return vo;
    }

    private void refreshFavoriteCount(Integer assetId) {
        this.baseMapper.refreshFavoriteCount(assetId);
    }

    private void refreshUseCount(Integer assetId) {
        this.baseMapper.refreshUseCount(assetId);
    }

    private void deleteObjectIfNecessary(ImageAsset asset) {
        if (asset.getUrl() == null || !minioService.isOwnObjectUrl(asset.getUrl())) {
            return;
        }
        try {
            minioService.deleteFile(minioService.extractObjectName(asset.getUrl()));
        } catch (Exception ignored) {
        }
    }

    private int normalizePage(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizeSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 12 : pageSize;
    }
}
