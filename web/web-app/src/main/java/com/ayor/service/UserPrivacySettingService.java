package com.ayor.service;

import com.ayor.entity.dto.UserPrivacySettingDTO;
import com.ayor.entity.vo.UserPrivacySettingVO;
import com.ayor.entity.pojo.UserPrivacySetting;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户隐私设置服务接口。
 */
public interface UserPrivacySettingService extends IService<UserPrivacySetting> {

    /**
     * 按用户ID获取隐私设置。
     *
     * @param accountId 用户ID
     * @return 隐私设置实体
     */
    UserPrivacySetting getByAccountId(Integer accountId);

    /**
     * 初始化用户默认隐私设置，若已存在则直接返回。
     *
     * @param accountId 用户ID
     * @return 隐私设置实体
     */
    UserPrivacySetting initDefaultIfAbsent(Integer accountId);

    /**
     * 创建默认隐私设置。
     *
     * @param accountId 用户ID
     * @return 隐私设置实体
     */
    UserPrivacySetting createDefault(Integer accountId);

    /**
     * 更新用户隐私设置。
     *
     * @param accountId 用户ID
     * @param dto 隐私设置请求体
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updatePrivacySetting(Integer accountId, UserPrivacySettingDTO dto);

    /**
     * 获取用户隐私设置视图对象。
     *
     * @param accountId 用户ID
     * @return 隐私设置视图对象
     */
    UserPrivacySettingVO getPrivacySetting(Integer accountId);
}
