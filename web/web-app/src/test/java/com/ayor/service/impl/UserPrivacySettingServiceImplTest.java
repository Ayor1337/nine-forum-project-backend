package com.ayor.service.impl;

import com.ayor.entity.dto.UserPrivacySettingDTO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.entity.vo.UserPrivacySettingVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.UserPrivacySettingMapper;
import com.ayor.type.DmPermission;
import com.ayor.type.VisibilityScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPrivacySettingServiceImplTest {

    @Mock
    private UserPrivacySettingMapper userPrivacySettingMapper;

    @Mock
    private AccountMapper accountMapper;

    private UserPrivacySettingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserPrivacySettingServiceImpl(userPrivacySettingMapper, accountMapper);
        ReflectionTestUtils.setField(service, "baseMapper", userPrivacySettingMapper);
    }

    // 测试按账号ID获取隐私设置时拒绝无效账号ID
    @Test
    void getByAccountIdRejectsInvalidAccountId() {
        assertThat(service.getByAccountId(null)).isNull();
        assertThat(service.getByAccountId(0)).isNull();
        verify(userPrivacySettingMapper, never()).selectById(any());
    }

    // 测试账号不存在时初始化默认隐私设置返回空
    @Test
    void initDefaultReturnsNullWhenAccountDoesNotExist() {
        when(accountMapper.getAccountById(7)).thenReturn(null);

        assertThat(service.initDefaultIfAbsent(7)).isNull();
        verify(userPrivacySettingMapper, never()).insert(any(UserPrivacySetting.class));
    }

    // 测试初始化默认隐私设置时直接返回已存在设置
    @Test
    void initDefaultReturnsExistingSetting() {
        UserPrivacySetting existing = setting(7);
        when(accountMapper.getAccountById(7)).thenReturn(new Account());
        when(userPrivacySettingMapper.selectById(7)).thenReturn(existing);

        assertThat(service.initDefaultIfAbsent(7)).isSameAs(existing);
        verify(userPrivacySettingMapper, never()).insert(any(UserPrivacySetting.class));
    }

    // 测试创建默认隐私设置时初始化默认值
    @Test
    void createDefaultInitializesPrivacyDefaults() {
        when(accountMapper.getAccountById(7)).thenReturn(new Account());

        UserPrivacySetting result = service.createDefault(7);

        ArgumentCaptor<UserPrivacySetting> captor = ArgumentCaptor.forClass(UserPrivacySetting.class);
        verify(userPrivacySettingMapper).insert(captor.capture());
        assertThat(result).isSameAs(captor.getValue());
        assertThat(result.getAccountId()).isEqualTo(7);
        assertThat(result.getProfileVisibility()).isEqualTo(VisibilityScope.PUBLIC);
        assertThat(result.getCollectedThreadsVisibility()).isEqualTo(VisibilityScope.PRIVATE);
        assertThat(result.getDmPermission()).isEqualTo(DmPermission.EVERYONE);
        assertThat(result.getCreateTime()).isNotNull();
        assertThat(result.getUpdateTime()).isNotNull();
    }

    // 测试更新隐私设置拒绝缺失用户并 DTO
    @Test
    void updatePrivacySettingRejectsMissingUserAndDto() {
        assertThat(service.updatePrivacySetting(null, dto())).isEqualTo("用户不存在");
        assertThat(service.updatePrivacySetting(7, null)).isEqualTo("隐私设置不能为空");
    }

    // 测试更新隐私设置复制 DTO 并持久化
    @Test
    void updatePrivacySettingCopiesDtoAndPersists() {
        UserPrivacySetting existing = setting(7);
        when(accountMapper.getAccountById(7)).thenReturn(new Account());
        when(userPrivacySettingMapper.selectById(7)).thenReturn(existing);
        when(userPrivacySettingMapper.updateById(any(UserPrivacySetting.class))).thenReturn(1);

        String result = service.updatePrivacySetting(7, dto());

        assertThat(result).isNull();
        assertThat(existing.getProfileVisibility()).isEqualTo(VisibilityScope.PRIVATE);
        assertThat(existing.getDmPermission()).isEqualTo(DmPermission.FOLLOWER_ONLY);
        verify(userPrivacySettingMapper).updateById(existing);
    }

    // 测试更新隐私设置返回消息当更新失败
    @Test
    void updatePrivacySettingReturnsMessageWhenUpdateFails() {
        when(accountMapper.getAccountById(7)).thenReturn(new Account());
        when(userPrivacySettingMapper.selectById(7)).thenReturn(setting(7));
        when(userPrivacySettingMapper.updateById(any(UserPrivacySetting.class))).thenReturn(0);

        assertThat(service.updatePrivacySetting(7, dto())).isEqualTo("更新隐私设置失败");
    }

    // 测试获取隐私设置复制实体到 VO
    @Test
    void getPrivacySettingCopiesEntityToVo() {
        when(accountMapper.getAccountById(7)).thenReturn(new Account());
        when(userPrivacySettingMapper.selectById(7)).thenReturn(setting(7));

        UserPrivacySettingVO result = service.getPrivacySetting(7);

        assertThat(result.getAccountId()).isEqualTo(7);
        assertThat(result.getProfileVisibility()).isEqualTo(VisibilityScope.PUBLIC);
    }

    // 测试缓存注解覆盖读取和更新路径
    @Test
    void cacheAnnotationsProtectReadAndUpdatePaths() throws NoSuchMethodException {
        Method read = UserPrivacySettingServiceImpl.class.getMethod("getByAccountId", Integer.class);
        Method update = UserPrivacySettingServiceImpl.class.getMethod(
                "updatePrivacySetting", Integer.class, UserPrivacySettingDTO.class);

        assertThat(read.getAnnotation(Cacheable.class).value()).containsExactly("userPrivacySetting");
        assertThat(update.getAnnotation(CacheEvict.class).value()).containsExactly("userPrivacySetting");
    }

    private UserPrivacySetting setting(Integer accountId) {
        return UserPrivacySetting.builder()
                .accountId(accountId)
                .profileVisibility(VisibilityScope.PUBLIC)
                .likedThreadsVisibility(VisibilityScope.PUBLIC)
                .collectedThreadsVisibility(VisibilityScope.PRIVATE)
                .followListVisibility(VisibilityScope.PUBLIC)
                .followerListVisibility(VisibilityScope.PUBLIC)
                .birthdayVisibility(VisibilityScope.PRIVATE)
                .dmPermission(DmPermission.EVERYONE)
                .build();
    }

    private UserPrivacySettingDTO dto() {
        UserPrivacySettingDTO dto = new UserPrivacySettingDTO();
        dto.setProfileVisibility(VisibilityScope.PRIVATE);
        dto.setLikedThreadsVisibility(VisibilityScope.PRIVATE);
        dto.setCollectedThreadsVisibility(VisibilityScope.PRIVATE);
        dto.setFollowListVisibility(VisibilityScope.PRIVATE);
        dto.setFollowerListVisibility(VisibilityScope.PRIVATE);
        dto.setBirthdayVisibility(VisibilityScope.PRIVATE);
        dto.setDmPermission(DmPermission.FOLLOWER_ONLY);
        return dto;
    }
}
