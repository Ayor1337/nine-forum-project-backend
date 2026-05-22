package com.ayor.service;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.StickerVO;

/**
 * 图片资源业务接口。
 */
public interface ImageAssetService {

    /**
     * 上传表情包资源并返回资源地址。
     */
    String upload(Integer accountId, Base64Upload upload);

    /**
     * 查询当前用户已添加的表情包库。
     */
    PageEntity<StickerVO> getStickers(Integer accountId, Integer pageNum, Integer pageSize);

    /**
     * 将指定表情包加入当前用户的表情包库。
     */
    String addSticker(Integer accountId, Integer assetId);

    /**
     * 根据平台图片地址添加或派生表情包。
     */
    String addStickerByUrl(Integer accountId, String url);

    /**
     * 从当前用户的表情包库移除指定表情包。
     */
    String removeSticker(Integer accountId, Integer assetId);

    /**
     * 查询表情包资源详情。
     */
    StickerVO getDetail(Integer accountId, Integer assetId);

    /**
     * 同步指定内容中的图片引用关系。
     */
    void syncContentRefs(String contentType, Integer contentId, String content, Integer accountId);

    /**
     * 清除指定内容的图片引用关系。
     */
    void clearContentRefs(String contentType, Integer contentId);
}
