package com.ayor.service.impl;

import com.ayor.entity.dto.UserPrivacySettingDTO;
import com.ayor.entity.vo.UserPrivacySettingVO;
import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.UserPrivacySettingMapper;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.type.DmPermission;
import com.ayor.type.VisibilityScope;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 用户隐私设置服务实现。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserPrivacySettingServiceImpl extends ServiceImpl<UserPrivacySettingMapper, UserPrivacySetting>
        implements UserPrivacySettingService {

    private final UserPrivacySettingMapper userPrivacySettingMapper;

    private final AccountMapper accountMapper;

    /**
     * 按用户ID获取隐私设置。
     */
    @Override
    @Cacheable(value = "userPrivacySetting", key = "#accountId", condition = "#accountId != null", unless = "#result == null")
    public UserPrivacySetting getByAccountId(Integer accountId) {
        if (accountId == null || accountId <= 0) {
            return null;
        }
        return initDefaultIfAbsent(accountId);
    }

    /**
     * 初始化用户默认隐私设置，若已存在则直接返回。
     */
    @Override
    public UserPrivacySetting initDefaultIfAbsent(Integer accountId) {
        if (accountId == null || accountId <= 0 || accountMapper.getAccountById(accountId) == null) {
            return null;
        }
        UserPrivacySetting setting = userPrivacySettingMapper.selectById(accountId);
        return setting == null ? createDefault(accountId) : setting;
    }

    /**
     * 创建默认隐私设置。
     */
    @Override
    public UserPrivacySetting createDefault(Integer accountId) {
        if (accountId == null || accountId <= 0 || accountMapper.getAccountById(accountId) == null) {
            return null;
        }
        UserPrivacySetting setting = UserPrivacySetting.builder()
                .accountId(accountId)
                .profileVisibility(VisibilityScope.PUBLIC)
                .likedThreadsVisibility(VisibilityScope.PUBLIC)
                .collectedThreadsVisibility(VisibilityScope.PRIVATE)
                .followListVisibility(VisibilityScope.PUBLIC)
                .followerListVisibility(VisibilityScope.PUBLIC)
                .birthdayVisibility(VisibilityScope.PRIVATE)
                .dmPermission(DmPermission.EVERYONE)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        userPrivacySettingMapper.insert(setting);
        return setting;
    }

    /**
     * 更新用户隐私设置。
     */
    @Override
    @CacheEvict(value = "userPrivacySetting", key = "#accountId", condition = "#accountId != null")
    public String updatePrivacySetting(Integer accountId, UserPrivacySettingDTO dto) {
        if (accountId == null || accountId <= 0) {
            return "用户不存在";
        }
        if (dto == null) {
            return "隐私设置不能为空";
        }
        UserPrivacySetting setting = initDefaultIfAbsent(accountId);
        if (setting == null) {
            return "用户不存在";
        }
        BeanUtils.copyProperties(dto, setting);
        setting.setUpdateTime(new Date());
        return userPrivacySettingMapper.updateById(setting) > 0 ? null : "更新隐私设置失败";
    }

    /**
     * 获取用户隐私设置视图对象。
     */
    @Override
    public UserPrivacySettingVO getPrivacySetting(Integer accountId) {
        UserPrivacySetting setting = initDefaultIfAbsent(accountId);
        if (setting == null) {
            return null;
        }
        UserPrivacySettingVO vo = new UserPrivacySettingVO();
        BeanUtils.copyProperties(setting, vo);
        return vo;
    }
}
