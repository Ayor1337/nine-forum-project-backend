package com.ayor.service.impl;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.DecorationDTO;
import com.ayor.entity.pojo.Decoration;
import com.ayor.entity.vo.DecorationVO;
import com.ayor.image.ImageStorageService;
import com.ayor.image.StoredImage;
import com.ayor.mapper.DecorationMapper;
import com.ayor.type.DecorationStatus;
import com.ayor.type.ShopItemType;
import com.ayor.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecorationServiceImplTest {

    private static final String FRAME_CONFIG = """
            {"schemaVersion": 2, "mode": "css", "border": {"width": 0.08, "color": "#ffd700"},
             "animation": {"type": "rotate", "durationMs": 2000}, "scale": 1.1}
            """;

    @Mock
    private DecorationMapper decorationMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private SecurityUtils securityUtils;

    private DecorationServiceImpl decorationService;

    @BeforeEach
    void setUp() {
        decorationService = new DecorationServiceImpl(imageStorageService, securityUtils);
        ReflectionTestUtils.setField(decorationService, "baseMapper", decorationMapper);
    }

    // 测试创建装扮时字段映射正确，初始为草稿
    @Test
    void shouldCreateDecorationAsDraft() {
        when(decorationMapper.countByDecorationKey("star_track_frame", null)).thenReturn(0L);
        when(securityUtils.getOptionalSecurityUserId()).thenReturn(1);

        String result = decorationService.createDecoration(decorationDto());

        assertThat(result).isNull();
        ArgumentCaptor<Decoration> captor = ArgumentCaptor.forClass(Decoration.class);
        verify(decorationMapper).insert(captor.capture());
        Decoration decoration = captor.getValue();
        assertThat(decoration.getName()).isEqualTo("头像框·星轨");
        assertThat(decoration.getDecorationKey()).isEqualTo("star_track_frame");
        assertThat(decoration.getType()).isEqualTo("avatar_frame");
        assertThat(decoration.getDraftConfig()).isEqualTo(FRAME_CONFIG);
        assertThat(decoration.getStatus()).isEqualTo(DecorationStatus.DRAFT.getCode());
        assertThat(decoration.getVersion()).isEqualTo(0);
        assertThat(decoration.getCreatedBy()).isEqualTo(1);
        assertThat(decoration.getIsDeleted()).isFalse();
    }

    // 测试创建装扮关键字重复时拒绝
    @Test
    void shouldRejectCreateWhenKeyDuplicated() {
        when(decorationMapper.countByDecorationKey("star_track_frame", null)).thenReturn(1L);

        assertThat(decorationService.createDecoration(decorationDto())).isEqualTo("装扮关键字已存在");
        verify(decorationMapper, never()).insert(any(Decoration.class));
    }

    // 测试创建装扮参数缺失或配置非法时拒绝
    @Test
    void shouldRejectCreateWhenParamsOrConfigInvalid() {
        assertThat(decorationService.createDecoration(null)).isEqualTo("参数错误");
        DecorationDTO dto = decorationDto();
        dto.setName("  ");
        assertThat(decorationService.createDecoration(dto)).isEqualTo("参数错误");

        DecorationDTO invalidConfig = decorationDto();
        invalidConfig.setDraftConfig("{\"mode\": \"css\"}");
        assertThat(decorationService.createDecoration(invalidConfig)).isEqualTo("schemaVersion 缺失或不合法");
        verify(decorationMapper, never()).insert(any(Decoration.class));
    }

    // 测试更新装扮只改草稿配置，不影响已发布配置
    @Test
    void shouldUpdateDraftOnlyWhenPublished() {
        Decoration existing = existingDecoration(DecorationStatus.PUBLISHED.getCode());
        existing.setPublishedConfig("{\"schemaVersion\": 2, \"mode\": \"css\"}");
        when(decorationMapper.selectById(3)).thenReturn(existing);
        when(decorationMapper.countByDecorationKey("star_track_frame", 3)).thenReturn(0L);

        String result = decorationService.updateDecoration(3, decorationDto());

        assertThat(result).isNull();
        ArgumentCaptor<Decoration> captor = ArgumentCaptor.forClass(Decoration.class);
        verify(decorationMapper).updateById(captor.capture());
        assertThat(captor.getValue().getDraftConfig()).isEqualTo(FRAME_CONFIG);
        assertThat(captor.getValue().getPublishedConfig()).isEqualTo("{\"schemaVersion\": 2, \"mode\": \"css\"}");
    }

    // 测试更新不存在或已归档装扮时拒绝
    @Test
    void shouldRejectUpdateWhenMissingOrArchived() {
        when(decorationMapper.selectById(3)).thenReturn(null);
        assertThat(decorationService.updateDecoration(3, decorationDto())).isEqualTo("装扮不存在");

        when(decorationMapper.selectById(4)).thenReturn(existingDecoration(DecorationStatus.ARCHIVED.getCode()));
        assertThat(decorationService.updateDecoration(4, decorationDto())).isEqualTo("已归档装扮不可编辑");
        verify(decorationMapper, never()).updateById(any(Decoration.class));
    }

    // 测试发布草稿装扮：配置复制到已发布字段并刷新状态与时间
    @Test
    void shouldPublishDraftDecoration() {
        Decoration draft = existingDecoration(DecorationStatus.DRAFT.getCode());
        when(decorationMapper.selectById(3)).thenReturn(draft);

        String result = decorationService.publishDecoration(3);

        assertThat(result).isNull();
        ArgumentCaptor<Decoration> captor = ArgumentCaptor.forClass(Decoration.class);
        verify(decorationMapper).updateById(captor.capture());
        Decoration published = captor.getValue();
        assertThat(published.getStatus()).isEqualTo(DecorationStatus.PUBLISHED.getCode());
        assertThat(published.getPublishedConfig()).isEqualTo(FRAME_CONFIG);
        assertThat(published.getPublishedAt()).isNotNull();
    }

    // 测试发布草稿配置为空或已归档装扮时拒绝
    @Test
    void shouldRejectPublishWhenDraftEmptyOrArchived() {
        Decoration empty = existingDecoration(DecorationStatus.DRAFT.getCode());
        empty.setDraftConfig(null);
        when(decorationMapper.selectById(3)).thenReturn(empty);
        assertThat(decorationService.publishDecoration(3)).isEqualTo("草稿配置为空，无法发布");

        when(decorationMapper.selectById(4)).thenReturn(existingDecoration(DecorationStatus.ARCHIVED.getCode()));
        assertThat(decorationService.publishDecoration(4)).isEqualTo("已归档装扮不可发布");
        verify(decorationMapper, never()).updateById(any(Decoration.class));
    }

    // 测试归档已发布装扮
    @Test
    void shouldArchivePublishedDecoration() {
        when(decorationMapper.selectById(3)).thenReturn(existingDecoration(DecorationStatus.PUBLISHED.getCode()));

        assertThat(decorationService.archiveDecoration(3)).isNull();
        ArgumentCaptor<Decoration> captor = ArgumentCaptor.forClass(Decoration.class);
        verify(decorationMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(DecorationStatus.ARCHIVED.getCode());
    }

    // 测试归档非已发布装扮时拒绝
    @Test
    void shouldRejectArchiveWhenNotPublished() {
        when(decorationMapper.selectById(3)).thenReturn(existingDecoration(DecorationStatus.DRAFT.getCode()));

        assertThat(decorationService.archiveDecoration(3)).isEqualTo("仅已发布装扮可归档");
        verify(decorationMapper, never()).updateById(any(Decoration.class));
    }

    // 测试仅草稿可软删除
    @Test
    void shouldSoftDeleteOnlyDraft() {
        when(decorationMapper.selectById(3)).thenReturn(existingDecoration(DecorationStatus.DRAFT.getCode()));
        assertThat(decorationService.deleteDecoration(3)).isNull();
        ArgumentCaptor<Decoration> captor = ArgumentCaptor.forClass(Decoration.class);
        verify(decorationMapper).updateById(captor.capture());
        assertThat(captor.getValue().getIsDeleted()).isTrue();

        when(decorationMapper.selectById(4)).thenReturn(existingDecoration(DecorationStatus.PUBLISHED.getCode()));
        assertThat(decorationService.deleteDecoration(4)).isEqualTo("仅草稿状态的装扮可删除");
    }

    // 测试装扮详情返回草稿与已发布双配置
    @Test
    void shouldGetDecorationDetail() {
        Decoration decoration = existingDecoration(DecorationStatus.PUBLISHED.getCode());
        decoration.setPublishedConfig(FRAME_CONFIG);
        when(decorationMapper.selectById(3)).thenReturn(decoration);

        DecorationVO vo = decorationService.getDecoration(3);

        assertThat(vo.getDecorationId()).isEqualTo(3);
        assertThat(vo.getDraftConfig()).isEqualTo(FRAME_CONFIG);
        assertThat(vo.getPublishedConfig()).isEqualTo(FRAME_CONFIG);

        when(decorationMapper.selectById(9)).thenReturn(null);
        assertThat(decorationService.getDecoration(9)).isNull();
    }

    // 测试装扮列表筛选与分页参数归一化
    @Test
    void shouldListDecorationsWithFilters() {
        DecorationVO vo = new DecorationVO();
        vo.setDecorationId(3);
        when(decorationMapper.countDecorations("头像框", "avatar_frame", 1)).thenReturn(1L);
        when(decorationMapper.selectDecorations(0, 10, "头像框", "avatar_frame", 1)).thenReturn(List.of(vo));

        PageEntity<DecorationVO> page = decorationService.listDecorations(
                0, 0, " 头像框 ", ShopItemType.AVATAR_FRAME, DecorationStatus.DRAFT);

        assertThat(page.getTotalSize()).isEqualTo(1L);
        assertThat(page.getData()).containsExactly(vo);
        verify(decorationMapper).selectDecorations(0, 10, "头像框", "avatar_frame", 1);
    }

    // 测试素材上传成功返回 URL，失败返回 null
    @Test
    void shouldUploadAsset() {
        Base64Upload upload = new Base64Upload("data:image/png;base64,abc", "frame.png");
        StoredImage storedImage = mock(StoredImage.class);
        when(storedImage.getUrl()).thenReturn("https://example.com/decoration/frame.webp");
        when(imageStorageService.storeImageBase64Image(eq(upload), eq("decoration/"))).thenReturn(storedImage);

        assertThat(decorationService.uploadAsset(upload)).isEqualTo("https://example.com/decoration/frame.webp");

        assertThat(decorationService.uploadAsset(null)).isNull();

        when(imageStorageService.storeImageBase64Image(any(), eq("decoration/")))
                .thenThrow(new RuntimeException("minio down"));
        assertThat(decorationService.uploadAsset(upload)).isNull();
    }

    private DecorationDTO decorationDto() {
        DecorationDTO dto = new DecorationDTO();
        dto.setName("头像框·星轨");
        dto.setDecorationKey("star_track_frame");
        dto.setDescription("环绕星轨的头像框");
        dto.setType(ShopItemType.AVATAR_FRAME);
        dto.setDraftConfig(FRAME_CONFIG);
        return dto;
    }

    private Decoration existingDecoration(Integer status) {
        Decoration decoration = new Decoration();
        decoration.setDecorationId(3);
        decoration.setDecorationKey("star_track_frame");
        decoration.setName("头像框·星轨");
        decoration.setType("avatar_frame");
        decoration.setStatus(status);
        decoration.setDraftConfig(FRAME_CONFIG);
        decoration.setVersion(0);
        decoration.setIsDeleted(false);
        return decoration;
    }
}
