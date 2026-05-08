package com.ayor.service;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ImageAssetVO;

/**
 * 表情包资源业务接口。
 */
public interface ImageAssetService {

    /**
     * 上传表情包资源并返回资源地址。
     */
    String upload(Integer accountId, Base64Upload upload);

    /**
     * 查询当前用户上传的表情包资源。
     */
    PageEntity<ImageAssetVO> getMine(Integer accountId, Integer pageNum, Integer pageSize);

    /**
     * 查询当前用户收藏的表情包资源。
     */
    PageEntity<ImageAssetVO> getFavorites(Integer accountId, Integer pageNum, Integer pageSize);

    /**
     * 收藏指定表情包资源。
     */
    String favorite(Integer accountId, Integer assetId);

    /**
     * 根据平台图片地址收藏或派生表情包资源。
     */
    String favoriteByUrl(Integer accountId, String url);

    /**
     * 取消收藏表情包资源。
     */
    String unfavorite(Integer accountId, Integer assetId);

    /**
     * 删除当前用户上传的表情包资源。
     */
    String deleteMine(Integer accountId, Integer assetId);

    /**
     * 查询表情包资源详情。
     */
    ImageAssetVO getDetail(Integer accountId, Integer assetId);

    /**
     * 同步指定内容中的图片引用关系。
     */
    void syncContentRefs(String contentType, Integer contentId, String content, Integer accountId);

    /**
     * 清除指定内容的图片引用关系。
     */
    void clearContentRefs(String contentType, Integer contentId);
}
