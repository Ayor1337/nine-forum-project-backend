package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ImageAssetAdminVO;

/**
 * 管理端图片资源业务接口。
 */
public interface ImageAssetService {

    /**
     * 分页查询图片资源。
     */
    PageEntity<ImageAssetAdminVO> getAssets(Integer accountId, String status, String assetType, Integer pageNum, Integer pageSize);

    /**
     * 修改图片资源状态。
     */
    String updateStatus(Integer assetId, String status);

    /**
     * 强制删除图片资源。
     */
    String forceDelete(Integer assetId);
}
