package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.DecorationDTO;
import com.ayor.entity.pojo.Decoration;
import com.ayor.entity.vo.DecorationVO;
import com.ayor.image.ImageStorageService;
import com.ayor.image.ImageUploadException;
import com.ayor.image.StoredImage;
import com.ayor.mapper.DecorationMapper;
import com.ayor.service.DecorationService;
import com.ayor.type.DecorationStatus;
import com.ayor.type.ShopItemType;
import com.ayor.util.DecorationConfigValidator;
import com.ayor.util.SecurityUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Objects;

/**
 * 装扮低代码平台管理服务实现（管理端）。
 * 生命周期：DRAFT → PUBLISHED → ARCHIVED；发布时将 draft_config 复制到
 * published_config，编辑已发布装扮只改 draft_config，不影响线上展示。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DecorationServiceImpl extends ServiceImpl<DecorationMapper, Decoration> implements DecorationService {

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ImageStorageService imageStorageService;

    private final SecurityUtils securityUtils;

    @Override
    public String createDecoration(DecorationDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getName()) || !StringUtils.hasText(dto.getDecorationKey())
                || dto.getType() == null) {
            return "参数错误";
        }
        if (baseMapper.countByDecorationKey(dto.getDecorationKey().trim(), null) > 0) {
            return "装扮关键字已存在";
        }
        String configError = validateConfig(dto.getType(), dto.getDraftConfig());
        if (configError != null) {
            return configError;
        }
        Decoration decoration = new Decoration();
        applyDto(decoration, dto);
        decoration.setStatus(DecorationStatus.DRAFT.getCode());
        decoration.setVersion(0);
        decoration.setCreatedBy(securityUtils.getOptionalSecurityUserId());
        decoration.setIsDeleted(false);
        this.save(decoration);
        return null;
    }

    @Override
    public String updateDecoration(Integer decorationId, DecorationDTO dto) {
        if (decorationId == null || dto == null || !StringUtils.hasText(dto.getDecorationKey())) {
            return "参数错误";
        }
        Decoration decoration = this.getById(decorationId);
        if (decoration == null || Boolean.TRUE.equals(decoration.getIsDeleted())) {
            return "装扮不存在";
        }
        if (Objects.equals(decoration.getStatus(), DecorationStatus.ARCHIVED.getCode())) {
            return "已归档装扮不可编辑";
        }
        if (baseMapper.countByDecorationKey(dto.getDecorationKey().trim(), decorationId) > 0) {
            return "装扮关键字已存在";
        }
        String configError = validateConfig(dto.getType(), dto.getDraftConfig());
        if (configError != null) {
            return configError;
        }
        applyDto(decoration, dto);
        this.updateById(decoration);
        return null;
    }

    @Override
    public String publishDecoration(Integer decorationId) {
        if (decorationId == null) {
            return "参数错误";
        }
        Decoration decoration = this.getById(decorationId);
        if (decoration == null || Boolean.TRUE.equals(decoration.getIsDeleted())) {
            return "装扮不存在";
        }
        if (Objects.equals(decoration.getStatus(), DecorationStatus.ARCHIVED.getCode())) {
            return "已归档装扮不可发布";
        }
        if (!StringUtils.hasText(decoration.getDraftConfig())) {
            return "草稿配置为空，无法发布";
        }
        String configError = DecorationConfigValidator.validate(decoration.getType(), decoration.getDraftConfig());
        if (configError != null) {
            return configError;
        }
        decoration.setPublishedConfig(decoration.getDraftConfig());
        decoration.setStatus(DecorationStatus.PUBLISHED.getCode());
        decoration.setPublishedAt(new Date());
        this.updateById(decoration);
        return null;
    }

    @Override
    public String archiveDecoration(Integer decorationId) {
        if (decorationId == null) {
            return "参数错误";
        }
        Decoration decoration = this.getById(decorationId);
        if (decoration == null || Boolean.TRUE.equals(decoration.getIsDeleted())) {
            return "装扮不存在";
        }
        if (!Objects.equals(decoration.getStatus(), DecorationStatus.PUBLISHED.getCode())) {
            return "仅已发布装扮可归档";
        }
        decoration.setStatus(DecorationStatus.ARCHIVED.getCode());
        this.updateById(decoration);
        return null;
    }

    @Override
    public String deleteDecoration(Integer decorationId) {
        if (decorationId == null) {
            return "参数错误";
        }
        Decoration decoration = this.getById(decorationId);
        if (decoration == null || Boolean.TRUE.equals(decoration.getIsDeleted())) {
            return "装扮不存在";
        }
        if (!Objects.equals(decoration.getStatus(), DecorationStatus.DRAFT.getCode())) {
            return "仅草稿状态的装扮可删除";
        }
        decoration.setIsDeleted(true);
        this.updateById(decoration);
        return null;
    }

    @Override
    public PageEntity<DecorationVO> listDecorations(Integer pageNum,
                                                    Integer pageSize,
                                                    String name,
                                                    ShopItemType type,
                                                    DecorationStatus status) {
        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        String normalizedName = StringUtils.hasText(name) ? name.trim() : null;
        String typeValue = type == null ? null : type.getType();
        Integer statusValue = status == null ? null : status.getCode();
        return new PageEntity<>(
                baseMapper.countDecorations(normalizedName, typeValue, statusValue),
                baseMapper.selectDecorations(offset, normalizedPageSize, normalizedName, typeValue, statusValue));
    }

    @Override
    public DecorationVO getDecoration(Integer decorationId) {
        if (decorationId == null) {
            return null;
        }
        Decoration decoration = this.getById(decorationId);
        if (decoration == null || Boolean.TRUE.equals(decoration.getIsDeleted())) {
            return null;
        }
        DecorationVO vo = new DecorationVO();
        BeanUtils.copyProperties(decoration, vo);
        return vo;
    }

    @Override
    public String uploadAsset(Base64Upload upload) {
        if (upload == null || !StringUtils.hasText(upload.getBase64())) {
            return null;
        }
        try {
            StoredImage storedImage = imageStorageService.storeImageBase64Image(upload, "decoration/");
            return storedImage == null ? null : storedImage.getUrl();
        } catch (ImageUploadException exception) {
            throw exception;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 配置为空（保留草稿）时跳过校验，有内容时必须通过结构校验。
     */
    private String validateConfig(ShopItemType type, String draftConfig) {
        if (!StringUtils.hasText(draftConfig)) {
            return null;
        }
        return DecorationConfigValidator.validate(type == null ? null : type.getType(), draftConfig);
    }

    private void applyDto(Decoration decoration, DecorationDTO dto) {
        decoration.setName(dto.getName().trim());
        decoration.setDecorationKey(dto.getDecorationKey().trim());
        decoration.setDescription(dto.getDescription());
        decoration.setType(dto.getType().getType());
        decoration.setDraftConfig(StringUtils.hasText(dto.getDraftConfig()) ? dto.getDraftConfig() : null);
    }
}
