package com.ayor.service;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.DecorationDTO;
import com.ayor.entity.vo.DecorationVO;
import com.ayor.type.DecorationStatus;
import com.ayor.type.ShopItemType;

/**
 * 装扮低代码平台管理服务（管理端）
 */
public interface DecorationService {

    /**
     * 创建装扮（初始 DRAFT）。
     *
     * @param dto 装扮参数
     * @return 成功返回 null，否则返回错误消息
     */
    String createDecoration(DecorationDTO dto);

    /**
     * 保存草稿配置（仅 DRAFT / PUBLISHED 可编辑，不影响线上 published_config）。
     *
     * @param decorationId 装扮ID
     * @param dto 装扮参数
     * @return 成功返回 null，否则返回错误消息
     */
    String updateDecoration(Integer decorationId, DecorationDTO dto);

    /**
     * 发布装扮：draft_config 复制到 published_config，状态置 PUBLISHED。
     *
     * @param decorationId 装扮ID
     * @return 成功返回 null，否则返回错误消息
     */
    String publishDecoration(Integer decorationId);

    /**
     * 归档装扮（仅 PUBLISHED 可归档）。
     *
     * @param decorationId 装扮ID
     * @return 成功返回 null，否则返回错误消息
     */
    String archiveDecoration(Integer decorationId);

    /**
     * 软删除装扮（仅 DRAFT 可删除）。
     *
     * @param decorationId 装扮ID
     * @return 成功返回 null，否则返回错误消息
     */
    String deleteDecoration(Integer decorationId);

    /**
     * 分页查询装扮。
     */
    PageEntity<DecorationVO> listDecorations(Integer pageNum,
                                             Integer pageSize,
                                             String name,
                                             ShopItemType type,
                                             DecorationStatus status);

    /**
     * 查询装扮详情（含 draft 与 published 两份配置）。
     *
     * @param decorationId 装扮ID
     * @return 装扮详情，不存在返回 null
     */
    DecorationVO getDecoration(Integer decorationId);

    /**
     * 上传装扮素材图片，返回可引用的 URL。
     *
     * @param upload Base64 图片
     * @return 图片 URL，失败返回 null
     */
    String uploadAsset(Base64Upload upload);
}
