package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ImageAsset;
import com.ayor.entity.pojo.ImageAssetFavorite;
import com.ayor.entity.vo.StickerVO;
import com.ayor.image.ImageProcessor;
import com.ayor.image.ImageStorageService;
import com.ayor.image.StoredImage;
import com.ayor.mapper.ContentImageRefMapper;
import com.ayor.mapper.ImageAssetFavoriteMapper;
import com.ayor.mapper.ImageAssetMapper;
import com.ayor.minio.MinioService;
import com.ayor.type.ImageAssetStatus;
import com.ayor.type.ImageAssetType;
import com.ayor.util.TipTapUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageAssetServiceImplTest {

    @Mock
    private ImageAssetMapper imageAssetMapper;

    @Mock
    private ImageAssetFavoriteMapper imageAssetFavoriteMapper;

    @Mock
    private ContentImageRefMapper contentImageRefMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private ImageProcessor imageProcessor;

    @Mock
    private MinioService minioService;

    @Test
    void shouldAddUploadedStickerToCurrentUsersLibrary() {
        ImageAssetServiceImpl service = spy(createService());
        Base64Upload upload = new Base64Upload("data:image/png;base64,abc", "cat.png");
        StoredImage storedImage = createStoredImage("https://cdn.example.com/stickers/7/cat.webp");

        when(imageStorageService.storeStickerBase64Image(upload, "stickers/7/")).thenReturn(storedImage);
        doAnswer(invocation -> {
            ImageAsset asset = invocation.getArgument(0);
            asset.setAssetId(15);
            return true;
        }).when(service).save(any(ImageAsset.class));

        String result = service.upload(7, upload);

        assertEquals("https://cdn.example.com/stickers/7/cat.webp", result);
        boolean insertedMembership = mockingDetails(imageAssetFavoriteMapper).getInvocations().stream()
                .filter(invocation -> "insert".equals(invocation.getMethod().getName()))
                .map(invocation -> invocation.getArgument(0))
                .filter(ImageAssetFavorite.class::isInstance)
                .map(ImageAssetFavorite.class::cast)
                .anyMatch(relation -> relation.getAccountId().equals(7) && relation.getAssetId().equals(15));
        assertTrue(insertedMembership);
        verify(imageAssetMapper).refreshAddedCount(15);
    }

    @Test
    void shouldListCurrentUsersStickerLibrary() {
        ImageAssetServiceImpl service = createService();
        ImageAsset asset = createStickerAsset(22, 3);

        when(imageAssetMapper.selectActiveStickers(8, 12, 0)).thenReturn(List.of(asset));
        when(imageAssetMapper.countActiveStickers(8)).thenReturn(1L);

        PageEntity<StickerVO> result = service.getStickers(8, 1, 12);

        assertEquals(1L, result.getTotalSize());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().get(0).getAdded());
        assertTrue(result.getData().get(0).getAvailable());
    }

    @Test
    void shouldRemoveStickerFromCurrentUsersLibraryWithoutDeletingResource() {
        ImageAssetServiceImpl service = spy(createService());
        ImageAsset asset = createStickerAsset(22, 3);
        ImageAssetFavorite relation = new ImageAssetFavorite(5, 8, 22, new Date());

        when(imageAssetMapper.selectById(22)).thenReturn(asset);
        when(imageAssetFavoriteMapper.findMembership(8, 22)).thenReturn(relation);

        String result = service.removeSticker(8, 22);

        assertNull(result);
        boolean deletedMembership = mockingDetails(imageAssetFavoriteMapper).getInvocations().stream()
                .anyMatch(invocation -> "deleteById".equals(invocation.getMethod().getName())
                        && invocation.getArguments().length == 1
                        && Integer.valueOf(5).equals(invocation.getArgument(0)));
        assertTrue(deletedMembership);
        verify(imageAssetMapper).refreshAddedCount(22);
        verify(service, never()).removeById(22);
        verify(imageAssetMapper, never()).updateById(any(ImageAsset.class));
    }

    @Test
    void shouldDeleteStickerResourceInsteadOfOnlyRemovingMembership() {
        ImageAssetServiceImpl service = spy(createService());
        ImageAsset asset = createStickerAsset(22, 8);
        asset.setFavoriteCount(1);
        asset.setUseCount(0);
        asset.setUrl("https://cdn.example.com/stickers/8/cat.webp");
        ImageAssetFavorite ownerMembership = new ImageAssetFavorite(5, 8, 22, new Date());

        when(imageAssetMapper.selectById(22)).thenReturn(asset);
        when(imageAssetFavoriteMapper.findMembership(8, 22)).thenReturn(ownerMembership);
        when(minioService.isOwnObjectUrl(asset.getUrl())).thenReturn(false);
        doReturn(true).when(service).removeById(22);

        String result = service.deleteStickerResource(8, 22);

        assertNull(result);
        verify(service).removeById(22);
        boolean deletedMembership = mockingDetails(imageAssetFavoriteMapper).getInvocations().stream()
                .anyMatch(invocation -> "deleteById".equals(invocation.getMethod().getName()));
        assertFalse(deletedMembership);
        verify(imageAssetFavoriteMapper).findMembership(8, 22);
    }

    private ImageAssetServiceImpl createService() {
        ImageAssetServiceImpl service = new ImageAssetServiceImpl(
                imageAssetFavoriteMapper,
                contentImageRefMapper,
                imageStorageService,
                imageProcessor,
                minioService,
                new TipTapUtils()
        );
        ReflectionTestUtils.setField(service, "baseMapper", imageAssetMapper);
        return service;
    }

    private StoredImage createStoredImage(String url) {
        StoredImage image = new StoredImage();
        image.setUrl(url);
        image.setObjectName("stickers/7/cat.webp");
        image.setOriginalExt("png");
        image.setOutputExt("webp");
        image.setMimeType("image/webp");
        image.setFileSize(1234L);
        image.setWidth(512);
        image.setHeight(320);
        image.setSha256("hash");
        image.setBytes(new byte[]{1, 2, 3});
        return image;
    }

    private ImageAsset createStickerAsset(Integer assetId, Integer ownerId) {
        ImageAsset asset = new ImageAsset();
        asset.setAssetId(assetId);
        asset.setAccountId(ownerId);
        asset.setAssetType(ImageAssetType.STICKER.name());
        asset.setStatus(ImageAssetStatus.ACTIVE.name());
        asset.setUrl("https://cdn.example.com/stickers/" + ownerId + "/cat.webp");
        asset.setOutputExt("webp");
        asset.setFavoriteCount(1);
        asset.setUseCount(0);
        asset.setWidth(512);
        asset.setHeight(320);
        asset.setFileSize(1234L);
        asset.setCreateTime(new Date());
        return asset;
    }
}
