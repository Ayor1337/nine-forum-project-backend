package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ContentImageRef;
import com.ayor.entity.pojo.ImageAsset;
import com.ayor.entity.pojo.ImageAssetFavorite;
import com.ayor.entity.vo.StickerVO;
import com.ayor.image.ProcessedImage;
import com.ayor.image.ImageProcessor;
import com.ayor.image.ImageStorageService;
import com.ayor.image.StoredImage;
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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 表情包资源业务实现。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ImageAssetServiceImpl extends ServiceImpl<ImageAssetMapper, ImageAsset> implements ImageAssetService {

    private final ImageAssetFavoriteMapper imageAssetFavoriteMapper;

    private final ContentImageRefMapper contentImageRefMapper;

    private final ImageStorageService imageStorageService;

    private final ImageProcessor imageProcessor;

    private final MinioService minioService;

    private final TipTapUtils tipTapUtils;

    @Override
    public String upload(Integer accountId, Base64Upload upload) {
        if (accountId == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        StoredImage storedImage = imageStorageService.storeStickerBase64Image(upload, "stickers/" + accountId + "/");
        ImageAsset asset = buildAsset(accountId, storedImage, ImageAssetSourceType.UPLOAD.name(), ImageAssetType.STICKER.name(), ImageAssetVisibility.PRIVATE.name());
        this.save(asset);
        imageAssetFavoriteMapper.insert(new ImageAssetFavorite(null, accountId, asset.getAssetId(), new Date()));
        refreshAddedCount(asset.getAssetId());
        return asset.getUrl();
    }

    @Override
    public PageEntity<StickerVO> getStickers(Integer accountId, Integer pageNum, Integer pageSize) {
        if (accountId == null) {
            return new PageEntity<>(0L, List.of());
        }
        int normalizedPage = normalizePage(pageNum);
        int normalizedSize = normalizeSize(pageSize);
        long offset = (long) (normalizedPage - 1) * normalizedSize;
        List<ImageAsset> assets = this.baseMapper.selectActiveStickers(accountId, normalizedSize, offset);
        Long total = this.baseMapper.countActiveStickers(accountId);
        return new PageEntity<>(total, assets.stream().map(asset -> toVO(asset, true)).toList());
    }

    @Override
    public String addSticker(Integer accountId, Integer assetId) {
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
        if (imageAssetFavoriteMapper.findMembership(accountId, assetId) != null) {
            return null;
        }
        imageAssetFavoriteMapper.insert(new ImageAssetFavorite(null, accountId, assetId, new Date()));
        refreshAddedCount(assetId);
        return null;
    }

    @Override
    public String addStickerByUrl(Integer accountId, String url) {
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
            return "仅支持添加平台内的图片";
        }
        return addSticker(accountId, asset.getAssetId());
    }

    @Override
    public String removeSticker(Integer accountId, Integer assetId) {
        if (accountId == null) {
            return "用户不存在";
        }
        ImageAsset asset = this.getById(assetId);
        if (asset == null || !ImageAssetType.STICKER.name().equals(asset.getAssetType())) {
            return null;
        }
        ImageAssetFavorite membership = imageAssetFavoriteMapper.findMembership(accountId, assetId);
        if (membership == null) {
            return null;
        }
        imageAssetFavoriteMapper.deleteById(membership.getFavoriteId());
        refreshAddedCount(assetId);
        return null;
    }

    @Override
    public String deleteStickerResource(Integer accountId, Integer assetId) {
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
        ImageAssetFavorite ownerMembership = imageAssetFavoriteMapper.findMembership(accountId, assetId);
        refreshAddedCount(assetId);
        refreshUseCount(assetId);
        ImageAsset latestAsset = this.getById(assetId);
        int selfMembershipCount = ownerMembership == null ? 0 : 1;
        if (latestAsset.getFavoriteCount() <= selfMembershipCount && latestAsset.getUseCount() == 0) {
            deleteObjectIfNecessary(latestAsset);
            this.removeById(assetId);
            return null;
        }
        latestAsset.setStatus(ImageAssetStatus.DISABLED.name());
        latestAsset.setUpdateTime(new Date());
        return this.updateById(latestAsset) ? null : "删除资源失败";
    }

    @Override
    public StickerVO getDetail(Integer accountId, Integer assetId) {
        ImageAsset asset = this.getById(assetId);
        if (asset == null) {
            return null;
        }
        if (!ImageAssetType.STICKER.name().equals(asset.getAssetType())) {
            return null;
        }
        boolean added = accountId != null
                && imageAssetFavoriteMapper.findMembership(accountId, assetId) != null;
        return toVO(asset, added);
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
        ProcessedImage image = imageProcessor.inspectStoredImage(bytes, normalizedUrl);
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
        ProcessedImage sourceImage = imageProcessor.inspectStoredImage(bytes, normalizedUrl);
        if ("gif".equalsIgnoreCase(sourceImage.getOriginalExt())) {
            throw new IllegalArgumentException("GIF 或其他动图暂不支持添加到表情");
        }

        String dataUrl = "data:" + sourceImage.getMimeType() + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
        StoredImage stickerImage = imageStorageService.storeStickerBase64Image(
                new Base64Upload(dataUrl, "sticker." + sourceImage.getOriginalExt()),
                "stickers/" + accountId + "/"
        );
        ImageAsset asset = buildAsset(accountId, stickerImage, ImageAssetSourceType.CONTENT.name(), ImageAssetType.STICKER.name(), ImageAssetVisibility.PRIVATE.name());
        this.save(asset);
        return asset;
    }

    private ImageAsset buildAsset(Integer accountId,
                                  ProcessedImage image,
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
        if (image instanceof StoredImage storedStaticImage) {
            asset.setUrl(storedStaticImage.getUrl());
            asset.setObjectPath(storedStaticImage.getObjectName());
        }
        return asset;
    }

    private StickerVO toVO(ImageAsset asset, boolean added) {
        StickerVO vo = new StickerVO();
        BeanUtils.copyProperties(asset, vo);
        vo.setAddedCount(asset.getFavoriteCount());
        vo.setAdded(added);
        vo.setAvailable(ImageAssetStatus.ACTIVE.name().equals(asset.getStatus()));
        return vo;
    }

    private void refreshAddedCount(Integer assetId) {
        this.baseMapper.refreshAddedCount(assetId);
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
